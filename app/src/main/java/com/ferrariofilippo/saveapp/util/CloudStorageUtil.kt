// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.activity.result.IntentSenderRequest
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.await
import androidx.work.workDataOf
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.data.AppDatabase
import com.ferrariofilippo.saveapp.model.SummaryStatistics
import com.ferrariofilippo.saveapp.util.TimeUtil.getInitialDelay
import com.ferrariofilippo.saveapp.view.viewmodels.ManageDataViewModel
import com.ferrariofilippo.saveapp.workers.cloud.GoogleDriveDownloadWorker
import com.ferrariofilippo.saveapp.workers.cloud.GoogleDrivePeriodicUploadWorker
import com.ferrariofilippo.saveapp.workers.cloud.GoogleDriveUploadWorker
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.material.snackbar.Snackbar
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

object CloudStorageUtil {
    private const val CLASS_NAME = "CloudStorageUtil"
    const val PERIODIC_BACKUP_TAG = "periodic_backup_upload"
    const val ONE_TIME_BACKUP_UPLOAD_TAG = "one_time_backup_upload"
    const val ONE_TIME_BACKUP_DOWNLOAD_TAG = "one_time_backup_download"

    private fun displayError(activity: Activity?, messageId: Int) {
        if (activity == null)
            return

        Snackbar.make(
            activity.findViewById(R.id.containerView),
            messageId,
            Snackbar.LENGTH_SHORT
        ).setAnchorView(activity.findViewById(R.id.bottomAppBar)).show()
    }

    private fun authorizeDownload(app: SaveAppApplication, result: AuthorizationResult) {
        LogUtil.logInfo(CLASS_NAME, "authorizeDownload", "Authorizing download...")
        if (result.hasResolution() && result.pendingIntent != null) {
            try {
                val intentSenderRequest =
                    IntentSenderRequest.Builder(result.pendingIntent!!.intentSender).build()
                (app.getCurrentActivity() as MainActivity).downloadBackupFromDrive.launch(
                    intentSenderRequest
                )
            } catch (e: Exception) {
                displayError(app.getCurrentActivity(), R.string.backup_restore_failed)
                Handler(Looper.getMainLooper()).post {
                    ManageDataViewModel.setAreBackupButtonsEnabled(
                        true
                    )
                }
                LogUtil.logException(e, CLASS_NAME, "authorizeDownload")
            }
        } else {
            enqueueDownload(app, result)
        }
    }

    private fun authorizeUpload(app: SaveAppApplication, result: AuthorizationResult) {
        LogUtil.logInfo(CLASS_NAME, "authorizeUpload", "Authorizing upload...")
        if (result.hasResolution() && result.pendingIntent != null) {
            try {
                val intentSenderRequest =
                    IntentSenderRequest.Builder(result.pendingIntent!!.intentSender).build()
                (app.getCurrentActivity() as MainActivity).uploadBackupToDrive.launch(
                    intentSenderRequest
                )
            } catch (e: Exception) {
                displayError(app.getCurrentActivity(), R.string.backup_upload_failed)
                Handler(Looper.getMainLooper()).post {
                    ManageDataViewModel.setAreBackupButtonsEnabled(
                        true
                    )
                }
                LogUtil.logException(e, CLASS_NAME, "authorizeUpload")
            }
        } else {
            enqueueUpload(app, result)
        }
    }

    fun enqueueDownload(app: SaveAppApplication, result: AuthorizationResult) {
        val filesDirPath = app.filesDir.absolutePath
        val downloadWorkRequest: WorkRequest =
            OneTimeWorkRequestBuilder<GoogleDriveDownloadWorker>()
                .setInputData(
                    workDataOf(
                        "ACCESS_TOKEN" to result.accessToken,
                        "SCOPES" to result.grantedScopes.toTypedArray(),
                        "DB_PATH" to app.getDatabasePath(AppDatabase.DB_NAME).absolutePath,
                        "WAL_PATH" to app.getDatabasePath("${AppDatabase.DB_NAME}-wal").absolutePath,
                        "SHM_PATH" to app.getDatabasePath("${AppDatabase.DB_NAME}-shm").absolutePath,
                        "SETTINGS_PATH" to "$filesDirPath/datastore/${SaveAppApplication.SETTINGS_FILE_NAME}.preferences_pb",
                        "STATS_PATH" to "$filesDirPath/${SummaryStatistics.FILE_NAME}"
                    )
                )
                .addTag(ONE_TIME_BACKUP_DOWNLOAD_TAG)
                .build()

        LogUtil.logInfo(CLASS_NAME, "enqueueDownload", "Starting backup download...")
        WorkManager.getInstance(app).enqueue(downloadWorkRequest)
    }

    fun enqueueUpload(app: SaveAppApplication, result: AuthorizationResult) {
        val filesDirPath = app.filesDir.absolutePath
        val uploadWorkRequest: WorkRequest =
            OneTimeWorkRequestBuilder<GoogleDriveUploadWorker>()
                .setInputData(
                    workDataOf(
                        "ACCESS_TOKEN" to result.accessToken,
                        "SCOPES" to result.grantedScopes.toTypedArray(),
                        "DB_PATH" to app.getDatabasePath(AppDatabase.DB_NAME).absolutePath,
                        "WAL_PATH" to app.getDatabasePath("${AppDatabase.DB_NAME}-wal").absolutePath,
                        "SHM_PATH" to app.getDatabasePath("${AppDatabase.DB_NAME}-shm").absolutePath,
                        "SETTINGS_PATH" to "$filesDirPath/datastore/${SaveAppApplication.SETTINGS_FILE_NAME}.preferences_pb",
                        "STATS_PATH" to "$filesDirPath/${SummaryStatistics.FILE_NAME}"
                    )
                )
                .addTag(ONE_TIME_BACKUP_UPLOAD_TAG)
                .build()

        LogUtil.logInfo(CLASS_NAME, "enqueueUpload", "Starting backup upload...")
        WorkManager.getInstance(app).enqueue(uploadWorkRequest)
    }

    fun authorize(app: SaveAppApplication, upload: Boolean) {
        val scopes: List<Scope> = listOf(Scope(DriveScopes.DRIVE_APPDATA))
        val authRequest = AuthorizationRequest.builder().setRequestedScopes(scopes).build()
        Identity.getAuthorizationClient(app).authorize(authRequest)
            .addOnSuccessListener { authorizationResult ->
                if (upload) {
                    authorizeUpload(app, authorizationResult)
                } else {
                    authorizeDownload(app, authorizationResult)
                }
            }.addOnFailureListener {
                displayError(
                    app.getCurrentActivity(),
                    if (upload) R.string.backup_upload_failed else R.string.backup_restore_failed
                )
                Handler(Looper.getMainLooper()).post {
                    ManageDataViewModel.setAreBackupButtonsEnabled(
                        true
                    )
                }
                LogUtil.logException(it, CLASS_NAME, "addOnFailureListener")
            }
    }

    suspend fun updateScheduledBackupUpload(app: SaveAppApplication) {
        val wManager = WorkManager.getInstance(app)
        wManager.cancelAllWorkByTag(PERIODIC_BACKUP_TAG).await()
        wManager.pruneWork().await()

        if (!SettingsUtil.getPeriodicBackupUpload().first()) {
            return
        }

        val requiresWiFi = SettingsUtil.getPeriodicBackupRequiresWiFi().first()
        val constraints = Constraints
            .Builder()
            .setRequiresDeviceIdle(true)
            .setRequiredNetworkType(if (requiresWiFi) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<GoogleDrivePeriodicUploadWorker>()
            .addTag(PERIODIC_BACKUP_TAG)
            .setConstraints(constraints)
            .setInitialDelay(getInitialDelay(3), TimeUnit.MINUTES)
            .build()

        wManager.enqueue(workRequest)
        LogUtil.logInfo(
            CLASS_NAME,
            ::updateScheduledBackupUpload.name,
            "Enqueued backup worker: ${workRequest.id}"
        )
    }
}
