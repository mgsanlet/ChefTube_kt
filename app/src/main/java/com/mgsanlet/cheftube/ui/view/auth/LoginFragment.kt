package com.mgsanlet.cheftube.ui.view.auth

import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.transition.Visibility
import com.mgsanlet.cheftube.ChefTubeApplication
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentLoginBinding
import com.mgsanlet.cheftube.databinding.FragmentScannerBinding
import com.mgsanlet.cheftube.ui.view.base.BaseFormFragment
import com.mgsanlet.cheftube.ui.view.home.HomeActivity
import com.mgsanlet.cheftube.ui.viewmodel.auth.LoginState
import com.mgsanlet.cheftube.ui.viewmodel.auth.LoginViewModel
import com.mgsanlet.cheftube.ui.viewmodel.auth.LoginViewModelFactory
import com.mgsanlet.cheftube.ui.viewmodel.home.ScannerViewModel
import com.mgsanlet.cheftube.ui.viewmodel.home.ScannerViewModelFactory
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
                    binding.identityEditText.setText("")
                    binding.passwordEditText.setText("")
                    showLoading(false)
                    cleanErrors()
                }
                is LoginState.Loading ->{
                    showLoading(true)
                    cleanErrors()
                }
                is LoginState.Success ->{
                    showLoading(false)
                    navToHomePage()
                }
                is LoginState.Error ->{
                    showLoading(false)
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                is LoginState.AlreadyLogged ->{
                    navToHomePage()
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
        val color = "#46A467".toColorInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.progressBar.indeterminateDrawable.colorFilter =
                BlendModeColorFilter(color, BlendMode.SRC_IN)
        }
        else{
            @Suppress("DEPRECATION") // Solo para versiones antiguas
            binding.progressBar.indeterminateDrawable.setColorFilter(
                color, android.graphics.PorterDuff.Mode.SRC_IN)
        }
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

    /**
     * Navega a la página de inicio (HomeActivity)
     */
    private fun navToHomePage() {
        val mainActIntent = Intent(activity, HomeActivity::class.java)
        startActivity(mainActIntent)
        activity?.finish()
    }
}