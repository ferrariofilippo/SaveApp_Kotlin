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
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.view.viewmodels.ManageDataViewModel
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.FileContent
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Collections
import java.util.Date

class GoogleDriveUploadWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    private val applicationName = "SaveApp"

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    private val fileTypesList = arrayOf("", "-wal", "-shm", "-settings", "-stats")

    private val jsonFactory = GsonFactory.getDefaultInstance()

    private val ctx: Context = context

    private fun deleteOldBackup(service: Drive): Boolean {
        val className = javaClass.kotlin.simpleName ?: ""
        try {
            val lastTimeStamp = SettingsUtil.lastBackupTimeStamp
            val files = service.files().list()
                .setSpaces("appDataFolder")
                .setFields("nextPageToken, files(id, name)")
                .setPageSize(10)
                .execute()

            for (type in fileTypesList) {
                val fileName = "saveapp${if (type == "") "-db" else type}_${lastTimeStamp}"

                val fileId = files.files.firstOrNull { f -> f.name == fileName }?.id
                if (fileId != null) {
                    service.files().delete(fileId)
                }
            }
            LogUtil.logInfo(className, "deleteOldBackup", "Deleted old backup")
        } catch (e: GoogleJsonResponseException) {
            LogUtil.logException(e, className, "deleteOldBackup")
            return false
        }

        return true
    }

    override suspend fun doWork(): Result {
        val className = javaClass.kotlin.simpleName ?: ""

        val app = ctx.applicationContext as SaveAppApplication
        val scopes = inputData.getStringArray("SCOPES")?.toMutableList()
        val timeStamp = LocalDateTime.now().format(dateTimeFormatter)
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
            MainActivity.requireCheckpoint()
            withContext(Dispatchers.IO) {
                for (type in fileTypesList) {
                    val fileName = "saveapp${if (type == "") "-db" else type}_${timeStamp}"
                    val fileMetadata = com.google.api.services.drive.model.File()
                    fileMetadata.setName(fileName)
                    fileMetadata.setParents(Collections.singletonList("appDataFolder"))

                    val fileCopy = File.createTempFile(fileName, "", app.cacheDir)
                    val mediaContent = FileContent("application/octet-stream", fileCopy)
                    val srcPath = when (type) {
                        "" -> inputData.getString("DB_PATH")
                        "-wal" -> inputData.getString("WAL_PATH")
                        "-shm" -> inputData.getString("SHM_PATH")
                        "-settings" -> inputData.getString("SETTINGS_PATH")
                        else -> inputData.getString("STATS_PATH")
                    }

                    if (srcPath != null) {
                        val src = File(srcPath)
                        if (src.exists() && src.length() > 0) {
                            src.copyTo(fileCopy, true)
                            service.files().create(fileMetadata, mediaContent)
                                .setFields("id")
                                .execute()
                            LogUtil.logInfo(className, "doWork", "Uploaded $fileName")
                        }
                    }
                    fileCopy.delete()
                }
            }
            LogUtil.logInfo(className, "doWork", "Upload finished")
        } catch (e: GoogleJsonResponseException) {
            handler.post { ManageDataViewModel.setAreBackupButtonsEnabled(true) }
            LogUtil.logException(e, className, "doWork")
            return Result.failure()
        }

        withContext(Dispatchers.IO) { deleteOldBackup(service) }
        SettingsUtil.setStore(app)
        SettingsUtil.setLastBackupTimeStamp(timeStamp)
        SettingsUtil.setPeriodicBackupVisible(true)
        handler.post { ManageDataViewModel.setAreBackupButtonsEnabled(true) }
        return Result.success()
    }
}
