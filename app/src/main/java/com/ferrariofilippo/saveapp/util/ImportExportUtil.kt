package com.ferrariofilippo.saveapp.util

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import androidx.core.app.ActivityCompat.startActivityForResult
import com.ferrariofilippo.saveapp.model.entities.Movement
import com.ferrariofilippo.saveapp.model.entities.Subscription
import com.ferrariofilippo.saveapp.util.DateUtil.toLocalDateOrNull
import java.time.LocalDate

object ImportExportUtil {
    private const val CREATE_FILE: Int = 1;

    private const val OPEN_FILE: Int = 2;

    fun exportData() {

    }

    fun importData() {

    }

    fun getTemplateFile() {

    }

    private fun exportMovements(activity: Activity, movements: List<Movement>) {
        val intent: Intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE);
            type = "text/csv";

            putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.DIRECTORY_DOWNLOADS);
        };

        startActivityForResult(activity, intent, CREATE_FILE, Bundle.EMPTY);
    }

    private fun exportSubscriptions(activity: Activity, subscriptions: List<Subscription>) {
        val intent: Intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE);
            type = "text/csv";

            putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.DIRECTORY_DOWNLOADS);
        };

        startActivityForResult(activity, intent, CREATE_FILE, Bundle.EMPTY);
    }

    private fun getMovementsFromFile(activity: Activity): List<Movement> {
        val movements: List<Movement> = mutableListOf();

        val intent: Intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE);
            type = "text/csv";

            putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.DIRECTORY_DOCUMENTS);
        };

        startActivityForResult(activity, intent, OPEN_FILE, null);

        return movements;
    }

    private fun String.toMovementOrNull(): Movement? {
        val fields: List<String> = this.split(';');

        val id: Int = fields[0].toIntOrNull() ?: return null;
        val amount: Double = fields[1].toDoubleOrNull() ?: return null;
        val description = fields[2];
        val date: LocalDate = fields[3].toLocalDateOrNull() ?: return null;
        val tagId = fields[4].toIntOrNull() ?: return null;
        val budgetId: Int = fields[5].toIntOrNull() ?: return null;

        return Movement(
            id, amount, description, date, tagId, budgetId
        );
    }
}