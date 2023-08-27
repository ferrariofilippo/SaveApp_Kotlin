package com.ferrariofilippo.saveapp.converters

import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.ferrariofilippo.saveapp.R
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

@BindingAdapter("dynamicTint")
fun View.setDynamicTint(value: Int) {
    value.let {
        (this as ImageView).setColorFilter(ContextCompat.getColor(context, value))
    }
}
