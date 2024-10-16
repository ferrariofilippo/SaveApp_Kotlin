// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.workers.cloud

import android.app.PendingIntent.CanceledException
import android.content.Context
import androidx.activity.result.IntentSenderRequest
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.enums.PeriodicInterval
import com.ferrariofilippo.saveapp.util.CloudStorageUtil
import com.ferrariofilippo.saveapp.util.CloudStorageUtil.PERIODIC_BACKUP_TAG
import com.ferrariofilippo.saveapp.util.LogUtil
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.util.TimeUtil.getInitialDelay
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class GoogleDrivePeriodicUploadWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    private val CLASS_NAME = javaClass.kotlin.simpleName ?: ""

    private val _app = context.applicationContext as SaveAppApplication
    private var _nextScheduled = false

    override suspend fun doWork(): Result {
        try {
            LogUtil.logInfo(CLASS_NAME, ::doWork.name, "${this.id} beginning upload process...")
            val scopes: List<Scope> = listOf(Scope(DriveScopes.DRIVE_APPDATA))
            val authRequest = AuthorizationRequest.builder().setRequestedScopes(scopes).build()
            Identity.getAuthorizationClient(_app).authorize(authRequest)
                .addOnSuccessListener { authorizationResult ->
                    authorizePeriodicUpload(_app, authorizationResult)
                }.addOnFailureListener {
                    LogUtil.logException(it, CLASS_NAME, "addOnFailureListener")
                }
        } catch (e: CanceledException) {
            LogUtil.logInfo(CLASS_NAME, ::doWork.name, "${this.id} cancelled")
        }

        scheduleNextBackup()

        return Result.success()
    }

    private fun authorizePeriodicUpload(app: SaveAppApplication, result: AuthorizationResult) {
        LogUtil.logInfo(CLASS_NAME, "authorizePeriodicUpload", "Authorizing upload...")
        if (result.hasResolution() && result.pendingIntent != null) {
            try {
                val mainAct = app.getCurrentActivity() as MainActivity
                mainAct.uploadBackupToDrive.launch(
                    IntentSenderRequest.Builder(result.pendingIntent!!.intentSender).build()
                )
            } catch (e: Exception) {
                LogUtil.logException(e, CLASS_NAME, "authorizePeriodicUpload")
            }
        } else {
            CloudStorageUtil.enqueueUpload(app, result)
        }
    }

    private suspend fun scheduleNextBackup() {
        if (_nextScheduled) {
            LogUtil.logInfo(CLASS_NAME, ::scheduleNextBackup.name, "Next backup already scheduled")

            return
        }

        val wManager = WorkManager.getInstance(_app)
        SettingsUtil.setStore(_app)
        if (!SettingsUtil.getPeriodicBackupUpload().first()) {
            return
        }

        wManager.pruneWork().await()

        val interval = SettingsUtil.getPeriodicBackupInterval().first().toLong()
        val delay = getInitialDelay(3, interval - PeriodicInterval.DAILY.minutes)
        val requiresWiFi = SettingsUtil.getPeriodicBackupRequiresWiFi().first()
        val constraints = Constraints
            .Builder()
            .setRequiresDeviceIdle(true)
            .setRequiredNetworkType(if (requiresWiFi) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<GoogleDrivePeriodicUploadWorker>()
            .addTag(PERIODIC_BACKUP_TAG)
            .setConstraints(constraints)
            .setInitialDelay(delay, TimeUnit.MINUTES)
            .build()

        wManager.enqueue(workRequest)
        _nextScheduled = true
        LogUtil.logInfo(
            CLASS_NAME,
            ::scheduleNextBackup.name,
            "Next backup scheduled in $delay minutes"
        )
    }
}
