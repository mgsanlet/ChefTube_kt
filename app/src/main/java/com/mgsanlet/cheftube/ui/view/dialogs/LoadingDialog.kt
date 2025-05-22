package com.mgsanlet.cheftube.ui.view.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        val dialog = builder.create()
        
        // Configurar el diálogo para que use WRAP_CONTENT
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(
                150.dpToPx(context),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        
        return dialog
    }

    private fun createDialogView(): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_loading, null, false)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        progressBar.setCustomStyle(context)
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