// Copyright (c) 2025 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.work.WorkManager
import androidx.work.await
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.util.BudgetUtil
import com.ferrariofilippo.saveapp.util.CloudStorageUtil
import com.ferrariofilippo.saveapp.util.ColorUtil
import com.ferrariofilippo.saveapp.util.CurrencyUtil
import com.ferrariofilippo.saveapp.util.ImportExportUtil
import com.ferrariofilippo.saveapp.util.LogUtil
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.util.SpacingUtil
import com.ferrariofilippo.saveapp.util.StatsUtil
import com.ferrariofilippo.saveapp.util.SubscriptionUtil
import com.ferrariofilippo.saveapp.util.TagUtil
import com.ferrariofilippo.saveapp.util.ThemeManagerUtil
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.color.DynamicColors
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    companion object {
        private const val OPEN_NEW_TRANSACTION_PAGE_INTENT_KEY =
            "com.ferrariofilippo.saveapp.launchNewTransaction"
        private const val OPEN_NEW_TRANSACTION_PAGE_INTENT_CODE = 1

        private var _restartFunction: () -> Unit = { }

        private var _checkpointFunction: () -> Unit = { }

        fun requireRestart() {
            _restartFunction()
        }

        fun requireCheckpoint() {
            _checkpointFunction()
        }
    }

    private var _shouldOpenNewTransactionPage = false

    private var navControllerInitialized = false

    private lateinit var rootDestinations: Set<Int>

    private val _isUpdatingCurrencies: MutableLiveData<Boolean> = MutableLiveData(false)

    val isUpdatingCurrencies: LiveData<Boolean> = _isUpdatingCurrencies

    // IO Activities
    val exportTransactions = registerForActivityResult(CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            ImportExportUtil.export(
                ImportExportUtil.CREATE_TRANSACTIONS_FILE,
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

    val importTransactions = registerForActivityResult(GetContent()) { uri ->
        if (uri != null) {
            ImportExportUtil.import(
                ImportExportUtil.OPEN_TRANSACTIONS_FILE,
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

    val createTransactionsTemplate = registerForActivityResult(CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            ImportExportUtil.writeTemplate(
                ImportExportUtil.CREATE_TRANSACTIONS_TEMPLATE,
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

    val uploadBackupToDrive =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            try {
                val authResult: AuthorizationResult = Identity.getAuthorizationClient(this)
                    .getAuthorizationResultFromIntent(result.data)
                CloudStorageUtil.enqueueUpload(application as SaveAppApplication, authResult)
            } catch (e: Exception) {
                LogUtil.logException(e, javaClass.kotlin.simpleName ?: "", "uploadBackupToDrive")
            }
        }
    val downloadBackupFromDrive =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            try {
                val authResult: AuthorizationResult = Identity.getAuthorizationClient(this)
                    .getAuthorizationResultFromIntent(result.data)

                CloudStorageUtil.enqueueDownload(application as SaveAppApplication, authResult)
            } catch (e: Exception) {
                LogUtil.logException(
                    e,
                    javaClass.kotlin.simpleName ?: "",
                    "downloadBackupFromDrive"
                )
            }
        }

    val exportLogFile = registerForActivityResult(CreateDocument("text/plain")) { uri ->
        if (uri != null) {
            LogUtil.exportLogTo(contentResolver?.openOutputStream(uri) as FileOutputStream)
        }
    }

    // Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        val saveApp = application as SaveAppApplication
        SettingsUtil.setStore(saveApp)
        CurrencyUtil.setStore(saveApp)
        TagUtil.updateAll(saveApp)
        TagUtil.setNameTemplate(saveApp.getString(R.string.tag_complete_name))
        BudgetUtil.init(saveApp)
        StatsUtil.init(saveApp)
        SpacingUtil.init(saveApp)
        ColorUtil.initColors(saveApp)

        _restartFunction = { restartApplication() }
        _checkpointFunction = { saveApp.utilRepository.checkpoint() }

        val themeId = runBlocking { ThemeManagerUtil.getCurrentThemeResId() }
        if (themeId == 0) {
            DynamicColors.applyToActivityIfAvailable(this)
        } else {
            setTheme(themeId)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        saveApp.setCurrentActivity(this)

        lifecycleScope.launch { SubscriptionUtil.validateSubscriptions(saveApp) }
        lifecycleScope.launch { CurrencyUtil.init() }

        setupButtons()

        _shouldOpenNewTransactionPage = intent.getIntExtra(
            OPEN_NEW_TRANSACTION_PAGE_INTENT_KEY,
            0
        ) == OPEN_NEW_TRANSACTION_PAGE_INTENT_CODE
    }

    override fun onStart() {
        super.onStart()
        val ctx = this
        lifecycleScope.launch {
            val manager = WorkManager.getInstance(ctx)
            manager.cancelAllWorkByTag(CloudStorageUtil.ONE_TIME_BACKUP_DOWNLOAD_TAG).await()
            manager.cancelAllWorkByTag(CloudStorageUtil.ONE_TIME_BACKUP_UPLOAD_TAG).await()
            manager.pruneWork().await()

            StatsUtil.startIntegrityCheckInterval(ctx.application as SaveAppApplication)
        }

        if (_shouldOpenNewTransactionPage) {
            _shouldOpenNewTransactionPage = false
            onAddTransactionClick()
        }
    }

    // Methods
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

    fun goToEditTransactionOrSubscription(id: Int, isTransaction: Boolean) {
        ensureNavControllerInitialized()

        val bundle = bundleOf("itemId" to id, "isTransaction" to isTransaction)
        findNavController(R.id.containerView).navigate(R.id.newTransactionFragment, bundle)
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

    fun goToManageData() {
        ensureNavControllerInitialized()
        findNavController(R.id.containerView).navigate(R.id.action_settingsFragment_to_manageDataFragment)
    }

    fun gotToAddOrEditTag(id: Int) {
        ensureNavControllerInitialized()

        val bundle = bundleOf("tagId" to id)
        findNavController(R.id.containerView).navigate(
            R.id.action_manageTagsFragment_to_newTagFragment,
            bundle
        )
    }

    fun popLastView() {
        ensureNavControllerInitialized()
        findNavController(R.id.containerView).popBackStack()
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
        val addTransactionButton: FloatingActionButton = findViewById(R.id.addTransactionFAB)
        addTransactionButton.setOnClickListener {
            onAddTransactionClick()
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
            val fab = findViewById<FloatingActionButton>(R.id.addTransactionFAB)
            if (rootDestinations.contains(destination.id)) {
                fab.show()
            } else {
                fab.hide()
            }
        }

        navControllerInitialized = true
    }

    private fun onAddTransactionClick() {
        try {
            ensureNavControllerInitialized()
            val navController = findNavController(R.id.containerView)
            navController.navigate(
                when (navController.currentDestination?.id) {
                    R.id.historyFragment -> R.id.action_historyFragment_to_newTransactionFragment
                    R.id.budgetsFragment -> R.id.action_budgetsFragment_to_newTransactionFragment
                    R.id.statsFragment -> R.id.action_statsFragment_to_newTransactionFragment
                    else -> R.id.action_homeFragment_to_newTransactionFragment
                }
            )
        } catch (e: Exception) {
            LogUtil.logException(e, javaClass.kotlin.simpleName ?: "", "onAddTransactionClick")
        }
    }

    private fun onMenuItemClick(menuItem: MenuItem): Boolean {
        ensureNavControllerInitialized()

        val navController = findNavController(R.id.containerView)
        val current = navController.currentDestination
        val destinationId = when (menuItem.itemId) {
            R.id.home -> R.id.homeFragment
            R.id.history -> R.id.historyFragment
            R.id.budget -> R.id.budgetsFragment
            else -> R.id.statsFragment
        }

        if (current == null || current.id != destinationId) {
            navController.clearBackStack(destinationId)
            navController.navigate(destinationId)
            return true
        }

        return false
    }

    private fun restartApplication() {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return
        val startIntent = Intent.makeRestartActivityTask(intent.component)
        startIntent.`package` = packageName
        startActivity(startIntent)
        Runtime.getRuntime().exit(0)
    }
}
