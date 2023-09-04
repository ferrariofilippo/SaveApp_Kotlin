package com.ferrariofilippo.saveapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.ferrariofilippo.saveapp.util.CurrencyUtil
import com.ferrariofilippo.saveapp.util.ImportExportUtil
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.util.StatsUtil
import com.google.android.material.bottomappbar.BottomAppBar
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private var lastFragmentId: Int = R.id.homeFragment

    // Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        val saveApp = application as SaveAppApplication
        SettingsUtil.setStore(saveApp)
        CurrencyUtil.setStore(saveApp)
        runBlocking { StatsUtil.init(saveApp) }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        saveApp.setCurrentActivity(this)

        lifecycleScope.launch { CurrencyUtil.init() }

        setupButtons()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val saveApp = application as SaveAppApplication
        when (requestCode) {
            ImportExportUtil.OPEN_MOVEMENTS_FILE,
            ImportExportUtil.OPEN_SUBSCRIPTIONS_FILE,
            ImportExportUtil.OPEN_BUDGETS_FILE -> {
                if (resultCode == RESULT_OK && data?.data != null) {
                    ImportExportUtil.import(
                        requestCode,
                        contentResolver.openInputStream(data.data!!) as FileInputStream,
                        saveApp
                    )
                }
            }

            ImportExportUtil.CREATE_MOVEMENTS_FILE,
            ImportExportUtil.CREATE_SUBSCRIPTIONS_FILE,
            ImportExportUtil.CREATE_BUDGETS_FILE -> {
                if (resultCode == RESULT_OK && data?.data != null) {
                    ImportExportUtil.export(
                        requestCode,
                        contentResolver?.openOutputStream(data.data!!) as FileOutputStream,
                        saveApp
                    )
                }
            }

            ImportExportUtil.CREATE_MOVEMENTS_TEMPLATE,
            ImportExportUtil.CREATE_SUBSCRIPTIONS_TEMPLATE,
            ImportExportUtil.CREATE_BUDGETS_TEMPLATE -> {
                if (resultCode == RESULT_OK && data?.data != null) {
                    ImportExportUtil.writeTemplate(
                        requestCode,
                        contentResolver?.openOutputStream(data.data!!) as FileOutputStream,
                        saveApp
                    )
                }
            }
        }
    }

    // Methods
    fun goBack() {
        when (lastFragmentId) {
            R.id.homeFragment ->
                findNavController(R.id.containerView).navigate(R.id.homeFragment)

            R.id.historyFragment ->
                findNavController(R.id.containerView).navigate(R.id.historyFragment)

            R.id.budgetsFragment ->
                findNavController(R.id.containerView).navigate(R.id.budgetsFragment)

            R.id.statsFragment ->
                findNavController(R.id.containerView).navigate(R.id.statsFragment)
        }
    }

    fun goToSettings() {
        findNavController(R.id.containerView).navigate(R.id.action_homeFragment_to_settingsFragment)
    }

    fun goToSubscriptions() {
        findNavController(R.id.containerView).navigate(R.id.action_homeFragment_to_subscriptionsFragment)
    }

    fun goToNewBudget() {
        findNavController(R.id.containerView).navigate(R.id.action_budgetsFragment_to_newBudgetFragment)
    }

    private fun setupButtons() {
        val addMovementButton: Button = findViewById(R.id.addMovementFAB)
        addMovementButton.setOnClickListener {
            onAddMovementClick()
        }

        val appBar: BottomAppBar = findViewById(R.id.bottomAppBar)
        appBar.setOnMenuItemClickListener { menuItem: MenuItem ->
            onMenuItemClick(menuItem)
        }
    }

    private fun onAddMovementClick() {
        findNavController(R.id.containerView).navigate(R.id.newMovementFragment)
    }

    private fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.home -> {
                findNavController(R.id.containerView).navigate(R.id.homeFragment)
                lastFragmentId = R.id.homeFragment

                return true
            }

            R.id.history -> {
                findNavController(R.id.containerView).navigate(R.id.historyFragment)
                lastFragmentId = R.id.historyFragment

                return true
            }

            R.id.budget -> {
                findNavController(R.id.containerView).navigate(R.id.budgetsFragment)
                R.id.budgetsFragment

                return true
            }

            R.id.stats -> {
                findNavController(R.id.containerView).navigate(R.id.statsFragment)
                R.id.statsFragment

                return true
            }
        }

        return false
    }
}
