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
import com.mgsanlet.cheftube.databinding.ViewRecipeImageLoaderBinding
import com.mgsanlet.cheftube.ui.util.loadUrl
import com.mgsanlet.cheftube.ui.view.customviews.ImageState.INITIAL
import com.mgsanlet.cheftube.ui.view.customviews.ImageState.VALID
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Estados posibles para el cargador de imágenes.
 *
 * @property INITIAL Estado inicial, sin imagen cargada
 * @property VALID Estado con una imagen válida cargada
 */
enum class ImageState {
    INITIAL,
    VALID
}

/**
 * Vista personalizada para cargar y mostrar imágenes de recetas.
 *
 * Permite seleccionar una imagen desde la galería del dispositivo y validar su selección.
 * También soporta la carga de imágenes desde URL.
 *
 * @property context Contexto de la aplicación
 * @property attrs Atributos XML personalizados
 * @property defStyleAttr Estilo por defecto
 */
class RecipeImageLoaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewRecipeImageLoaderBinding = ViewRecipeImageLoaderBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    /** Estado actual del cargador de imágenes */
    private var _state: ImageState = ImageState.INITIAL
    
    /**
     * Obtiene el estado actual del cargador de imágenes.
     *
     * @return Estado actual del cargador
     */
    val state: ImageState
        get() = _state

    private var newImage: ByteArray? = null

    private lateinit var imagePicker: ActivityResultLauncher<Intent>

    init {
        setupListeners()
    }

    /**
     * Configura los listeners para los elementos interactivos de la vista.
     *
     * Establece los listeners para el botón de cargar imagen y para hacer clic
     * directamente en la imagen mostrada.
     */
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

    /**
     * Establece el registro de resultados de actividad para manejar la selección de imágenes.
     *
     * @param registry Registro de resultados de actividad para manejar la respuesta del selector de imágenes
     */
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

    /**
     * Establece una nueva imagen como un arreglo de bytes.
     *
     * @param newImage Imagen como arreglo de bytes
     */
    fun setNewImage(newImage: ByteArray) {
        this.newImage = newImage
    }

    /**
     * Obtiene la imagen actual como un arreglo de bytes.
     *
     * @return La imagen actual como arreglo de bytes, o null si no hay imagen
     */
    fun getNewImage(): ByteArray? {
        return newImage
    }

    /**
     * Actualiza el estado a válido y ajusta la interfaz de usuario.
     *
     * Muestra la imagen cargada y oculta los elementos de carga.
     */
    private fun setValidState() {
        _state = ImageState.VALID
        binding.recipeImageView.visibility = VISIBLE
        binding.recipeImageFrame.visibility = VISIBLE
        binding.loadImageButton.visibility = GONE
        binding.clickHereTextView.visibility = GONE

        clearError()
    }

    /**
     * Valida que se haya seleccionado una imagen.
     *
     * @return true si hay una imagen válida, false en caso contrario
     */
    fun validateNotInitial(): Boolean {
        if (_state == ImageState.INITIAL) {
            setError()
        }
        return _state != ImageState.INITIAL
    }

    /**
     * Muestra un indicador de error en la interfaz de usuario.
     *
     * Se utiliza para indicar que se requiere seleccionar una imagen.
     */
    fun setError() {
        binding.validationImageView.visibility = VISIBLE
        binding.requiredTextView.visibility = VISIBLE
    }

    /**
     * Limpia cualquier indicador de error en la interfaz de usuario.
     */
    fun clearError() {
        binding.validationImageView.visibility = GONE
        binding.requiredTextView.visibility = GONE
    }

    /**
     * Carga una imagen desde una URL y la muestra en la vista.
     *
     * @param url URL de la imagen a cargar
     */
    fun loadUrl(url: String) {
        binding.recipeImageView.loadUrl(url, context)
        setValidState()
        clearError()
    }
}