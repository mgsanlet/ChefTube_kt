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
import com.mgsanlet.cheftube.ui.util.setCustomStyle
import com.mgsanlet.cheftube.ui.view.base.BaseFormFragment
import com.mgsanlet.cheftube.ui.view.dialogs.LoadingDialog
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

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentLoginBinding = FragmentLoginBinding.inflate(inflater, container, false)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.tryAutoLogin()

    }

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

    override fun isValidViewInput(): Boolean {
        val requiredFields = listOf(
            binding.emailEditText, binding.passwordEditText
        )

        return !areFieldsEmpty(requiredFields)
    }

    /**
     * Limpia los mensajes de error de los campos de entrada
     */
    private fun cleanErrors() {
        binding.emailEditText.error = null
        binding.passwordEditText.error = null
    }

    private fun showLoading(show: Boolean) {
        binding.signInButton.isEnabled = !show
        if (show) {
            LoadingDialog.show(requireContext(), this.parentFragmentManager)
        } else {
            LoadingDialog.dismiss(this.parentFragmentManager)
        }
    }
}