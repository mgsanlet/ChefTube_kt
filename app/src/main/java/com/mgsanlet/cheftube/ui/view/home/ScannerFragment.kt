@file:Suppress("DEPRECATION")

package com.mgsanlet.cheftube.ui.view.home


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.zxing.integration.android.IntentIntegrator
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentScannerBinding
import java.util.Locale

/**
 * Un fragmento que proporciona funcionalidad para escanear códigos de barras de productos y mostrar
 * información nutricional utilizando la API de Open Food Facts.
 * @author MarioG
 */
@Suppress("DEPRECATION") // Actualización muy reciente de lirería ZXING
class ScannerFragment : Fragment() {

    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!

    private var currentBarcode: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)

        binding.scanButton.setOnClickListener { startBarcodeScan() }
        binding.infoButton.setOnClickListener { openProductPage() }
        return binding.root
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
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt(getString(R.string.scan_prompt))
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(false)
        integrator.initiateScan()
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
        //String barcode = "3017620422003";  ejemplo
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                currentBarcode = result.contents
                fetchProductData()
            }
        }
    }

    /**
     * Verifica si el producto existe en la API de Open Food Facts usando el código de barras
     */
    private fun fetchProductData() {
        val url = BASE_URL + currentBarcode
        val requestQueue = Volley.newRequestQueue(context)

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { openProductPage() }, // Si la respuesta de API es correcta, se abre la página web
            { error ->
                when (error.networkResponse?.statusCode) {
                    404 -> Toast.makeText(
                        context, getString(R.string.product_not_found), Toast.LENGTH_LONG).show()
                    null -> Toast.makeText(
                        context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
                    else -> Toast.makeText(
                        context, getString(R.string.api_error, error.networkResponse.statusCode), Toast.LENGTH_LONG).show()
                }
            }
        )

        requestQueue.add(stringRequest)
    }

    /**
     * Abre la página del producto en el sitio web de Open Food Facts en un navegador
     */
    private fun openProductPage() {
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val locale = prefs.getString(LANGUAGE_KEY, Locale.getDefault().language)
        // Formar URL web basada en la configuración regional y el código de barras
        val productUrl = "https://$locale.openfoodfacts.org/product/$currentBarcode"
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(productUrl))
        startActivity(browserIntent)
    }

    companion object {
        private const val BASE_URL = "https://world.openfoodfacts.org/api/v3/product/"

        private const val PREFS_NAME = "AppPrefs"
        private const val LANGUAGE_KEY = "language"
    }
}