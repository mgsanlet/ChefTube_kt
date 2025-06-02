package com.mgsanlet.cheftube.ui.view.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Clase base abstracta que proporciona funcionalidad común para los diálogos de gestión de cuenta.
 *
 * Proporciona manejo de estados de carga, gestión del teclado virtual y métodos auxiliares
 * comunes para la interacción con el usuario.
 *
 * Las clases hijas deben implementar [createDialogView] para definir la interfaz de usuario
 * y [setupListeners] para configurar los manejadores de eventos.
 */
abstract class BaseAccountDialog : DialogFragment() {
    
    /**
     * Vista raíz del diálogo. Debe ser inicializada por [createDialogView].
     */
    protected lateinit var dialogView: View
    
    /**
     * Crea el diálogo con la vista personalizada proporcionada por [createDialogView].
     *
     * @param savedInstanceState Estado guardado de la instancia anterior
     * @return Diálogo configurado con la vista personalizada
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
        dialogView = createDialogView()
        
        builder.setView(dialogView)
        
        val dialog = builder.create()
        setupListeners()
        return dialog
    }
    
    /**
     * Method abstracto que las clases hijas deben implementar para crear la vista del diálogo.
     *
     * @return View que se utilizará como contenido del diálogo
     */
    abstract fun createDialogView(): View
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }
    
    /**
     * Method abstracto que las clases hijas deben implementar para configurar
     * los listeners de los elementos de la interfaz de usuario.
     */
    abstract fun setupListeners()

    /**
     * Muestra u oculta el diálogo de carga y deshabilita la interacción con la vista principal.
     *
     * @param show true para mostrar el diálogo de carga, false para ocultarlo
     */
    protected open fun showLoading(show: Boolean) {
        if (show) {
            LoadingDialog.show(requireContext(), parentFragmentManager)
        } else {
            LoadingDialog.dismiss(parentFragmentManager)
        }
        dialogView.isEnabled = !show
    }
    
    /**
     * Limpia los recursos cuando la vista del fragmento es destruida.
     * Se asegura de que cualquier diálogo de carga abierto se cierre correctamente.
     */
    override fun onDestroyView() {
        LoadingDialog.dismiss(parentFragmentManager)
        super.onDestroyView()
    }
}
