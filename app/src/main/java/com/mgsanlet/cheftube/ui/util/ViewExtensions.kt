package com.mgsanlet.cheftube.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
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

fun Snackbar.showWithCustomStyle(context: Context){
    this.setBackgroundTint(ContextCompat.getColor(context, R.color.dark_green))
    this.setTextColor(ContextCompat.getColor(context, R.color.white))
    this.setActionTextColor(ContextCompat.getColor(context, R.color.white))
    this.show()
}

fun EditText.matches(other: EditText): Boolean{
    return this.text.toString() == other.text.toString()
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

fun ImageView.loadUrl(url: String, context: Context){
    Glide.with(context)
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .skipMemoryCache(true)
        .apply(RequestOptions.bitmapTransform(RoundedCorners(40)))
        .into(this)
}

fun ImageView.loadUrlToCircle(url: String, context: Context){
    Glide.with(context)
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .skipMemoryCache(true)
        .apply(RequestOptions.circleCropTransform())
        .into(this)
}

fun ImageView.loadBitmapToCircle(bitmap: Bitmap, context: Context) {
    Glide.with(context)
        .load(bitmap)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .skipMemoryCache(true)
        .apply(RequestOptions.circleCropTransform())
        .into(this)
}

/**
 * Oculta el teclado suavemente
 */
fun View.hideKeyboard() {
    context?.let { context ->
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(windowToken, 0)
    }
}

fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}

fun LinearLayout.removeLastChild(){
    this.removeView(this.getChildAt(this.childCount - 1))
}

fun LinearLayout.asStringList(): List<String> {
    val list = mutableListOf<String>()
    this.children.forEach {
        if (it is EditText) {
            list.add(it.text.toString().trim())
        }
    }
    return list
}