@file:Suppress("DEPRECATION")

package com.mgsanlet.cheftube.ui.view.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import com.google.zxing.integration.android.IntentIntegrator
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentScannerBinding
import com.mgsanlet.cheftube.ui.view.base.BaseFragment
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

    private val viewModel: ScannerViewModel by viewModels ()

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentScannerBinding = FragmentScannerBinding.inflate(inflater, container, false)

    override fun setUpObservers() {
        viewModel.scannerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ScannerState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.resultTextView.visibility = View.GONE
                    binding.infoButton.isEnabled = false
                }

                is ScannerState.ProductFound -> {
                    binding.progressBar.visibility = View.GONE
                    binding.resultTextView.apply {
                        visibility = View.VISIBLE
                        text = "Nombre del producto"
                        setBackgroundResource(R.drawable.result_green_shapes)
                    }
                    binding.infoButton.isEnabled = true
                    binding.infoButton.setBackgroundColor("#FB9E27".toColorInt())
                }

                is ScannerState.ProductNotFound -> {
                    showBadResult(getString(R.string.product_not_found))
                }

                is ScannerState.NetworkError -> {
                    showBadResult(getString(R.string.network_error))
                }

                is ScannerState.Error -> {
                    showBadResult(getString(R.string.api_error, state.code))
                }
            }
        }
    }

    private fun showBadResult(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.resultTextView.apply {
            visibility = View.VISIBLE
            text = message
            setBackgroundResource(R.drawable.result_red_shapes)
        }
        binding.infoButton.isEnabled = false
    }

    override fun setUpListeners() {
        binding.scanButton.setOnClickListener { startBarcodeScan() }
        binding.infoButton.setOnClickListener { openProductPage() }
    }

    override fun setUpViewProperties() {
        binding.infoButton.isEnabled = false
        binding.infoButton.setBackgroundColor("#505050".toColorInt())
        binding.progressBar.visibility = View.GONE
        binding.resultTextView.visibility = View.GONE
        setUpProgressBar(binding.progressBar)
    }

    override fun onResume() {
        super.onResume()
        binding.progressBar.visibility = View.GONE
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
        result?.contents?.let { viewModel.setBarcode(it) }
    }

    /**
     * Abre la página del producto en el sitio web de Open Food Facts en un navegador
     */
    private fun openProductPage() {
        val browserIntent = Intent(Intent.ACTION_VIEW, viewModel.getProductUrl().toUri())
        startActivity(browserIntent)
    }
}