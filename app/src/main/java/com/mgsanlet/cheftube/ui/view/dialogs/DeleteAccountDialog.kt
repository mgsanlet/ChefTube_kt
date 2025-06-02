package com.mgsanlet.cheftube.ui.view.dialogs

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.util.hideKeyboard
import com.mgsanlet.cheftube.ui.view.home.EditProfileFragment
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileState
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileViewModel

/**
 * Tipo de alias para la función de callback que se ejecuta cuando se elimina la cuenta exitosamente.
 */
typealias OnAccountDeletedListener = () -> Unit

/**
 * Diálogo para confirmar y procesar la eliminación de la cuenta de usuario.
 *
 * Este diálogo solicita la contraseña actual y una confirmación del usuario antes de proceder
 * con la eliminación de la cuenta. Se comunica con el [ProfileViewModel] para realizar
 * la operación y notifica a través de un callback cuando se completa exitosamente.
 *
 * @property fragment Fragmento padre que contiene este diálogo
 */
class DeleteAccountDialog(val fragment: EditProfileFragment) : BaseAccountDialog() {

    /** ViewModel que maneja la lógica de perfil del usuario */
    private val viewModel: ProfileViewModel by activityViewModels()
    
    /** Listener que se ejecuta cuando la cuenta se elimina exitosamente */
    private var onAccountDeletedListener: OnAccountDeletedListener? = null

    /** Campo de entrada para la contraseña actual */
    private lateinit var passwordEditText: EditText
    
    /** Checkbox de confirmación de eliminación */
    private lateinit var confirmationCheckbox: TextView
    
    /** Botón para confirmar la eliminación */
    private lateinit var deleteButton: Button
    
    /** Botón para cancelar la operación */
    private lateinit var cancelButton: Button
    
    /** Indica si el usuario ha confirmado la eliminación */
    private var isConfirmed = false

    /**
     * Establece el listener que se ejecutará cuando la cuenta se elimine exitosamente.
     *
     * @param listener Función sin parámetros que se ejecutará al eliminar la cuenta
     */
    fun setOnAccountDeletedListener(listener: OnAccountDeletedListener) {
        onAccountDeletedListener = listener
    }

    @SuppressLint("InflateParams")
    /**
     * Crea y configura la vista del diálogo de eliminación de cuenta.
     *
     * @return Vista raíz del diálogo
     */
    override fun createDialogView(): View {
        val inflater = LayoutInflater.from(fragment.requireContext())
        val view = inflater.inflate(R.layout.dialog_delete_account, null, false)

        passwordEditText = view.findViewById(R.id.currentPasswordEditText)
        confirmationCheckbox = view.findViewById(R.id.confirmationCheckbox)
        deleteButton = view.findViewById(R.id.deleteButton)
        cancelButton = view.findViewById(R.id.cancelButton)

        // Configurar el checkbox de confirmación
        confirmationCheckbox.setOnClickListener {
            isConfirmed = !isConfirmed
            updateConfirmationCheckbox()
        }

        return view
    }

    /**
     * Actualiza el estado visual del checkbox de confirmación.
     * Cambia entre los estados marcado y desmarcado.
     */
    private fun updateConfirmationCheckbox() {
        val drawableRes = if (isConfirmed) {
            R.drawable.ic_checkbox_checked_24
        } else {
            R.drawable.ic_checkbox_unchecked_24
        }

        val drawable = ResourcesCompat.getDrawable(resources, drawableRes, null)
        drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        confirmationCheckbox.setCompoundDrawables(drawable, null, null, null)
    }

    /**
     * Configura los listeners para los elementos interactivos del diálogo.
     * Incluye la validación de campos y el manejo de la lógica de eliminación.
     */
    override fun setupListeners() {

        deleteButton.setOnClickListener {
            val password = passwordEditText.text.toString().trim()

            if (password.isBlank()) {
                passwordEditText.error = getString(R.string.required)
                return@setOnClickListener
            }

            if (!isConfirmed) {
                Toast.makeText(fragment.requireContext(), R.string.please_confirm_deletion, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showConfirmDeleteDialog(password)
        }

        cancelButton.setOnClickListener { dismiss() }
    }

    /**
     * Muestra un diálogo de confirmación antes de proceder con la eliminación.
     *
     * @param password Contraseña actual del usuario para validar la operación
     */
    private fun showConfirmDeleteDialog(password: String) {
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle(R.string.delete_account)
            .setMessage(R.string.delete_account_confirmation_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteAccount(password)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * Inicia el proceso de eliminación de la cuenta.
     *
     * @param password Contraseña actual del usuario para autenticar la operación
     */
    private fun deleteAccount(password: String) {
        enableButtons(false)
        viewModel.deleteUserAccount(password)

        // Observar los cambios en el ViewModel
        viewModel.uiState.observe(fragment.viewLifecycleOwner) { state ->
            when (state) {
                is ProfileState.AccountDeleted -> {
                    enableButtons(true)
                    view?.hideKeyboard()
                    onAccountDeletedListener?.invoke()
                    dismiss()
                }

                is ProfileState.Error -> {
                    enableButtons(true)
                    passwordEditText.error = state.error.asMessage(fragment.requireContext())
                }

                else -> { /* No hacer nada para otros estados */
                }
            }
        }
    }

    /**
     * Habilita o deshabilita los botones del diálogo.
     *
     * @param enable true para habilitar los botones, false para deshabilitarlos
     */
    private fun enableButtons(enable: Boolean) {
        (dialog as? android.app.AlertDialog)?.apply {
            getButton(android.app.AlertDialog.BUTTON_POSITIVE).isEnabled = enable
            getButton(android.app.AlertDialog.BUTTON_NEGATIVE).isEnabled = enable
        }
    }

    companion object {
        /**
         * Etiqueta para identificar el diálogo en los logs y transacciones de fragmentos.
         */
        const val TAG = "DeleteAccountDialog"
    }
}
