package com.mgsanlet.cheftube.ui.view.dialogs

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.domain.util.error.UserError
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.util.hideKeyboard
import com.mgsanlet.cheftube.ui.view.home.EditProfileFragment
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileState
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileViewModel

/**
 * Tipo de alias para la función de callback que se ejecuta cuando la contraseña se actualiza exitosamente.
 */
typealias OnPasswordUpdatedListener = () -> Unit

/**
 * Diálogo para actualizar la contraseña del usuario actual.
 *
 * Permite al usuario cambiar su contraseña actual por una nueva, validando que:
 * - La contraseña actual sea correcta
 * - La nueva contraseña cumpla con los requisitos de seguridad
 * - La confirmación de la nueva contraseña coincida
 *
 * Se comunica con [ProfileViewModel] para realizar la operación y notifica a través
 * de un callback cuando se completa exitosamente.
 *
 * @property fragment Fragmento padre que contiene este diálogo
 */
class UpdatePasswordDialog(val fragment: EditProfileFragment) : BaseAccountDialog() {

    /** ViewModel que maneja la lógica de perfil del usuario */
    private val viewModel: ProfileViewModel by activityViewModels()
    
    /** Listener que se ejecuta cuando la contraseña se actualiza exitosamente */
    private var onPasswordUpdatedListener: OnPasswordUpdatedListener? = null

    /** Campo para la contraseña actual */
    private lateinit var currentPasswordEditText: EditText
    
    /** Campo para la nueva contraseña */
    private lateinit var newPasswordEditText: EditText
    
    /** Campo para confirmar la nueva contraseña */
    private lateinit var confirmPasswordEditText: EditText
    
    /** Botón para guardar los cambios */
    private lateinit var saveButton: Button
    
    /** Botón para cancelar la operación */
    private lateinit var cancelButton: Button

    /**
     * Establece el listener que se ejecutará cuando la contraseña se actualice exitosamente.
     *
     * @param listener Función sin parámetros que se ejecutará al actualizar la contraseña
     */
    fun setOnPasswordUpdatedListener(listener: OnPasswordUpdatedListener) {
        onPasswordUpdatedListener = listener
    }

    /**
     * Crea y configura la vista del diálogo de actualización de contraseña.
     *
     * @return Vista raíz del diálogo
     */
    @SuppressLint("InflateParams")
    override fun createDialogView(): View {
        val inflater = LayoutInflater.from(fragment.requireContext())
        val view = inflater.inflate(R.layout.dialog_change_password, null)

        currentPasswordEditText = view.findViewById(R.id.currentPasswordEditText)
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText)
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
        saveButton = view.findViewById(R.id.saveButton)
        cancelButton = view.findViewById(R.id.cancelButton)

        return view
    }

    /**
     * Configura los listeners para los elementos interactivos del diálogo.
     * Incluye la validación de campos y el manejo de la lógica de actualización.
     */
    override fun setupListeners() {

        saveButton.setOnClickListener {
            val currentPassword = currentPasswordEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()


            var hasError = false

            // Validar contraseña actual
            if (currentPassword.isEmpty()) {
                currentPasswordEditText.error = getString(R.string.required)
                hasError = true
            } else {
                currentPasswordEditText.error = null
            }

            // Validar nueva contraseña
            if (newPassword.isBlank()) {
                newPasswordEditText.error = getString(R.string.required)
                hasError = true
            } else {
                newPasswordEditText.error = null
            }

            // Validar confirmación de contraseña
            if (confirmPassword.isBlank()) {
                confirmPasswordEditText.error = getString(R.string.required)
                hasError = true
            } else if (newPassword != confirmPassword) {
                confirmPasswordEditText.error = getString(R.string.pwd_d_match)
                hasError = true
            } else {
                confirmPasswordEditText.error = null
            }

            if (!hasError) {
                updatePassword(currentPassword, newPassword)
            }
        }

        cancelButton.setOnClickListener { dismiss() }
    }

    /**
     * Inicia el proceso de actualización de contraseña.
     *
     * @param currentPassword Contraseña actual del usuario
     * @param newPassword Nueva contraseña a establecer
     */
    private fun updatePassword(currentPassword: String, newPassword: String) {

        viewModel.updateUserPassword(
            currentPassword,
            newPassword
        )

        // Observar los cambios en el ViewModel
        viewModel.uiState.observe(fragment.viewLifecycleOwner) { state ->
            when (state) {

                is ProfileState.PasswordUpdated -> {
                    view?.hideKeyboard()
                    onPasswordUpdatedListener?.invoke()
                    dismiss()
                }

                is ProfileState.Error -> {
                    when (state.error) {
                        is UserError.WrongCredentials -> {
                            currentPasswordEditText.error = state.error.asMessage(fragment.requireContext())
                        }

                        is UserError.PasswordTooShort -> {
                            newPasswordEditText.error = state.error.asMessage(fragment.requireContext())
                        }

                        is UserError.InvalidPasswordPattern -> {
                            newPasswordEditText.error = state.error.asMessage(fragment.requireContext())
                        }

                        else -> {
                            currentPasswordEditText.error = state.error.asMessage(fragment.requireContext())
                        }
                    }
                }

                else -> { }
            }
        }
    }

    /**
     * Muestra u oculta el indicador de carga y actualiza el estado de los botones.
     *
     * @param show true para mostrar el indicador de carga, false para ocultarlo
     */
    override fun showLoading(show: Boolean) {
        super.showLoading(show)
        saveButton.isEnabled = !show
        cancelButton.isEnabled = !show
    }

    companion object {
        /**
         * Etiqueta para identificar el diálogo en los logs y transacciones de fragmentos.
         */
        const val TAG = "UpdatePasswordDialog"
    }
}
