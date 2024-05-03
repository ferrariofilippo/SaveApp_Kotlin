// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import android.content.Context
import android.content.res.Resources
import androidx.core.content.ContextCompat
import com.ferrariofilippo.saveapp.R

object ColorUtil {
    fun getColorOrDefault(ctx: Context, resId: Int): Int {
        return try {
            ContextCompat.getColor(ctx, resId)
        } catch (e: Resources.NotFoundException) {
            LogUtil.logException(e, javaClass.kotlin.simpleName ?: "", "getColorOrDefault")
            ContextCompat.getColor(ctx, R.color.emerald_500)
        }
    }
}
