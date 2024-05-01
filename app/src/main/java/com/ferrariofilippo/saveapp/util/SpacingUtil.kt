// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import com.ferrariofilippo.saveapp.SaveAppApplication
import kotlinx.coroutines.launch

object SpacingUtil {
    private const val COMPACT_PADDING_TOP = 8f
    private const val NORMAL_PADDING_TOP = 12f
    private const val PADDING_HORIZONTAL = 16f

    private lateinit var _paddingNormal: Array<Int>
    private lateinit var _paddingCompact: Array<Int>

    lateinit var padding: Array<Int>

    fun init(app: SaveAppApplication) {
        val scale = app.resources.displayMetrics.density
        val dpHorizontal = (scale * PADDING_HORIZONTAL + 0.5f).toInt()
        _paddingNormal = arrayOf(dpHorizontal, (scale * NORMAL_PADDING_TOP + 0.5f).toInt())
        _paddingCompact = arrayOf(dpHorizontal, (scale * COMPACT_PADDING_TOP + 0.5f).toInt())
        padding = _paddingNormal

        app.applicationScope.launch {
            SettingsUtil.getUseCompactMode().collect {
                padding = if (it) _paddingCompact else _paddingNormal
            }
        }
    }
}
