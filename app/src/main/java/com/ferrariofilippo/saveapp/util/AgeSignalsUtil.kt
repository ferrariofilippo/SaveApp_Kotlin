// Copyright (c) 2026 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.util.DateUtil.toLocalDateOrNull
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

object AgeSignalsUtil {
    private var _attempts = 0;
    fun verifyUserAge(activity: Activity, onSuccess: () -> Unit = {}) {
        if (LocalDate.now() < LocalDate.of(2026, 1, 1) || !isCheckNecessary()) {
            onSuccess()
            return
        }

        val ageSignalsManager = AgeSignalsManagerFactory.create(activity)
        ageSignalsManager
            .checkAgeSignals(AgeSignalsRequest.builder().build())
            .addOnSuccessListener { ageSignalsResult ->
                val msgId = when (ageSignalsResult.userStatus()) {
                    AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_DENIED ->
                        R.string.parental_access_denied

                    AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_PENDING ->
                        R.string.parental_access_pending

                    AgeSignalsVerificationStatus.VERIFIED -> 0
                    AgeSignalsVerificationStatus.SUPERVISED ->
                        if ((ageSignalsResult.ageLower()
                                ?: 0) < 3
                        ) 0 else R.string.minimum_age_warning

                    else -> R.string.unknown_age_verification
                }

                if (msgId == 0) {
                    updateLastCheck()
                    onSuccess()
                } else {
                    showErrorMessageAndCloseApp(activity, msgId)
                }
            }
            .addOnFailureListener { exc ->
                try {
                    val code = (exc.message?.split(':')[0] ?: "-1").toInt()
                    if (!arrayOf(-1, -9, -100).contains(code)) {
                        val msg = when (code) {
                            -2 -> R.string.age_verification_error_2
                            -3 -> R.string.age_verification_error_3
                            -4 -> R.string.age_verification_error_4
                            -5 -> R.string.age_verification_error_5
                            -6 -> R.string.age_verification_error_6
                            -7 -> R.string.age_verification_error_7
                            -8 -> R.string.age_verification_error_8
                            else -> R.string.unable_to_verify_age
                        };
                        showErrorMessageAndCloseApp(activity, msg)
                    } else {
                        updateLastCheck()
                    }
                } catch (fe: NumberFormatException) {
                    updateLastCheck()
                }
            }
    }

    private fun showErrorMessageAndCloseApp(activity: Activity, msg: Int) {
        runBlocking { SettingsUtil.setAgeVerificationAttempts(++_attempts) }
        if (activity.isFinishing || activity.isDestroyed)
            return

        AlertDialog.Builder(activity)
            .setTitle(getLocalizedString(activity, R.string.age_verification_error))
            .setMessage(getLocalizedString(activity, msg))
            .setCancelable(false)
            .setPositiveButton(getLocalizedString(activity, R.string.close_app)) { _, _ ->
                Runtime.getRuntime().exit(0)
            }
            .setOnDismissListener {
                Runtime.getRuntime().exit(0)
            }
            .show()
    }

    private fun getLocalizedString(act: Activity, resId: Int): String {
        return act.getString(resId)
    }

    private fun isCheckNecessary(): Boolean {
        val lastTimeStamp = runBlocking { SettingsUtil.getLastAgeVerificationTimeStamp().first() }
        _attempts = runBlocking { SettingsUtil.getAgeVerificationAttempts().first() }
        val date = lastTimeStamp.toLocalDateOrNull()

        return date == null ||
                (LocalDate.now() > date.plusMonths(2) && _attempts < 5)
    }

    private fun updateLastCheck() {
        runBlocking {
            SettingsUtil.setLastAgeVerificationTimeStamp(
                LocalDate.now().toString()
            )
        }
        runBlocking { SettingsUtil.setAgeVerificationAttempts(0) }
    }
}
