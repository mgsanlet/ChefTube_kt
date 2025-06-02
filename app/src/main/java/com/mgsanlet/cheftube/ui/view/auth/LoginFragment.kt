package com.mgsanlet.cheftube.ui.view.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentLoginBinding
import com.mgsanlet.cheftube.domain.util.error.UserError
import com.mgsanlet.cheftube.ui.util.FragmentNavigator
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.util.hideKeyboard
import com.mgsanlet.cheftube.ui.view.base.BaseFormFragment
import com.mgsanlet.cheftube.ui.view.dialogs.LoadingDialog
import com.mgsanlet.cheftube.ui.viewmodel.auth.LoginState
import com.mgsanlet.cheftube.ui.viewmodel.auth.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Fragmento que maneja el proceso de inicio de sesión de la aplicación.
 * Permite a los usuarios autenticarse ingresando su correo electrónico y contraseña,
 * y maneja la lógica de autenticación automática.
 *
 * Este fragmento implementa [BaseFormFragment] para el manejo de formularios y utiliza
 * [LoginViewModel] para la lógica de negocio relacionada con la autenticación.
 */
@AndroidEntryPoint
class LoginFragment @Inject constructor() : BaseFormFragment<FragmentLoginBinding>() {

    /** ViewModel que maneja la lógica de inicio de sesión. */
    private val viewModel: LoginViewModel by viewModels()

    /**
     * Infla el binding del layout del fragmento.
     *
     * @param inflater LayoutInflater usado para inflar la vista
     * @param container Contenedor padre de la vista
     * @return Instancia del binding inflado
     */
    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentLoginBinding = FragmentLoginBinding.inflate(inflater, container, false)

    /**
     * Configura los observadores para los cambios de estado de la UI.
     * Maneja los diferentes estados del proceso de inicio de sesión:
     * - Initial: Estado inicial del formulario
     * - Loading: Muestra indicador de carga
     * - Success: Navega a la pantalla principal
     * - Error: Muestra mensajes de error
     * - AlreadyLogged: Intenta autenticación automática
     */
    override fun setUpObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginState.Initial -> {
                    binding.emailEditText.text.clear()
                    binding.passwordEditText.text.clear()
                    showLoading(false)
                    cleanErrors()
                }

                is LoginState.Loading -> {
                    showLoading(true)
                    cleanErrors()
                }

                is LoginState.Success -> {
                    showLoading(false)
                    (activity as? AuthActivity)?.navToHomePage()
                }

                is LoginState.Error -> {
                    showLoading(false)
                    val errorMessage = state.error.asMessage(requireContext())
                    when (state.error) {

                        is UserError.WrongCredentials -> {
                            binding.emailEditText.error = errorMessage
                            binding.passwordEditText.error = errorMessage
                        }

                        else -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }

                }

                is LoginState.AlreadyLogged -> {
                    Toast.makeText(
                        context,
                        getString(R.string.recovering_last_session),
                        Toast.LENGTH_SHORT
                    ).show()
                    (activity as? AuthActivity)?.navToHomePage()
                }
            }
        }
    }

    /**
     * Se llama después de que la vista ha sido creada.
     * Intenta realizar un inicio de sesión automático si hay credenciales guardadas.
     *
     * @param view Vista creada
     * @param savedInstanceState Estado anterior del fragmento
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.tryAutoLogin()
    }

    /**
     * Configura los listeners de los elementos de la interfaz de usuario.
     * - Botón de inicio de sesión: Valida y envía las credenciales
     * - Enlace de registro: Navega al fragmento de registro
     */
    override fun setUpListeners() {
        binding.signInButton.setOnClickListener {
            view?.hideKeyboard()
            if (isValidViewInput()) {
                viewModel.tryLogin(
                    binding.emailEditText.text.toString(),
                    binding.passwordEditText.text.toString()
                )
            }

        }
        binding.signUpLink.setOnClickListener {
            cleanErrors()
            FragmentNavigator.loadFragment(
                null, this, SignUpFragment(), R.id.authFrContainer
            )
        }
    }

    /**
     * Valida que los campos obligatorios del formulario no estén vacíos.
     *
     * @return true si todos los campos requeridos tienen contenido, false en caso contrario
     */
    override fun isValidViewInput(): Boolean {
        val requiredFields = listOf(
            binding.emailEditText, binding.passwordEditText
        )
        return !areFieldsEmpty(requiredFields)
    }

    /**
     * Limpia los mensajes de error de los campos de entrada del formulario.
     * Se llama al cambiar entre pantallas o al reiniciar el formulario.
     */
    private fun cleanErrors() {
        binding.emailEditText.error = null
        binding.passwordEditText.error = null
    }

    /**
     * Muestra u oculta el diálogo de carga y deshabilita el botón de inicio de sesión.
     *
     * @param show true para mostrar el diálogo de carga, false para ocultarlo
     */
    private fun showLoading(show: Boolean) {
        binding.signInButton.isEnabled = !show
        if (show) {
            LoadingDialog.show(requireContext(), this.parentFragmentManager)
        } else {
            LoadingDialog.dismiss(this.parentFragmentManager)
        }
    }
}