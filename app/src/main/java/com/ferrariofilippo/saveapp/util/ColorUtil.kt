// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import android.content.Context
import android.content.res.Resources
import androidx.core.content.ContextCompat
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication

object ColorUtil {
    private const val CLASS_NAME = "ColorUtil"

    private var _colorToResIdMap: Map<Int, Int> = mapOf()

    val colors: Array<Int> = arrayOf(
        R.color.cyan_50,
        R.color.cyan_100,
        R.color.cyan_200,
        R.color.cyan_300,
        R.color.cyan_400,
        R.color.cyan_500,
        R.color.cyan_600,
        R.color.cyan_700,
        R.color.cyan_800,
        R.color.cyan_900,
        R.color.blue_50,
        R.color.blue_100,
        R.color.blue_200,
        R.color.blue_300,
        R.color.blue_400,
        R.color.blue_500,
        R.color.blue_600,
        R.color.blue_700,
        R.color.blue_800,
        R.color.blue_900,
        R.color.emerald_50,
        R.color.emerald_100,
        R.color.emerald_200,
        R.color.emerald_300,
        R.color.emerald_400,
        R.color.emerald_500,
        R.color.emerald_600,
        R.color.emerald_700,
        R.color.emerald_800,
        R.color.emerald_900,
        R.color.green_50,
        R.color.green_100,
        R.color.green_200,
        R.color.green_300,
        R.color.green_400,
        R.color.green_500,
        R.color.green_600,
        R.color.green_700,
        R.color.green_800,
        R.color.green_900,
        R.color.red_50,
        R.color.red_100,
        R.color.red_200,
        R.color.red_300,
        R.color.red_400,
        R.color.red_500,
        R.color.red_600,
        R.color.red_700,
        R.color.red_800,
        R.color.red_900,
        R.color.purple_50,
        R.color.purple_100,
        R.color.purple_200,
        R.color.purple_300,
        R.color.purple_400,
        R.color.purple_500,
        R.color.purple_600,
        R.color.purple_700,
        R.color.purple_800,
        R.color.purple_900
    )

    fun getColorOrDefault(ctx: Context, resId: Int): Int {
        return try {
            ContextCompat.getColor(ctx, resId)
        } catch (e: Resources.NotFoundException) {
            LogUtil.logException(e, CLASS_NAME, "getColorOrDefault")
            ContextCompat.getColor(ctx, R.color.emerald_500)
        }
    }

    fun getColorIdFromHex(hex: Int): Int {
        return _colorToResIdMap.getOrElse(hex) { R.color.emerald_500 }
    }

    fun initColors(app: SaveAppApplication) {
        val colorMap: MutableMap<Int, Int> = mutableMapOf()
        for (id in colors) {
            val color = try {
                app.getColor(id)
            } catch (e: Exception) {
                app.getColor(R.color.emerald_500)
            }
            colorMap[color] = id
        }
        _colorToResIdMap = colorMap
    }
}
