package com.mgsanlet.cheftube.ui.view.auth

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
import com.mgsanlet.cheftube.ui.util.setCustomStyle
import com.mgsanlet.cheftube.ui.view.base.BaseFormFragment
import com.mgsanlet.cheftube.ui.viewmodel.auth.LoginState
import com.mgsanlet.cheftube.ui.viewmodel.auth.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Fragmento que maneja el proceso de inicio de sesión para la aplicación.
 * Permite a los usuarios ingresar sus credenciales (email y contraseña) y
 * los autentica en la aplicación.
 */
@AndroidEntryPoint
class LoginFragment @Inject constructor() : BaseFormFragment<FragmentLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        viewModel.resetState()
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentLoginBinding = FragmentLoginBinding.inflate(inflater, container, false)

    override fun setUpObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginState.Initial -> {
                    binding.identityEditText.text.clear()
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
                        is UserError.UserNotFound -> binding.identityEditText.error = errorMessage

                        is UserError.WrongPassword -> binding.passwordEditText.error = errorMessage

                        else -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }

                }

                is LoginState.AlreadyLogged -> {
                    (activity as? AuthActivity)?.navToHomePage()
                }
            }
        }
    }

    override fun setUpListeners() {
        binding.signInButton.setOnClickListener {
            if (isValidViewInput()) {
                viewModel.tryLogin(
                    binding.identityEditText.text.toString(),
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

    override fun setUpViewProperties() {
        binding.progressBar.setCustomStyle(requireContext())
    }

    override fun isValidViewInput(): Boolean {
        val requiredFields = listOf(
            binding.identityEditText, binding.passwordEditText
        )

        return !areFieldsEmpty(requiredFields)
    }

    /**
     * Limpia los mensajes de error de los campos de entrada
     */
    private fun cleanErrors() {
        binding.identityEditText.error = null
        binding.passwordEditText.error = null
    }

    private fun showLoading(show: Boolean) {
        binding.signInButton.isEnabled = !show
        if (show) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}