// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.workers.cloud

import android.content.Context
import androidx.activity.result.IntentSenderRequest
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.util.CloudStorageUtil
import com.ferrariofilippo.saveapp.util.LogUtil
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

class GoogleDrivePeriodicUploadWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    private val CLASS_NAME = javaClass.kotlin.simpleName ?: ""

    private val app = context.applicationContext as SaveAppApplication

    override suspend fun doWork(): Result {
        LogUtil.logInfo(CLASS_NAME, "doWork", "Beginning upload process...")
        val scopes: List<Scope> = listOf(Scope(DriveScopes.DRIVE_APPDATA))
        val authRequest = AuthorizationRequest.builder().setRequestedScopes(scopes).build()
        Identity.getAuthorizationClient(app).authorize(authRequest)
            .addOnSuccessListener { authorizationResult ->
                authorizePeriodicUpload(app, authorizationResult)
            }.addOnFailureListener {
                LogUtil.logException(it, CLASS_NAME, "addOnFailureListener")
            }

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
}
