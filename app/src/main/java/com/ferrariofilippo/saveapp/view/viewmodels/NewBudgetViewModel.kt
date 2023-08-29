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
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.entities.Budget
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.util.CurrencyUtil
import com.ferrariofilippo.saveapp.util.SettingsUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class NewBudgetViewModel(application: Application) : AndroidViewModel(application), Observable {
    private val saveAppApplication = application as SaveAppApplication

    private val budgetRepository = saveAppApplication.budgetRepository

    private val callbacks: PropertyChangeRegistry = PropertyChangeRegistry()

    val baseCurrency: Currencies =
        runBlocking { Currencies.from(SettingsUtil.getCurrency().first()) }

    private var _amount: String = ""
    private var _currency: Currencies = baseCurrency
    private var _name: String = ""
    private var _tag: Tag? = null
    private var _fromDate: LocalDate = LocalDate.now()
    private var _toDate: LocalDate = LocalDate.now().plusDays(1)

    val tags: MutableLiveData<Array<Tag>> = MutableLiveData<Array<Tag>>()

    val currencies: MutableLiveData<Array<Currencies>> =
        MutableLiveData<Array<Currencies>>(Currencies.values())

    var onAmountChanged: () -> Unit = { }
    var onNameChanged: () -> Unit = { }

    init {
        viewModelScope.launch {
            tags.value = saveAppApplication.tagRepository.allTags.first().toTypedArray()
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
    fun getName(): String {
        return _name
    }

    fun setName(value: String) {
        if (value == _name)
            return;

        _name = value
        notifyPropertyChanged(BR.description)
        onNameChanged.invoke()
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
    fun getFromDate(): LocalDate {
        return _fromDate
    }

    fun setFromDate(value: LocalDate) {
        if (value == _fromDate)
            return;

        _fromDate = value
        notifyPropertyChanged(BR.fromDate)
    }

    @Bindable
    fun getToDate(): LocalDate {
        return _toDate
    }

    fun setToDate(value: LocalDate) {
        if (value == _toDate)
            return;

        _toDate = value
        notifyPropertyChanged(BR.toDate)
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
        if ((amount != null) && (amount > 0.0) && _name.isNotBlank()) {
            val newAmount = updateToDefaultCurrency(amount)

            budgetRepository.insert(
                Budget(
                    0,
                    newAmount,
                    0.0,
                    _name,
                    _fromDate,
                    _toDate,
                    _tag?.id ?: 0
                )
            )

            (saveAppApplication.getCurrentActivity() as MainActivity).goBack()
        } else {
            onAmountChanged.invoke()
            onNameChanged.invoke()
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
}
