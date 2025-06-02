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
import com.mgsanlet.cheftube.ui.view.customviews.VideoUrlState.INITIAL
import com.mgsanlet.cheftube.ui.view.customviews.VideoUrlState.INVALID
import com.mgsanlet.cheftube.ui.view.customviews.VideoUrlState.LOADING
import com.mgsanlet.cheftube.ui.view.customviews.VideoUrlState.VALID

/**
 * Estados posibles para el cargador de videos.
 *
 * @property INITIAL Estado inicial, sin URL cargada
 * @property VALID URL de video válida cargada
 * @property INVALID URL de video inválida
 * @property LOADING Cargando o validando la URL
 */
enum class VideoUrlState {
    INITIAL,
    VALID,
    INVALID,
    LOADING
}

/**
 * Vista personalizada para cargar y previsualizar videos de YouTube.
 *
 * Permite ingresar una URL de YouTube y muestra una previsualización del video.
 * Soporta diferentes formatos de URL de YouTube y valida que la URL sea correcta.
 *
 * @property context Contexto de la aplicación
 * @property attrs Atributos XML personalizados
 * @property defStyleAttr Estilo por defecto
 */
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

    /** Estado actual del cargador de videos */
    private var _state: VideoUrlState = VideoUrlState.INITIAL
    
    /**
     * Obtiene el estado actual del cargador de videos.
     *
     * @return Estado actual del cargador
     */
    val state: VideoUrlState
        get() = _state

    init {
        setupWebView()
        setupListeners()
        binding.progressBar.setCustomStyle(context)
    }

    @SuppressLint("SetJavaScriptEnabled")
    /**
     * Configura el WebView para mostrar la previsualización del video.
     * Habilita JavaScript y establece un WebViewClient personalizado.
     */
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

    /**
     * Configura los listeners para los elementos de la interfaz de usuario.
     *
     * Establece un TextWatcher en el campo de texto para validar la URL
     * en tiempo real mientras el usuario escribe.
     */
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

    /**
     * Carga y valida una URL de video de YouTube.
     *
     * @param url URL del video de YouTube a cargar
     */
    fun loadVideoUrl(url: String) {
        binding.videoUrlEditText.error = null
        val embedUrl = formatEmbedVideoUrl(url)
        if (embedUrl != null) {
            binding.videoWebView.loadUrl(embedUrl)
        } else {
            setState(VideoUrlState.INVALID)
        }
    }

    /**
     * Actualiza el estado del cargador de videos y la interfaz de usuario.
     *
     * @param newState Nuevo estado a establecer
     */
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


    /**
     * Convierte una URL de YouTube a formato de URL de incrustación (embed).
     *
     * @param url URL del video de YouTube en cualquier formato
     * @return URL de incrustación si la URL es válida, null en caso contrario
     */
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

    /**
     * Establece el texto en el campo de URL y carga el video si el texto no está vacío.
     *
     * @param text Texto a establecer en el campo de URL
     */
    fun setText(text: String) {
        binding.videoUrlEditText.setText(text)
        if (text.isNotBlank()) {
            loadVideoUrl(text)
        } else {
            setState(VideoUrlState.INITIAL)
        }
    }

    /**
     * Obtiene el texto actual del campo de URL.
     *
     * @return Texto actual del campo de URL
     */
    fun getText(): String {
        return binding.videoUrlEditText.text.toString()
    }

    /**
     * Obtiene la URL de incrustación del video actual.
     *
     * @return URL de incrustación del video o null si no hay una URL válida
     */
    fun getEmbedVideoUrl(): String? = embedVideoUrl

    /**
     * Muestra un mensaje de error en el campo de URL.
     *
     * @param error Mensaje de error a mostrar (opcional)
     */
    fun setError(error: String = "") {
        binding.videoUrlEditText.error = error
    }
}