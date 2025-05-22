package com.mgsanlet.cheftube.ui.view.customviews

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import com.mgsanlet.cheftube.databinding.RecipeImageLoaderViewBinding
import com.mgsanlet.cheftube.ui.util.loadUrl
import java.io.ByteArrayOutputStream
import java.io.InputStream

enum class ImageState {
    INITIAL,
    VALID
}

class RecipeImageLoaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: RecipeImageLoaderViewBinding = RecipeImageLoaderViewBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    private var _state: ImageState = ImageState.INITIAL
    val state: ImageState
        get() = _state

    private var newImage: ByteArray? = null

    private lateinit var imagePicker: ActivityResultLauncher<Intent>

    init {
        setupListeners()
    }

    private fun setupListeners() {
        binding.loadImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePicker.launch(intent)
        }
        binding.recipeImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePicker.launch(intent)
        }
    }

    fun setActivityResultRegistry(registry: ActivityResultRegistry) {
        imagePicker = registry.register(
            "imagePicker",
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    // Convertir la imagen a ByteArray
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()

                    // Guardar la imagen
                    setNewImage(byteArray)

                    // Actualizar la vista
                    binding.recipeImageView.setImageBitmap(bitmap)
                    setValidState()
                    clearError()
                }
            }
        }
    }

    fun setNewImage(newImage: ByteArray) {
        this.newImage = newImage
    }

    fun getNewImage(): ByteArray? {
        return newImage
    }

    private fun setValidState() {
        _state = ImageState.VALID
        binding.recipeImageView.visibility = VISIBLE
        binding.recipeImageFrame.visibility = VISIBLE
        binding.loadImageButton.visibility = GONE
        binding.clickHereTextView.visibility = GONE

        clearError()
    }

    fun validateNotInitial(): Boolean {
        if (_state == ImageState.INITIAL) {
            setError()
        }
        return _state != ImageState.INITIAL
    }

    fun setError() {
        binding.validationImageView.visibility = VISIBLE
        binding.requiredTextView.visibility = VISIBLE
    }

    fun clearError() {
        binding.validationImageView.visibility = GONE
        binding.requiredTextView.visibility = GONE
    }

    fun loadUrl(url: String) {
        binding.recipeImageView.loadUrl(url, context)
        setValidState()
        clearError()
    }
}