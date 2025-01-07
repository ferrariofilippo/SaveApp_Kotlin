// Copyright (c) 2025 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.model.enums.SaveAppThemes
import kotlinx.coroutines.flow.first

object ThemeManagerUtil {
    suspend fun setTheme(theme: SaveAppThemes) {
        SettingsUtil.setTheme(theme)
    }

    suspend fun getCurrentThemeResId(): Int {
        val currentTheme = SettingsUtil.getTheme().first()
        return when (currentTheme.id) {
            1 -> 0
            2 -> R.style.Theme_Dracula
            3 -> R.style.Theme_Nord
            else -> R.style.Theme_SaveApp
        }
    }
}
