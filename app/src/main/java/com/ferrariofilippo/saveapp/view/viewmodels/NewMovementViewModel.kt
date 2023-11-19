package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import android.view.View
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ferrariofilippo.saveapp.BR
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.entities.Movement
import com.ferrariofilippo.saveapp.model.entities.Subscription
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.ferrariofilippo.saveapp.model.enums.AddToBudgetResult
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.enums.RenewalType
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedBudget
import com.ferrariofilippo.saveapp.util.BudgetUtil
import com.ferrariofilippo.saveapp.util.CurrencyUtil
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.util.StatsUtil
import com.ferrariofilippo.saveapp.util.SubscriptionUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class NewMovementViewModel(application: Application) : AndroidViewModel(application), Observable {
    private val saveAppApplication = application as SaveAppApplication

    private val movementRepository = saveAppApplication.movementRepository
    private val subscriptionRepository = saveAppApplication.subscriptionRepository

    private val callbacks: PropertyChangeRegistry = PropertyChangeRegistry()

    val baseCurrency: Currencies =
        runBlocking { Currencies.from(SettingsUtil.getCurrency().first()) }

    private var _amount: String = ""
    private var _currency: Currencies = baseCurrency
    private var _description: String = ""
    private var _date: LocalDate = LocalDate.now()
    private var _tag: Tag? = null
    private var _budget: TaggedBudget? = null
    private var _isSubscription: Boolean = false
    private var _renewalType: RenewalType = RenewalType.WEEKLY

    private var _isSubscriptionSwitchEnabled: Boolean = true

    val tags: MutableLiveData<Array<Tag>> = MutableLiveData<Array<Tag>>()

    val budgets: MutableLiveData<Array<TaggedBudget>> = MutableLiveData<Array<TaggedBudget>>()

    val currencies: MutableLiveData<Array<Currencies>> =
        MutableLiveData<Array<Currencies>>(Currencies.values())

    val renewalTypes: MutableLiveData<Array<RenewalType>> =
        MutableLiveData<Array<RenewalType>>(RenewalType.values())

    var editingSubscription: Subscription? = null
    var editingMovement: Movement? = null

    var onAmountChanged: () -> Unit = { }
    var onDescriptionChanged: () -> Unit = { }
    var validateTag: () -> Unit = { }

    init {
        viewModelScope.launch {
            tags.value = saveAppApplication.tagRepository.allTags.first().toTypedArray()
            budgets.value = saveAppApplication.budgetRepository.allBudgets.first().toTypedArray()
        }
    }

    // Bindings
    @Bindable
    fun getAmount(): String {
        return _amount
    }

    fun setAmount(value: String) {
        if (value == _amount)
            return

        _amount = value.replace(',', '.')
        notifyPropertyChanged(BR.amount)
        onAmountChanged.invoke()
    }

    @Bindable
    fun getCurrency(): Currencies {
        return _currency
    }

    fun setCurrency(value: Currencies) {
        if (value == _currency)
            return

        _currency = value
        notifyPropertyChanged(BR.currency)
    }

    @Bindable
    fun getDescription(): String {
        return _description
    }

    fun setDescription(value: String) {
        if (value == _description)
            return

        _description = value
        notifyPropertyChanged(BR.description)
        onDescriptionChanged.invoke()
    }

    @Bindable
    fun getDate(): LocalDate {
        return _date
    }

    fun setDate(value: LocalDate) {
        if (value == _date)
            return

        _date = value
        notifyPropertyChanged(BR.date)
    }

    @Bindable
    fun getTag(): Tag? {
        return _tag
    }

    fun setTag(value: Tag?) {
        if (value == _tag)
            return

        _tag = value
        notifyPropertyChanged(BR.tag)
    }

    @Bindable
    fun getBudget(): TaggedBudget? {
        return _budget
    }

    fun setBudget(value: TaggedBudget?) {
        if (value == _budget)
            return

        _budget = value
        _tag = tags.value?.first { t -> t.id == value?.tagId }
        notifyPropertyChanged(BR.budget)
        notifyPropertyChanged(BR.tag)
    }

    @Bindable
    fun getIsSubscription(): Boolean {
        return _isSubscription
    }

    fun setIsSubscription(value: Boolean) {
        if (value == _isSubscription)
            return

        _isSubscription = value
        notifyPropertyChanged(BR.isSubscription)
    }

    @Bindable
    fun getRenewalType(): RenewalType {
        return _renewalType
    }

    fun setRenewalType(value: RenewalType) {
        if (value == _renewalType)
            return

        _renewalType = value

        notifyPropertyChanged(BR.renewalType)
    }

    @Bindable
    fun getIsSubscriptionSwitchEnabled(): Boolean {
        return _isSubscriptionSwitchEnabled
    }

    fun setIsSubscriptionSwitchEnabled(value: Boolean) {
        if (value == _isSubscriptionSwitchEnabled)
            return

        _isSubscriptionSwitchEnabled = value

        notifyPropertyChanged(BR.isSubscriptionSwitchEnabled)
    }

    // Overrides
    override fun addOnPropertyChangedCallback(
        callback: Observable.OnPropertyChangedCallback
    ) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(
        callback: Observable.OnPropertyChangedCallback?
    ) {
        callbacks.remove(callback)
    }

    // Methods
    fun insert() = viewModelScope.launch {
        val amount = _amount.replace(",", ".").toDoubleOrNull()
        if (amount != null && amount > 0.0 && _description.isNotBlank() && _tag != null) {
            val newAmount = updateToDefaultCurrency(amount)
            var succeeded = true

            if (_isSubscription)
                insertSubscription(newAmount)
            else
                succeeded = tryInsertMovement(newAmount)

            if (succeeded) {
                val activity = saveAppApplication.getCurrentActivity() as MainActivity
                activity.goBack()
                Snackbar.make(
                    activity.findViewById(R.id.containerView),
                    getSaveSnackBarMessage(),
                    Snackbar.LENGTH_SHORT
                ).setAnchorView(activity.findViewById(R.id.bottomAppBar)).show()
            }
        } else {
            onAmountChanged.invoke()
            onDescriptionChanged.invoke()
            validateTag.invoke()
        }
    }

    fun amountOnFocusChange(v: View, hasFocus: Boolean) {
        if (!hasFocus) {
            val amount = _amount.replace(",", ".").toDoubleOrNull()
            setAmount(
                if (amount == null) ""
                else String.format("%.2f", amount)
            )
        }
    }

    private fun notifyPropertyChanged(fieldId: Int) {
        callbacks.notifyCallbacks(this, fieldId, null)
    }

    private fun updateToDefaultCurrency(amount: Double): Double {
        if (_currency.id == baseCurrency.id || CurrencyUtil.rates.size < currencies.value!!.size)
            return amount

        return amount * CurrencyUtil.rates[baseCurrency.id] / CurrencyUtil.rates[_currency.id]
    }

    private suspend fun tryInsertMovement(amount: Double): Boolean {
        val budgetId = _budget?.budgetId ?: 0
        val tagId = _budget?.tagId ?: _tag?.id ?: 0
        val movement = Movement(0, amount, _description, _date, tagId, budgetId)

        if (editingMovement != null && editingMovement!!.budgetId != 0)
            BudgetUtil.removeMovementFromBudget(editingMovement!!)

        if (budgetId != 0) {
            val result = BudgetUtil.tryAddMovementToBudget(movement, editingMovement != null)
            if (result != AddToBudgetResult.SUCCEEDED) {
                handleAddBudgetResult(result)
                return false
            }
        }

        if (editingMovement != null) {
            editingMovement!!.amount *= -1
            StatsUtil.addMovementToStat(saveAppApplication, editingMovement!!)

            movement.id = editingMovement!!.id
            movementRepository.update(movement)
        } else {
            movementRepository.insert(movement)
        }

        StatsUtil.addMovementToStat(saveAppApplication, movement)

        return true
    }

    private suspend fun insertSubscription(amount: Double) {
        val tagId = _budget?.tagId ?: _tag?.id ?: 0
        val subscription = Subscription(
            0,
            amount,
            _description,
            _renewalType,
            _date,
            editingSubscription?.lastPaid,
            editingSubscription?.nextRenewal ?: _date,
            tagId,
            _budget?.budgetId ?: 0
        )
        val movement = SubscriptionUtil.getMovementFromSub(
            subscription, saveAppApplication.resources.getString(
                R.string.payment_of
            )
        )

        if (editingSubscription != null) {
            subscription.id = editingSubscription!!.id
            subscriptionRepository.update(subscription)
        } else {
            subscriptionRepository.insert(subscription)
        }

        if (movement != null) {
            if (movement.budgetId != 0 && BudgetUtil.tryAddMovementToBudget(movement) != AddToBudgetResult.SUCCEEDED)
                movement.budgetId = 0

            movementRepository.insert(movement)
            StatsUtil.addMovementToStat(saveAppApplication, movement)
        }
    }

    private fun handleAddBudgetResult(result: AddToBudgetResult) {
        val activity = saveAppApplication.getCurrentActivity() as MainActivity
        val msg: Int = when (result) {
            AddToBudgetResult.NOT_EXISTS -> R.string.budget_not_exists_error
            AddToBudgetResult.BUDGET_EMPTY -> R.string.budget_empty_error
            else -> R.string.budget_date_error
        }

        Snackbar.make(activity.findViewById(R.id.containerView), msg, Snackbar.LENGTH_SHORT)
            .setAnchorView(activity.findViewById(R.id.bottomAppBar)).show()
    }

    private fun getSaveSnackBarMessage(): Int {
        return if (editingMovement != null)
            R.string.movement_updated
        else if (editingSubscription != null)
            R.string.subscription_updated
        else if (_isSubscription)
            R.string.subscription_created
        else
            R.string.movement_created
    }
}
