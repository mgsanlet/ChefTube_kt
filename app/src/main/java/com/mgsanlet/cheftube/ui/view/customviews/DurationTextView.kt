package com.mgsanlet.cheftube.ui.view.customviews

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.setPadding
import com.mgsanlet.cheftube.R

class DurationTextView  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): AppCompatTextView(context, attrs, defStyleAttr) {

    private var currentMinutes = 0
    private var currentHours = 0

    init {
        setDuration(currentMinutes)
        setTextColor(context.getColor(R.color.white))
        setPadding(10)
        textSize = 18f
    }

    fun setDuration(minutes: Int) {
        currentHours = minutes / 60
        currentMinutes = minutes % 60
        text = if (currentHours > 0){
            if (currentMinutes > 0){
                context.getString(R.string.duration_hours_minutes, currentHours, currentMinutes )
            }else{
                context.getString(R.string.duration_hours, currentHours )
            }
        }else{
            context.getString(R.string.duration_minutes, currentMinutes)
        }
    }
}