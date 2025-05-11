package com.mgsanlet.cheftube.ui.view.home

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.mgsanlet.cheftube.databinding.RecipeVideoLoaderViewBinding

class RecipeCommentsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: RecipeVideoLoaderViewBinding = RecipeVideoLoaderViewBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )
}