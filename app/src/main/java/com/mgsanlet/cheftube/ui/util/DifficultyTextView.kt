package com.mgsanlet.cheftube.ui.util

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.setPadding
import com.mgsanlet.cheftube.R

class DifficultyTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): AppCompatTextView(context, attrs, defStyleAttr) {

    private var currentState = 0

    private val difficultyTexts: Array<String> = context.resources.getStringArray(R.array.difficulty)
    private val backgroundColors: List<Int> = listOf(
        context.getColor(R.color.dark_green),
        context.getColor(R.color.dark_orange),
        context.getColor(R.color.dark_red)
    )

    init {
        setDifficulty(currentState)
        setTextColor(context.getColor(R.color.white))
        setPadding(10)
        textSize = 18f
    }

    fun setDifficulty(state: Int) {
        if (state !in 0..2) return
        currentState = state
        text = difficultyTexts[state]
        setBackgroundColor(backgroundColors[state])

    }

    fun getDifficulty(): Int = currentState
}