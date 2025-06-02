package com.mgsanlet.cheftube.ui.view.auth

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentSignUpBinding
import com.mgsanlet.cheftube.domain.util.error.UserError
import com.mgsanlet.cheftube.ui.util.afterTextChanged
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.util.hideKeyboard
import com.mgsanlet.cheftube.ui.util.matches
import com.mgsanlet.cheftube.ui.util.showWithCustomStyle
import com.mgsanlet.cheftube.ui.view.base.BaseFormFragment
import com.mgsanlet.cheftube.ui.view.dialogs.LoadingDialog
import com.mgsanlet.cheftube.ui.viewmodel.auth.SignUpState
import com.mgsanlet.cheftube.ui.viewmodel.auth.SignUpViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Fragmento que maneja el proceso de registro de nuevos usuarios en la aplicación.
 * Permite a los usuarios crear una cuenta proporcionando nombre, correo electrónico
 * y contraseña, con validaciones en tiempo real.
 *
 * Este fragmento implementa [BaseFormFragment] para el manejo de formularios y utiliza
 * [SignUpViewModel] para la lógica de negocio relacionada con el registro.
 */
@AndroidEntryPoint
class SignUpFragment @Inject constructor() : BaseFormFragment<FragmentSignUpBinding>() {

    /** ViewModel que maneja la lógica de registro de usuarios. */
    private val viewModel: SignUpViewModel by viewModels()

    /**
     * Infla el binding del layout del fragmento de registro.
     *
     * @param inflater LayoutInflater usado para inflar la vista
     * @param container Contenedor padre de la vista
     * @return Instancia del binding inflado
     */
    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSignUpBinding = FragmentSignUpBinding.inflate(inflater, container, false)

    /**
     * Configura los observadores para los cambios de estado de la UI.
     * Maneja los diferentes estados del proceso de registro:
     * - Initial: Estado inicial del formulario
     * - Loading: Muestra indicador de carga
     * - Success: Muestra mensaje de éxito
     * - Error: Muestra mensajes de error específicos para cada campo
     */
    override fun setUpObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SignUpState.Initial -> {
                    showLoading(false)
                    cleanErrors()
                    clearFields()
                }

                is SignUpState.Loading -> {
                    showLoading(true)
                    cleanErrors()
                }

                is SignUpState.Error -> {
                    showLoading(false)
                    val errorMessage = state.error.asMessage(requireContext())
                    when (state.error) {

                        is UserError.InvalidEmailPattern -> binding.emailEditText.error =
                            errorMessage

                        is UserError.InvalidPasswordPattern -> {
                            binding.password1EditText.error = errorMessage
                            binding.password2EditText.error = errorMessage
                        }

                        is UserError.PasswordTooShort -> {
                            binding.password1EditText.error = errorMessage
                            binding.password2EditText.error = errorMessage
                        }

                        is UserError.InvalidUsernamePattern -> binding.nameEditText.error =
                            errorMessage

                        is UserError.UsernameInUse -> binding.nameEditText.error = errorMessage

                        is UserError.EmailInUse -> binding.emailEditText.error = errorMessage

                        else -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                is SignUpState.Success -> {
                    showSuccessSnackBar()
                    showLoading(false)
                }
            }
        }
    }

    /**
     * Muestra un Snackbar de éxito cuando el registro es exitoso.
     * Incluye un botón para iniciar sesión.
     */
    private fun showSuccessSnackBar() {
        val snackbar = Snackbar.make(
            binding.root, getString(R.string.account_created), Snackbar.LENGTH_INDEFINITE
        )
        snackbar.setAction(R.string.action_sign_in) {
            (activity as? AuthActivity)?.navToHomePage()
        }
        snackbar.showWithCustomStyle(requireContext())
    }

    /**
     * Configura los listeners de los elementos de la interfaz de usuario.
     * - Botón de guardar: Valida y envía los datos del formulario
     * - Campos de texto: Limpian los errores al editar
     * - Botón de volver: Navega a la pantalla anterior
     */
    override fun setUpListeners() {
        binding.saveButton.setOnClickListener {
            view?.hideKeyboard()
            if (isValidViewInput()) {
                viewModel.trySignUp(
                    binding.nameEditText.text.toString(),
                    binding.emailEditText.text.toString(),
                    binding.password1EditText.text.toString()
                )
            }
        }
        binding.nameEditText.afterTextChanged { cleanErrors() }
        binding.emailEditText.afterTextChanged { cleanErrors() }
        binding.password1EditText.afterTextChanged { cleanErrors() }
        binding.password2EditText.afterTextChanged { cleanErrors() }

        binding.backbutton.setOnClickListener {
            (activity as? AuthActivity)?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    /**
     * Valida que los campos obligatorios del formulario no estén vacíos
     * y que las contraseñas coincidan.
     *
     * @return true si la validación es exitosa, false en caso contrario
     */
    override fun isValidViewInput(): Boolean {
        val requiredFields = listOf(
            binding.nameEditText,
            binding.emailEditText,
            binding.password1EditText,
            binding.password2EditText,
        )
        return !areFieldsEmpty(requiredFields) && passwordsMatch()
    }

    /**
     * Verifica que las contraseñas ingresadas coincidan.
     * Muestra un mensaje de error si no coinciden.
     *
     * @return true si las contraseñas coinciden, false en caso contrario
     */
    private fun passwordsMatch(): Boolean {
        val isMatch = binding.password1EditText.matches(binding.password2EditText)
        if (!isMatch) binding.password2EditText.error = getString(R.string.pwd_d_match)
        return isMatch
    }

    /**
     * Limpia todos los campos del formulario.
     * Se utiliza al reiniciar el formulario o después de un registro exitoso.
     */
    private fun clearFields() {
        binding.nameEditText.text.clear()
        binding.emailEditText.text.clear()
        binding.password1EditText.text.clear()
        binding.password2EditText.text.clear()
    }

    /**
     * Limpia los mensajes de error de todos los campos del formulario.
     * Se llama al editar cualquier campo o al reiniciar el formulario.
     */
    private fun cleanErrors() {
        binding.nameEditText.error = null
        binding.emailEditText.error = null
        binding.password1EditText.error = null
        binding.password2EditText.error = null
    }

    /**
     * Muestra u oculta el diálogo de carga durante el proceso de registro.
     *
     * @param show true para mostrar el diálogo de carga, false para ocultarlo
     */
    fun showLoading(show: Boolean) {
        if (show) {
            LoadingDialog.show(requireContext(), parentFragmentManager)
        } else {
            LoadingDialog.dismiss(parentFragmentManager)
        }
    }

    /**
     * Se llama cuando la vista del fragmento está a punto de ser destruida.
     * Asegura que el diálogo de carga se cierre para evitar memory leaks.
     */
    override fun onDestroyView() {
        LoadingDialog.dismiss(parentFragmentManager)
        super.onDestroyView()
    }
}