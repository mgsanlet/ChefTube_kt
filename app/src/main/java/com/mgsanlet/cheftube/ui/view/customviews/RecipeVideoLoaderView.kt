package com.mgsanlet.cheftube.ui.view.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.constraintlayout.widget.ConstraintLayout
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.ViewRecipeVideoLoaderBinding
import com.mgsanlet.cheftube.ui.util.Constants.YOUTUBE_ID_REGEX
import com.mgsanlet.cheftube.ui.util.setCustomStyle

enum class VideoUrlState {
    INITIAL,
    VALID,
    INVALID,
    LOADING
}

class RecipeVideoLoaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var embedVideoUrl: String? = null

    private val binding: ViewRecipeVideoLoaderBinding = ViewRecipeVideoLoaderBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    private var _state: VideoUrlState = VideoUrlState.INITIAL
    val state: VideoUrlState
        get() = _state

    init {
        setupWebView()
        setupListeners()
        binding.progressBar.setCustomStyle(context)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.videoWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (url != null && url.isNotBlank()) {
                    setState(VideoUrlState.VALID)
                }
            }
        }
        binding.videoWebView.settings.javaScriptEnabled = true
    }

    private fun setupListeners() {
        binding.videoUrlEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // No action needed
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                setState(VideoUrlState.LOADING)
            }

            override fun afterTextChanged(s: Editable?) {
                val url = binding.videoUrlEditText.text.toString()
                if (url.isNotBlank()) {
                    loadVideoUrl(url)
                } else {
                    setState(VideoUrlState.INITIAL)
                }
            }

        })
    }

    fun loadVideoUrl(url: String) {
        binding.videoUrlEditText.error = null
        val embedUrl = formatEmbedVideoUrl(url)
        if (embedUrl != null) {
            binding.videoWebView.loadUrl(embedUrl)
        } else {
            setState(VideoUrlState.INVALID)
        }
    }

    private fun setState(newState: VideoUrlState) {
        _state = newState
        when (newState) {
            VideoUrlState.INITIAL -> {
                binding.videoFrame.visibility = GONE
                binding.validationImageView.visibility = GONE
                binding.progressBar.visibility = GONE
            }

            VideoUrlState.VALID -> {
                binding.videoFrame.visibility = VISIBLE
                binding.validationImageView.visibility = VISIBLE
                binding.validationImageView.setImageResource(R.drawable.ic_valid_24)
                binding.validationImageView.imageTintList =
                    context.getColorStateList(R.color.dark_green)
                binding.progressBar.visibility = GONE
            }

            VideoUrlState.INVALID -> {
                binding.videoFrame.visibility = GONE
                binding.validationImageView.visibility = VISIBLE
                binding.validationImageView.setImageResource(R.drawable.ic_invalid_24)
                binding.validationImageView.imageTintList =
                    context.getColorStateList(R.color.dark_red)
                binding.progressBar.visibility = GONE
            }

            VideoUrlState.LOADING -> {
                binding.progressBar.visibility = VISIBLE
                binding.videoFrame.visibility = GONE
                binding.validationImageView.visibility = GONE
            }
        }
    }


    fun formatEmbedVideoUrl(url: String): String? {
        val youtubeId: String? = when {
            url.contains("watch?v=") -> {
                url.substringAfter("watch?v=").substringBefore("&")
            }

            url.contains("youtu.be/") -> {
                url.substringAfter("youtu.be/").substringBefore("?")
            }

            url.contains("embed/") -> {
                url.substringAfter("embed/").substringBefore("?")
            }

            url.contains("v=") -> {
                url.substringAfter("v=").substringBefore("&")
            }

            else -> null
        }
        youtubeId?.let {
            if (YOUTUBE_ID_REGEX.toRegex().matches(it)) {
                embedVideoUrl = "https://www.youtube.com/embed/$it"
                return embedVideoUrl
            } else {
                return null
            }
        } ?: return null
    }

    fun setText(text: String) {
        binding.videoUrlEditText.setText(text)
        if (text.isNotBlank()) {
            loadVideoUrl(text)
        } else {
            setState(VideoUrlState.INITIAL)
        }
    }

    fun getText(): String {
        return binding.videoUrlEditText.text.toString()
    }

    fun getEmbedVideoUrl(): String? = embedVideoUrl

    fun setError(error: String = "") {
        binding.videoUrlEditText.error = error
    }
}