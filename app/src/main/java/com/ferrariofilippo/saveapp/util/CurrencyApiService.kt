package com.ferrariofilippo.saveapp.util

import android.app.Application
import android.util.Log
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.CurrencyApiResponse
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import kotlin.coroutines.coroutineContext

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

    var rates: Array<Double> = arrayOf()

    suspend fun init() {
        if (true) {
            try {
                val result = retrofitService.getRates()

                rates = Currencies.values().map{ result.rates[it.name] ?: 1.0 }.toTypedArray()

            } catch (e: Exception) {
                Log.e("API", e.message ?: "")
            }
        }
    }
}