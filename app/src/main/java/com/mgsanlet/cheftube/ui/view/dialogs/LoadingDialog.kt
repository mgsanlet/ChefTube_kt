package com.mgsanlet.cheftube.ui.view.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.ui.util.dpToPx
import com.mgsanlet.cheftube.ui.util.setCustomStyle

/**
 * Diálogo de carga que muestra un indicador de progreso circular.
 *
 * Este diálogo no es cancelable por el usuario (no responde a toques ni al botón atrás)
 * y se muestra sin título ni bordes, con un fondo transparente. Es útil para operaciones
 * asíncronas que requieren que el usuario espere.
 *
 * @property context Contexto de la aplicación
 */
class LoadingDialog(private val context: Context) : DialogFragment() {

    /** Vista raíz del diálogo */
    private lateinit var dialogView: View

    /**
     * Crea el diálogo con la configuración personalizada.
     *
     * Configura un diálogo no cancelable, sin título, con fondo transparente
     * y un tamaño fijo para el indicador de progreso.
     *
     * @param savedInstanceState Estado guardado de la instancia anterior
     * @return Diálogo configurado
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
        dialogView = createDialogView()

        builder.setView(dialogView)
            .setCancelable(false)

        val dialog = object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                // No hacer nada al presionar atrás
            }
        }
        
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(
                150.dpToPx(context),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            // Asegurarse de que el diálogo no se pueda descartar
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
        
        dialog.setContentView(dialogView)
        return dialog
    }

    /**
     * Crea la vista del diálogo de carga.
     *
     * Infla el layout del diálogo y configura el estilo del ProgressBar.
     *
     * @return Vista raíz del diálogo
     */
    private fun createDialogView(): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_loading, null, false)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        progressBar?.setCustomStyle(context)
        return view
    }

    companion object {
        /**
         * Etiqueta para identificar el diálogo en el FragmentManager.
         */
        const val TAG = "loading_dialog"

        /**
         * Muestra el diálogo de carga.
         *
         * @param context Contexto de la aplicación
         * @param supportFragmentManager Gestor de fragmentos donde se mostrará el diálogo
         */
        fun show(context: Context, supportFragmentManager: FragmentManager) {
            val dialog = LoadingDialog(context)
            dialog.show(supportFragmentManager, TAG)
        }

        /**
         * Cierra el diálogo de carga si está visible.
         *
         * @param supportFragmentManager Gestor de fragmentos donde se mostró el diálogo
         */
        fun dismiss(supportFragmentManager: FragmentManager) {
            try {
                val dialog = supportFragmentManager.findFragmentByTag(TAG) as? LoadingDialog
                if (dialog != null && !supportFragmentManager.isStateSaved && !supportFragmentManager.isDestroyed) {
                    dialog.dismissAllowingStateLoss()
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }
}