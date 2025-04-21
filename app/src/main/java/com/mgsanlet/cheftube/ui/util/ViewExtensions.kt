package com.mgsanlet.cheftube.ui.util

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.EditText
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.mgsanlet.cheftube.R

@Suppress("DEPRECATION") // Las funciones obsoletas solo se usarán para versiones antiguas
fun View.hideSystemBars() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val windowInsetsController = this.windowInsetsController
        windowInsetsController?.hide(
            WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        windowInsetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
    } else {
        this.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }
}

fun ProgressBar.setCustomStyle(context: Context){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        this.indeterminateDrawable.colorFilter = BlendModeColorFilter(
            ContextCompat.getColor(context, R.color.primary_green), BlendMode.SRC_IN
        )
    } else {
        @Suppress("DEPRECATION") // Solo para versiones antiguas
        this.indeterminateDrawable.setColorFilter(
            ContextCompat.getColor(context, R.color.primary_green),
            PorterDuff.Mode.SRC_IN
        )
    }
}

fun EditText.isEmpty(): Boolean{
    return this.text.trim().isEmpty()
}

fun Snackbar.showWithCustomStyle(context: Context){
    this.setBackgroundTint(ContextCompat.getColor(context, R.color.dark_green))
    this.setTextColor(ContextCompat.getColor(context, R.color.white))
    this.setActionTextColor(ContextCompat.getColor(context, R.color.white))
    this.show()
}

fun EditText.matches(other: EditText): Boolean{
    return this.text.toString() == other.text.toString()
}