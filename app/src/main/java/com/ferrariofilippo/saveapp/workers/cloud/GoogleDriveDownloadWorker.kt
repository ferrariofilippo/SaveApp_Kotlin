// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.workers.cloud

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.util.LogUtil
import com.ferrariofilippo.saveapp.view.viewmodels.ManageDataViewModel
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.util.Date

class GoogleDriveDownloadWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    private val ctx: Context = context

    private val applicationName = "SaveApp"

    private val fileTypesList = arrayOf("", "-wal", "-shm", "-settings", "-stats")

    private val jsonFactory = GsonFactory.getDefaultInstance()

    override suspend fun doWork(): Result {
        val className = javaClass.kotlin.simpleName ?: ""

        val app = ctx.applicationContext as SaveAppApplication
        val scopes = inputData.getStringArray("SCOPES")?.toMutableList()
        val credentials: GoogleCredentials?
        val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val handler = Handler(Looper.getMainLooper())
        try {
            val token = AccessToken.newBuilder()
                .setTokenValue(inputData.getString("ACCESS_TOKEN"))
                .setExpirationTime(Date.from(Instant.now().plusSeconds(59 * 60)))
                .setScopes(scopes)
                .build()

            credentials = GoogleCredentials.create(token)
        } catch (e: Exception) {
            handler.post { ManageDataViewModel.setAreBackupButtonsEnabled(true) }
            LogUtil.logException(e, javaClass.kotlin.simpleName ?: "", "doWork")
            return Result.failure()
        }

        val initializer: HttpRequestInitializer = HttpCredentialsAdapter(credentials)

        val service: Drive =
            Drive.Builder(httpTransport, jsonFactory, initializer)
                .setApplicationName(applicationName)
                .build()

        try {
            LogUtil.logInfo(className, "doWork", "Download started")
            withContext(Dispatchers.IO) {
                val files = service.files().list()
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageSize(6)
                    .execute()

                for (type in fileTypesList) {
                    val fileCopy = runBlocking {
                        File.createTempFile("db_$type", "", app.cacheDir)
                    }
                    val fileId = files.files
                        .sortedBy { f -> f.name }
                        .lastOrNull { f -> f.name.contains(if (type == "") "db" else type) }?.id

                    if (fileId != null) {
                        service.files().get(fileId)
                            .executeMediaAndDownloadTo(FileOutputStream(fileCopy))

                        val destPath = when (type) {
                            "" -> inputData.getString("DB_PATH")
                            "-wal" -> inputData.getString("WAL_PATH")
                            "-shm" -> inputData.getString("SHM_PATH")
                            "-settings" -> inputData.getString("SETTINGS_PATH")
                            else -> inputData.getString("STATS_PATH")
                        }

                        if (destPath != null) {
                            fileCopy.copyTo(File(destPath), true)
                        }
                    }
                }
            }
            MainActivity.requireCheckpoint()
            LogUtil.logInfo(className, "doWork", "Downloaded backup")
        } catch (e: GoogleJsonResponseException) {
            handler.post { ManageDataViewModel.setAreBackupButtonsEnabled(true) }
            LogUtil.logException(e, className, "doWork")
            return Result.failure()
        }

        handler.post {
            ManageDataViewModel.setAreBackupButtonsEnabled(true)
            MainActivity.requireRestart()
        }

        return Result.success()
    }
}
