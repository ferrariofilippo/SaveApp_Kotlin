// Copyright (c) 2025 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.ferrariofilippo.saveapp.R
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus
import java.time.LocalDate

object AgeSignalsUtil {
    fun verifyUserAge(activity: Activity, onSuccess: () -> Unit = {}) {
        if (LocalDate.now() < LocalDate.of(2026, 1, 1)) {
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
                        if ((ageSignalsResult.ageLower() ?: 0) < 3) 0 else R.string.minimum_age_warning
                    else -> R.string.unknown_age_verification
                }

                if (msgId == 0) {
                    onSuccess()
                } else {
                    showErrorMessageAndCloseApp(activity, msgId)
                }
            }
            .addOnFailureListener { exc ->
                showErrorMessageAndCloseApp(activity, R.string.unable_to_verify_age)
            }
    }

    private fun showErrorMessageAndCloseApp(activity: Activity, msg: Int) {
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
}
