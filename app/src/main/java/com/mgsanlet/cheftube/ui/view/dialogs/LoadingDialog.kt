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

class LoadingDialog(private val context: Context) : DialogFragment() {

    private lateinit var dialogView: View

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

    private fun createDialogView(): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_loading, null, false)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        progressBar?.setCustomStyle(context)
        return view
    }

    companion object {
        const val TAG = "loading_dialog"

        fun show(context: Context, supportFragmentManager: FragmentManager) {
            val dialog = LoadingDialog(context)
            dialog.show(supportFragmentManager, TAG)
        }

        fun dismiss(supportFragmentManager: FragmentManager) {
            try {
                val dialog = supportFragmentManager.findFragmentByTag(TAG) as? LoadingDialog
                if (dialog != null && !supportFragmentManager.isStateSaved && !supportFragmentManager.isDestroyed) {
                    dialog.dismissAllowingStateLoss()
                }
            } catch (e: IllegalStateException) {
                // Capturar y manejar la excepción si el estado del FragmentManager no es válido
                e.printStackTrace()
            }
        }
    }
}