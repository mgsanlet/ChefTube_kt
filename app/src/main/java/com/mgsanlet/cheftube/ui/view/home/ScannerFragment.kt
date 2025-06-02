@file:Suppress("DEPRECATION")

package com.mgsanlet.cheftube.ui.view.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import com.google.zxing.integration.android.IntentIntegrator
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentScannerBinding
import com.mgsanlet.cheftube.domain.util.error.ProductError
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.view.base.BaseFragment
import com.mgsanlet.cheftube.ui.view.dialogs.LoadingDialog
import com.mgsanlet.cheftube.ui.viewmodel.home.ScannerState
import com.mgsanlet.cheftube.ui.viewmodel.home.ScannerViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Un fragmento que proporciona funcionalidad para escanear códigos de barras de productos y mostrar
 * información nutricional utilizando la API de Open Food Facts.
 * @author MarioG
 */
@AndroidEntryPoint
class ScannerFragment @Inject constructor() : BaseFragment<FragmentScannerBinding>() {

    /** ViewModel que maneja la lógica de negocio del escáner. */
    private val viewModel: ScannerViewModel by viewModels()

    /**
     * Infla y devuelve el binding para el layout del fragmento.
     *
     * @param inflater El LayoutInflater usado para inflar la vista
     * @param container El ViewGroup padre al que se adjuntará la vista
     * @return Instancia del binding inflado
     */
    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentScannerBinding = FragmentScannerBinding.inflate(inflater, container, false)

    /**
     * Configura los observadores para los estados del ViewModel.
     *
     * Maneja los diferentes estados de la UI:
     * - Loading: Muestra el indicador de carga
     * - ProductFound: Muestra el nombre del producto encontrado
     * - Error: Muestra mensajes de error apropiados
     */
    override fun setUpObservers() {
        viewModel.scannerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ScannerState.Loading -> {
                    LoadingDialog.show(requireContext(), parentFragmentManager)
                    binding.resultTextView.visibility = View.GONE
                    binding.infoButton.isEnabled = false
                }

                is ScannerState.ProductFound -> {
                    LoadingDialog.dismiss(parentFragmentManager)
                    binding.resultTextView.apply {
                        visibility = View.VISIBLE
                        text = viewModel.getLocalizedProductName()
                            .ifBlank { getString(R.string.unknown) }
                        setBackgroundResource(R.drawable.shape_radial_green_gradient)
                    }
                    binding.infoButton.apply {
                        isEnabled = true
                        backgroundTintList =
                            ContextCompat.getColorStateList(requireContext(), R.color.primary_green)
                    }
                }

                is ScannerState.Error -> {
                    LoadingDialog.dismiss(parentFragmentManager)
                    val errorMessage = state.error.asMessage(requireContext())
                    when (state.error) {
                        is ProductError.NotFound -> showBadResult(errorMessage)
                        else -> {
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            showBadResult(getString(R.string.error))
                        }
                    }
                }
            }
        }
    }

    /**
     * Muestra un mensaje de error en la interfaz de usuario.
     * 
     * Actualiza la vista para mostrar un mensaje de error y desactiva el botón de información.
     * 
     * @param message Mensaje de error a mostrar
     */
    private fun showBadResult(message: String) {

        binding.resultTextView.apply {
            visibility = View.VISIBLE
            text = message
            this.setBackgroundResource(R.drawable.shape_radial_red_gradient)
        }

        binding.infoButton.apply {
            isEnabled = false
            backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.dark_red)
        }
    }

    /**
     * Configura los listeners para los elementos de la interfaz de usuario.
     *
     * Configura los siguientes listeners:
     * - Botón de escanear: Inicia el escaneo de código de barras
     * - Botón de información: Abre la página del producto en el navegador
     */
    override fun setUpListeners() {
        binding.scanButton.setOnClickListener { startBarcodeScan() }
        binding.infoButton.setOnClickListener { openProductPage() }
    }

    /**
     * Configura las propiedades iniciales de la vista.
     *
     * Realiza las siguientes configuraciones iniciales:
     * - Desactiva el botón de información
     * - Establece el color de fondo del botón de información
     * - Oculta el TextView de resultados
     */
    override fun setUpViewProperties() {
        binding.infoButton.isEnabled = false
        binding.infoButton.setBackgroundColor("#505050".toColorInt())
        binding.resultTextView.visibility = View.GONE
    }

    /**
     * Se llama cuando el fragmento se hace visible para el usuario.
     * 
     * Asegura que cualquier diálogo de carga activo sea descartado
     * cuando el usuario vuelve a este fragmento.
     */
    override fun onResume() {
        super.onResume()
        LoadingDialog.dismiss(parentFragmentManager)
    }

    /**
     * Inicia el proceso de escaneo de códigos de barras utilizando la cámara del dispositivo.
     * Configura el escáner con la configuración predeterminada:
     * - Utiliza todos los formatos de código de barras compatibles
     * - Utiliza la cámara trasera
     * - Reproduce un sonido al escanear con éxito
     * - Desactiva el guardado de imágenes de códigos de barras
     */
    private fun startBarcodeScan() {
        IntentIntegrator.forSupportFragment(this)
            .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            .setPrompt(getString(R.string.scan_prompt)).setCameraId(0).setBeepEnabled(true)
            .setBarcodeImageEnabled(false).initiateScan()
    }

    /**
     * Maneja el resultado de la actividad de escaneo de códigos de barras
     *
     * @param requestCode El código de solicitud entero originalmente suministrado a
     * startActivityForResult()
     * @param resultCode El código de resultado entero devuelto por la actividad secundaria
     * @param data Un Intent que contiene los datos del resultado
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        result?.contents?.let { viewModel.searchBarcode(it) }
    }

    /**
     * Abre la página del producto en el sitio web de Open Food Facts en un navegador
     */
    /**
     * Abre la página del producto en el navegador web predeterminado del dispositivo.
     * 
     * Utiliza un Intent con acción ACTION_VIEW para abrir la URL del producto
     * en el navegador web predeterminado del dispositivo.
     */
    private fun openProductPage() {
        val browserIntent = Intent(Intent.ACTION_VIEW, viewModel.getProductUrl().toUri())
        startActivity(browserIntent)
    }
}