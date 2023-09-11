package com.ferrariofilippo.saveapp.util

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.data.repository.BudgetRepository
import com.ferrariofilippo.saveapp.data.repository.MovementRepository
import com.ferrariofilippo.saveapp.data.repository.SubscriptionRepository
import com.ferrariofilippo.saveapp.model.CurrencyApiResponse
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.time.LocalDate

private const val BASE_URL = "https://api.frankfurter.app"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface CurrencyApiServer {
    @GET("latest")
    suspend fun getRates(): CurrencyApiResponse
}

object CurrencyUtil {
    private val retrofitService: CurrencyApiServer by lazy {
        retrofit.create(CurrencyApiServer::class.java)
    }

    private lateinit var movementRepository: MovementRepository
    private lateinit var subscriptionRepository: SubscriptionRepository
    private lateinit var budgetRepository: BudgetRepository

    private lateinit var ratesStore: DataStore<Preferences>

    private var keys: Array<Preferences.Key<Double>> = arrayOf()

    private var DATE_KEY = stringPreferencesKey("last_update")

    var rates: Array<Double> = arrayOf()

    fun setStore(application: SaveAppApplication) {
        ratesStore = application.ratesStore
        movementRepository = application.movementRepository
        subscriptionRepository = application.subscriptionRepository
        budgetRepository = application.budgetRepository
    }

    suspend fun init() {
        keys = Currencies.values().map {
            doublePreferencesKey(it.name)
        }.toTypedArray()

        val oldDate = getDate()

        if (oldDate == null || oldDate.isBefore(LocalDate.now())) {
            try {
                val result = retrofitService.getRates()
                rates = Currencies.values().map { result.rates[it.name] ?: 1.0 }.toTypedArray()

                setDate(result.date)
                setRates(rates)
            } catch (e: Exception) {
                Log.e("API", e.message ?: "")
                rates = getRates()
            }
        } else {
            rates = getRates()
        }
    }

    suspend fun updateAllToNewCurrency(context: Context, newCurrency: Currencies) {
        val oldCurrency = SettingsUtil.getCurrency().first()
        if (newCurrency.id != oldCurrency) {
            val rate = rates[newCurrency.id] / rates[oldCurrency]
            SettingsUtil.setCurrency(newCurrency)

            movementRepository.getAll().forEach {
                it.amount *= rate
                movementRepository.update(it)
            }
            subscriptionRepository.getAll().forEach {
                it.amount *= rate
                subscriptionRepository.update(it)
            }
            budgetRepository.getAll().forEach {
                it.max *= rate
                it.used *= rate
                budgetRepository.update(it)
            }

            StatsUtil.applyRateToAll(context, rate)
        }
    }

    private suspend fun setRates(rates: Array<Double>) {
        for (i: Int in rates.indices) {
            ratesStore.edit { pref ->
                pref[keys[i]] = rates[i]
            }
        }
    }

    private suspend fun getRates(): Array<Double> {
        return keys.map {
            ratesStore.data.map { preferences ->
                preferences[it] ?: 0.0
            }.first()
        }.toTypedArray()
    }

    private suspend fun setDate(date: String) {
        ratesStore.edit { pref ->
            pref[DATE_KEY] = date
        }
    }

    private suspend fun getDate(): LocalDate? {
        val dateStr = ratesStore.data.map { preferences ->
            preferences[DATE_KEY]
        }.first()

        return if (dateStr == null) null else LocalDate.parse(dateStr)
    }
}
