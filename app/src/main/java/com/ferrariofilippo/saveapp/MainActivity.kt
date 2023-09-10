package com.ferrariofilippo.saveapp

import android.os.Bundle
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
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private var lastFragmentId: Int = R.id.homeFragment

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
        runBlocking { StatsUtil.init(saveApp) }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        saveApp.setCurrentActivity(this)

        lifecycleScope.launch { SubscriptionUtil.validateSubscriptions(saveApp) }
        lifecycleScope.launch { CurrencyUtil.init() }

        setupButtons()
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

    fun goToEditMovementOrSubscription(id: Int, isMovement: Boolean) {
        val bundle = bundleOf("itemId" to id, "isMovement" to isMovement)
        findNavController(R.id.containerView).navigate(R.id.newMovementFragment, bundle)
    }

    fun goToEditBudget(id: Int) {
        val bundle = bundleOf("itemId" to id)
        findNavController(R.id.containerView).navigate(
            R.id.action_budgetsFragment_to_newBudgetFragment,
            bundle
        )
    }

    fun goToManageTags() {
        findNavController(R.id.containerView).navigate(R.id.action_settingsFragment_to_manageTagsFragment)
    }

    fun gotToAddOrEditTag(id: Int) {
        val bundle = bundleOf("tagId" to id)
        findNavController(R.id.containerView).navigate(
            R.id.action_manageTagsFragment_to_newTagFragment,
            bundle
        )
    }

    fun goBackToManageTags() {
        findNavController(R.id.containerView).navigate(R.id.action_newTagFragment_to_manageTagsFragment)
    }

    fun updateAllToNewCurrency(value: Currencies) {
        lifecycleScope.launch {
            _isUpdatingCurrencies.value = true
            CurrencyUtil.updateAllToNewCurrency(value)
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
