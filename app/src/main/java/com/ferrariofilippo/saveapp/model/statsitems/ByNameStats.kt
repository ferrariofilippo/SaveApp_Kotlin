// Copyright (c) 2025 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.model.statsitems

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ByNameStats {
    private val _sum: MutableLiveData<Double> = MutableLiveData(0.0)
    val sum: LiveData<Double> = _sum

    private val _avg: MutableLiveData<Double> = MutableLiveData(0.0)
    val avg: LiveData<Double> = _avg

    private val _count: MutableLiveData<Int> = MutableLiveData(0)
    val count: LiveData<Int> = _count

    private val _min: MutableLiveData<Double> = MutableLiveData(0.0)
    val min: LiveData<Double> = _min

    private val _max: MutableLiveData<Double> = MutableLiveData(0.0)
    val max: LiveData<Double> = _max

    fun setSum(s: Double) {
        _sum.value = s
    }

    fun setAvg(a: Double) {
        _avg.value = a
    }

    fun setCount(c: Int) {
        _count.value = c
    }

    fun setMin(m: Double) {
        _min.value = m
    }

    fun setMax(m: Double) {
        _max.value = m
    }
}
