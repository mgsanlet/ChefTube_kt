package com.mgsanlet.cheftube.ui.view.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mgsanlet.cheftube.R

/**
 * Clase base para los diálogos de gestión de cuenta.
 * Proporciona funcionalidad común para los diálogos de actualización de email, contraseña y eliminación de cuenta.
 */
abstract class BaseAccountDialog : DialogFragment() {
    
    protected lateinit var dialogView: View
    private var loadingDialog: LoadingDialog? = null
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
        dialogView = createDialogView()
        
        builder.setView(dialogView)
        
        val dialog = builder.create()
        setupListeners()
        return dialog
    }
    
    abstract fun createDialogView(): View
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }
    
    abstract fun setupListeners()

    protected open fun showLoading(show: Boolean) {
        if (show) {
            LoadingDialog.show(requireContext(), parentFragmentManager)
        } else {
            LoadingDialog.dismiss(parentFragmentManager)
        }
        dialogView.isEnabled = !show
    }
    
    override fun onDestroyView() {
        // Asegurarse de que el diálogo de carga se cierre si el fragmento se destruye
        LoadingDialog.dismiss(parentFragmentManager)
        super.onDestroyView()
    }
}
