package com.mgsanlet.cheftube.ui.view.auth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentSignUpBinding
import com.mgsanlet.cheftube.ui.view.base.BaseFormFragment
import com.mgsanlet.cheftube.ui.viewmodel.auth.SignUpState
import com.mgsanlet.cheftube.ui.viewmodel.auth.SignUpViewModel
import com.mgsanlet.cheftube.ui.util.matches
import com.mgsanlet.cheftube.ui.util.setCustomStyle
import com.mgsanlet.cheftube.ui.util.showWithCustomStyle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Un fragmento responsable de manejar el proceso de registro de usuario.
 * Este fragmento permite al usuario registrarse proporcionando un nombre,
 * correo electrónico y contraseña.
 */
@AndroidEntryPoint
class SignUpFragment @Inject constructor() : BaseFormFragment<FragmentSignUpBinding>() {

    private val viewModel: SignUpViewModel by viewModels()

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSignUpBinding = FragmentSignUpBinding.inflate(inflater, container, false)

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
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }

                is SignUpState.Success -> {
                    showSuccessSnackBar()
                    showLoading(false)
                }
            }
        }
    }

    private fun showSuccessSnackBar() {
        val snackbar = Snackbar.make(
            binding.root, getString(R.string.account_created), Snackbar.LENGTH_INDEFINITE
        )
        snackbar.setAction(R.string.action_sign_in) {
            (activity as? AuthActivity)?.navToHomePage()
        }
        snackbar.showWithCustomStyle(requireContext())
    }

    override fun setUpListeners() {
        binding.saveButton.setOnClickListener { trySignUp() }
    }

    override fun setUpViewProperties() {
        binding.progressBar.setCustomStyle(requireContext())
    }

    override fun isValidViewInput(): Boolean {
        val requiredFields = listOf(
            binding.nameEditText,
            binding.emailEditText,
            binding.password1EditText,
            binding.password2EditText,
        )

        return !areFieldsEmpty(requiredFields) &&
                isValidEmailPattern(binding.emailEditText) &&
                isValidPasswordPattern(
                    binding.password1EditText
                ) && passwordsMatch()
    }

    private fun trySignUp() {
        if (!isValidViewInput()) return

        if (viewModel.newUsernameAlreadyExists(binding.nameEditText.text.toString())) {
            binding.nameEditText.error = getString(R.string.username_already)
            return
        }

        if (viewModel.newEmailAlreadyExists(binding.emailEditText.text.toString())) {
            binding.emailEditText.error = getString(R.string.email_already)
            return
        }

        viewModel.trySignUp(
            binding.nameEditText.text.toString(),
            binding.emailEditText.text.toString(),
            binding.password1EditText.text.toString()
        )
    }

    /**
     * Verifica que las contraseñas coincidan
     */
    private fun passwordsMatch(): Boolean {
        var isMatch = binding.password1EditText.matches(binding.password2EditText)
        if (isMatch) binding.password2EditText.error = getString(R.string.pwd_d_match)
        return isMatch
    }

    private fun clearFields() {
        binding.nameEditText.text.clear()
        binding.emailEditText.text.clear()
        binding.password1EditText.text.clear()
        binding.password2EditText.text.clear()
    }

    private fun cleanErrors() {
        binding.nameEditText.error = null
        binding.emailEditText.error = null
        binding.password1EditText.error = null
        binding.password2EditText.error = null
    }

    private fun showLoading(show: Boolean) {
        binding.saveButton.isEnabled = !show
        if (show) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}