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
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.enums.RenewalType
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedBudget
import com.ferrariofilippo.saveapp.util.CurrencyUtil
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.util.SubscriptionUtil
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

    val tags: MutableLiveData<Array<Tag>> = MutableLiveData<Array<Tag>>()

    val budgets: MutableLiveData<Array<TaggedBudget>> = MutableLiveData<Array<TaggedBudget>>()

    val currencies: MutableLiveData<Array<Currencies>> =
        MutableLiveData<Array<Currencies>>(Currencies.values())

    val renewalTypes: MutableLiveData<Array<RenewalType>> =
        MutableLiveData<Array<RenewalType>>(RenewalType.values())

    var onAmountChanged: () -> Unit = { }
    var onDescriptionChanged: () -> Unit = { }

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
            return;

        _amount = value
        notifyPropertyChanged(BR.amount)
        onAmountChanged.invoke()
    }

    @Bindable
    fun getCurrency(): Currencies {
        return _currency
    }

    fun setCurrency(value: Currencies) {
        if (value == _currency)
            return;

        _currency = value
        notifyPropertyChanged(BR.currency)
    }

    @Bindable
    fun getDescription(): String {
        return _description
    }

    fun setDescription(value: String) {
        if (value == _description)
            return;

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
            return;

        _date = value
        notifyPropertyChanged(BR.date)
    }

    @Bindable
    fun getTag(): Tag? {
        return _tag
    }

    fun setTag(value: Tag?) {
        if (value == _tag)
            return;

        _tag = value
        notifyPropertyChanged(BR.tag)
    }

    @Bindable
    fun getBudget(): TaggedBudget? {
        return _budget
    }

    fun setBudget(value: TaggedBudget?) {
        if (value == _budget)
            return;

        _budget = value
        notifyPropertyChanged(BR.budget)
    }

    @Bindable
    fun getIsSubscription(): Boolean {
        return _isSubscription
    }

    fun setIsSubscription(value: Boolean) {
        if (value == _isSubscription)
            return;

        _isSubscription = value
        notifyPropertyChanged(BR.isSubscription)
    }

    @Bindable
    fun getRenewalType(): RenewalType {
        return _renewalType
    }

    fun setRenewalType(value: RenewalType) {
        if (value == _renewalType)
            return;

        _renewalType = value

        notifyPropertyChanged(BR.renewalType)
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
        if (amount != null && amount > 0.0 && _description.isNotBlank()) {
            val newAmount = updateToDefaultCurrency(amount)

            if (_isSubscription)
                insertSubscription(newAmount)
            else
                insertMovement(newAmount)

            (saveAppApplication.getCurrentActivity() as MainActivity).goBack()
        } else {
            onAmountChanged.invoke()
            onDescriptionChanged.invoke()
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
            return amount;

        return amount * CurrencyUtil.rates[baseCurrency.id] / CurrencyUtil.rates[_currency.id]
    }

    private suspend fun insertMovement(amount: Double) {
        movementRepository.insert(
            Movement(
                0,
                amount,
                _description,
                _date,
                _tag?.id ?: 0,
                _budget?.budgetId ?: 0
            )
        )
    }

    private suspend fun insertSubscription(amount: Double) {
        val subscription = Subscription(
            0,
            amount,
            _description,
            _renewalType,
            _date,
            null,
            _date,
            _tag?.id ?: 0,
            _budget?.budgetId ?: 0
        )
        val movement = SubscriptionUtil.getMovementFromSub(
            subscription, saveAppApplication.resources.getString(
                R.string.payment_of
            )
        )

        subscriptionRepository.insert(subscription)

        if (movement != null)
            movementRepository.insert(movement)
    }
}
