package com.mgsanlet.cheftube.ui.view.auth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.mgsanlet.cheftube.ChefTubeApplication
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentLoginBinding
import com.mgsanlet.cheftube.ui.view.base.BaseFormFragment
import com.mgsanlet.cheftube.ui.viewmodel.auth.LoginState
import com.mgsanlet.cheftube.ui.viewmodel.auth.LoginViewModel
import com.mgsanlet.cheftube.ui.viewmodel.auth.LoginViewModelFactory
import com.mgsanlet.cheftube.utils.FragmentNavigator

/**
 * Fragmento que maneja el proceso de inicio de sesión para la aplicación.
 * Permite a los usuarios ingresar sus credenciales (email y contraseña) y
 * los autentica en la aplicación.
 */
class LoginFragment : BaseFormFragment<FragmentLoginBinding, LoginViewModel>() {

    private val _viewModel: LoginViewModel by viewModels {
        val app by lazy { ChefTubeApplication.getInstance(requireContext()) }
        LoginViewModelFactory(app)
    }

    override fun defineViewModel(): LoginViewModel = _viewModel

    override fun onResume() {
        super.onResume()
        viewModel.resetState()
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLoginBinding =
        FragmentLoginBinding.inflate(inflater, container, false)

    override fun setUpObservers() {
        viewModel.loginState.observe(viewLifecycleOwner){ state ->
            when (state) {
                is LoginState.Initial ->{
                    binding.identityEditText.text.clear()
                    binding.passwordEditText.text.clear()
                    showLoading(false)
                    cleanErrors()
                }
                is LoginState.Loading ->{
                    showLoading(true)
                    cleanErrors()
                }
                is LoginState.Success ->{
                    showLoading(false)
                    (activity as? AuthActivity)?.navToHomePage()
                }
                is LoginState.Error ->{
                    showLoading(false)
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                is LoginState.AlreadyLogged ->{
                    (activity as? AuthActivity)?.navToHomePage()
                }
            }
        }
    }

    override fun setUpListeners() {
        binding.signInButton.setOnClickListener {
            if (isValidViewInput()){
                viewModel.tryLogin(
                    binding.identityEditText.text.toString(),
                    binding.passwordEditText.text.toString()
                )
            }

        }
        binding.signUpLink.setOnClickListener {
            cleanErrors()
            FragmentNavigator.loadFragment(
                null, this, SignUpFragment(), R.id.authFrContainer)
        }
    }

    override fun setUpViewProperties() {
        setUpProgressBar(binding.progressBar)
    }

    override fun isValidViewInput(): Boolean {
        val requiredFields = listOf(
            binding.identityEditText,
            binding.passwordEditText
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
        if(show){
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}