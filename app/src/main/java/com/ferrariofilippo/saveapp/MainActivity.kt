// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.util.BudgetUtil
import com.ferrariofilippo.saveapp.util.CurrencyUtil
import com.ferrariofilippo.saveapp.util.ImportExportUtil
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.util.StatsUtil
import com.ferrariofilippo.saveapp.util.SubscriptionUtil
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private var lastFragmentId: Int = R.id.homeFragment

    private var navControllerInitialized = false

    private lateinit var rootDestinations: Set<Int>

    private val _isUpdatingCurrencies: MutableLiveData<Boolean> = MutableLiveData(false)

    val isUpdatingCurrencies: LiveData<Boolean> = _isUpdatingCurrencies

    // IO Activities
    val exportMovements = registerForActivityResult(CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            ImportExportUtil.export(
                ImportExportUtil.CREATE_MOVEMENTS_FILE,
                contentResolver?.openOutputStream(uri) as FileOutputStream,
                application as SaveAppApplication
            )
        }
    }
    val exportSubscriptions = registerForActivityResult(CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            ImportExportUtil.export(
                ImportExportUtil.CREATE_SUBSCRIPTIONS_FILE,
                contentResolver?.openOutputStream(uri) as FileOutputStream,
                application as SaveAppApplication
            )
        }
    }
    val exportBudgets = registerForActivityResult(CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            ImportExportUtil.export(
                ImportExportUtil.CREATE_BUDGETS_FILE,
                contentResolver?.openOutputStream(uri) as FileOutputStream,
                application as SaveAppApplication
            )
        }
    }

    val importMovements = registerForActivityResult(GetContent()) { uri ->
        if (uri != null) {
            ImportExportUtil.import(
                ImportExportUtil.OPEN_MOVEMENTS_FILE,
                contentResolver.openInputStream(uri) as FileInputStream,
                application as SaveAppApplication
            )
        }
    }
    val importSubscriptions = registerForActivityResult(GetContent()) { uri ->
        if (uri != null) {
            ImportExportUtil.import(
                ImportExportUtil.OPEN_SUBSCRIPTIONS_FILE,
                contentResolver.openInputStream(uri) as FileInputStream,
                application as SaveAppApplication
            )
        }
    }
    val importBudgets = registerForActivityResult(GetContent()) { uri ->
        if (uri != null) {
            ImportExportUtil.import(
                ImportExportUtil.OPEN_BUDGETS_FILE,
                contentResolver.openInputStream(uri) as FileInputStream,
                application as SaveAppApplication
            )
        }
    }

    val createMovementsTemplate = registerForActivityResult(CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            ImportExportUtil.writeTemplate(
                ImportExportUtil.CREATE_MOVEMENTS_TEMPLATE,
                contentResolver?.openOutputStream(uri) as FileOutputStream,
                application as SaveAppApplication
            )
        }
    }
    val createSubscriptionsTemplate = registerForActivityResult(CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            ImportExportUtil.writeTemplate(
                ImportExportUtil.CREATE_SUBSCRIPTIONS_TEMPLATE,
                contentResolver?.openOutputStream(uri) as FileOutputStream,
                application as SaveAppApplication
            )
        }
    }
    val createBudgetsTemplate = registerForActivityResult(CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            ImportExportUtil.writeTemplate(
                ImportExportUtil.CREATE_BUDGETS_TEMPLATE,
                contentResolver?.openOutputStream(uri) as FileOutputStream,
                application as SaveAppApplication
            )
        }
    }

    // Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        val saveApp = application as SaveAppApplication
        SettingsUtil.setStore(saveApp)
        CurrencyUtil.setStore(saveApp)
        BudgetUtil.init(saveApp)
        StatsUtil.init(saveApp)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        saveApp.setCurrentActivity(this)

        lifecycleScope.launch { SubscriptionUtil.validateSubscriptions(saveApp) }
        lifecycleScope.launch { CurrencyUtil.init() }

        setupButtons()
    }

    // Methods
    fun goBack() {
        ensureNavControllerInitialized()

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
        ensureNavControllerInitialized()
        findNavController(R.id.containerView).navigate(R.id.action_homeFragment_to_settingsFragment)
    }

    fun goToSubscriptions() {
        ensureNavControllerInitialized()
        findNavController(R.id.containerView).navigate(R.id.action_homeFragment_to_subscriptionsFragment)
    }

    fun goToNewBudget() {
        ensureNavControllerInitialized()
        findNavController(R.id.containerView).navigate(R.id.action_budgetsFragment_to_newBudgetFragment)
    }

    fun goToEditMovementOrSubscription(id: Int, isMovement: Boolean) {
        ensureNavControllerInitialized()

        val bundle = bundleOf("itemId" to id, "isMovement" to isMovement)
        findNavController(R.id.containerView).navigate(R.id.newMovementFragment, bundle)
    }

    fun goToEditBudget(id: Int) {
        ensureNavControllerInitialized()

        val bundle = bundleOf("itemId" to id)
        findNavController(R.id.containerView).navigate(
            R.id.action_budgetsFragment_to_newBudgetFragment,
            bundle
        )
    }

    fun goToManageTags() {
        ensureNavControllerInitialized()
        findNavController(R.id.containerView).navigate(R.id.action_settingsFragment_to_manageTagsFragment)
    }

    fun gotToAddOrEditTag(id: Int) {
        ensureNavControllerInitialized()

        val bundle = bundleOf("tagId" to id)
        findNavController(R.id.containerView).navigate(
            R.id.action_manageTagsFragment_to_newTagFragment,
            bundle
        )
    }

    fun goBackToManageTags() {
        ensureNavControllerInitialized()
        findNavController(R.id.containerView).navigate(R.id.action_newTagFragment_to_manageTagsFragment)
    }

    fun updateAllToNewCurrency(value: Currencies) {
        lifecycleScope.launch {
            _isUpdatingCurrencies.value = true
            CurrencyUtil.updateAllToNewCurrency(application, value)
            _isUpdatingCurrencies.value = false

            Snackbar.make(
                findViewById(R.id.containerView),
                R.string.default_currency_updated,
                Snackbar.LENGTH_SHORT
            ).setAnchorView(findViewById(R.id.bottomAppBar)).show()
        }
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

    private fun ensureNavControllerInitialized() {
        if (navControllerInitialized)
            return

        rootDestinations =
            setOf(R.id.homeFragment, R.id.historyFragment, R.id.budgetsFragment, R.id.statsFragment)

        findNavController(R.id.containerView).addOnDestinationChangedListener { _, destination, _ ->
            val fab = findViewById<ExtendedFloatingActionButton>(R.id.addMovementFAB)
            if (rootDestinations.contains(destination.id)) {
                fab.show()
            } else {
                fab.hide()
            }
        }

        lastFragmentId = R.id.homeFragment
        navControllerInitialized = true
    }

    private fun onAddMovementClick() {
        ensureNavControllerInitialized()

        try {
            when (lastFragmentId) {
                R.id.homeFragment ->
                    findNavController(R.id.containerView).navigate(R.id.action_homeFragment_to_newMovementFragment)

                R.id.historyFragment ->
                    findNavController(R.id.containerView).navigate(R.id.action_historyFragment_to_newMovementFragment)

                R.id.budgetsFragment ->
                    findNavController(R.id.containerView).navigate(R.id.action_budgetsFragment_to_newMovementFragment)

                R.id.statsFragment ->
                    findNavController(R.id.containerView).navigate(R.id.action_statsFragment_to_newMovementFragment)
            }
        } catch (e: Exception) {
            Log.e("NAV_E", e.message.toString())
        }
    }

    private fun onMenuItemClick(menuItem: MenuItem): Boolean {
        ensureNavControllerInitialized()

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
                lastFragmentId = R.id.budgetsFragment

                return true
            }

            R.id.stats -> {
                findNavController(R.id.containerView).navigate(R.id.statsFragment)
                lastFragmentId = R.id.statsFragment

                return true
            }
        }

        return false
    }
}
