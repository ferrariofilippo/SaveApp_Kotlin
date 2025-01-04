// Copyright (c) 2025 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.model.enums

enum class SaveAppThemes(val id: Int) {
    DefaultTheme(0),
    DynamicColors(1);

    companion object {
        private val _map = entries.associateBy { it.id }
        fun from(id: Int): SaveAppThemes = _map[id] ?: DefaultTheme
    }
}
