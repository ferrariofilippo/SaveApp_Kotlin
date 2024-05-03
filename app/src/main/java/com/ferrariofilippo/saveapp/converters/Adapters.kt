// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.converters

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.util.ColorUtil
import com.google.android.material.button.MaterialButton

@BindingAdapter("dynamicIcon")
fun View.setDynamicIcon(value: Boolean) {
    value.let {
        (this as MaterialButton).setIconResource(
            if (value) R.drawable.baseline_arrow_upward_24
            else R.drawable.baseline_arrow_downward_24
        )
    }
}

@BindingAdapter("collapsibleIcon")
fun View.setCollapsibleIcon(isCollapsed: Boolean) {
    isCollapsed.let {
        (this as MaterialButton).setIconResource(
            if (isCollapsed) R.drawable.baseline_keyboard_arrow_down_24
            else R.drawable.baseline_keyboard_arrow_up_24
        )
    }
}

@BindingAdapter("dynamicTint")
fun View.setDynamicTint(value: Int?) {
    value.let {
        (this as ImageView).setColorFilter(
            ColorUtil.getColorOrDefault(
                context,
                if (value == null || value == 0) R.color.emerald_500 else value
            )
        )
    }
}
