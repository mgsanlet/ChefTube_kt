package com.mgsanlet.cheftube.utils

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.os.Build
import android.widget.EditText
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.mgsanlet.cheftube.R

fun ProgressBar.setCustomStyle(context: Context){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        this.indeterminateDrawable.colorFilter = BlendModeColorFilter(
            ContextCompat.getColor(context, R.color.primary_green), BlendMode.SRC_IN
        )
    } else {
        @Suppress("DEPRECATION") // Solo para versiones antiguas
        this.indeterminateDrawable.setColorFilter(
            ContextCompat.getColor(context, R.color.primary_green),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }
}

fun EditText.isEmpty(): Boolean{
    return this.text.trim().isEmpty()
}

fun Snackbar.showWithCustomStyle(context: Context){
    this.setBackgroundTint(ContextCompat.getColor(context, R.color.dark_green))
    this.setActionTextColor(ContextCompat.getColor(context, R.color.white))
    this.show()
}

fun EditText.matches(other: EditText): Boolean{
    return this.text.toString() == other.text.toString()
}