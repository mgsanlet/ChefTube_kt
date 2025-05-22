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
import com.mgsanlet.cheftube.ui.util.setCustomStyle
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

    private val viewModel: ScannerViewModel by viewModels()

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentScannerBinding = FragmentScannerBinding.inflate(inflater, container, false)

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
                        setBackgroundResource(R.drawable.result_green_shapes)
                    }
                    binding.infoButton.isEnabled = true
                    binding.infoButton.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.primary_green
                        )
                    )
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

    private fun showBadResult(message: String) {

        binding.resultTextView.apply {
            visibility = View.VISIBLE
            text = message
            this.setBackgroundResource(R.drawable.result_red_shapes)
        }
        binding.infoButton.isEnabled = false
        binding.infoButton.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.grey
            )
        )
    }

    override fun setUpListeners() {
        binding.scanButton.setOnClickListener { startBarcodeScan() }
        binding.infoButton.setOnClickListener { openProductPage() }
    }

    override fun setUpViewProperties() {
        binding.infoButton.isEnabled = false
        binding.infoButton.setBackgroundColor("#505050".toColorInt())
        binding.resultTextView.visibility = View.GONE
    }

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
    private fun openProductPage() {
        val browserIntent = Intent(Intent.ACTION_VIEW, viewModel.getProductUrl().toUri())
        startActivity(browserIntent)
    }
}