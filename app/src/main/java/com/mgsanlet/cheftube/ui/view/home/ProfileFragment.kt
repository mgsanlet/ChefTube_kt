package com.mgsanlet.cheftube.ui.view.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentProfileBinding
import com.mgsanlet.cheftube.domain.model.DomainUser as User
import com.mgsanlet.cheftube.domain.util.error.UserError
import com.mgsanlet.cheftube.ui.view.base.BaseFormFragment
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileState
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileViewModel
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.util.setCustomStyle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * ProfileFragment permite al usuario ver y modificar los detalles de su perfil,
 * incluyendo nombre de usuario, email y contraseña.
 */
@AndroidEntryPoint
class ProfileFragment @Inject constructor() : BaseFormFragment<FragmentProfileBinding>() {

    private val viewModel: ProfileViewModel by viewModels()

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentProfileBinding = FragmentProfileBinding.inflate(inflater, container, false)

    override fun setUpObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileState.Error -> {
                    val errorMessage = state.error.asMessage(requireContext())
                    showLoading(false)
                    when (state.error) {

                        is UserError.WrongPassword ->
                            binding.oldPasswordEditText.error = errorMessage

                        is UserError.EmailInUse -> binding.emailEditText.error = errorMessage

                        is UserError.UsernameInUse -> binding.nameEditText.error = errorMessage

                        is UserError.InvalidPasswordPattern -> {
                            binding.newPassword1EditText.error = errorMessage
                            binding.newPassword2EditText.error = errorMessage
                        }

                        is UserError.PasswordTooShort ->{
                            binding.newPassword1EditText.error = errorMessage
                            binding.newPassword2EditText.error = errorMessage
                        }

                        is UserError.InvalidEmailPattern ->
                            binding.emailEditText.error = errorMessage

                        else -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
                is ProfileState.Loading -> showLoading(true)
                is ProfileState.LoadSuccess -> {
                    showLoading(false)
                    showUserData(state.user)
                }
                is ProfileState.SaveSuccess -> {
                    showLoading(false)
                    Toast.makeText(context, getString(R.string.data_saved), Toast.LENGTH_SHORT)
                        .show()
                    clearPasswordFields()
                }
            }
        }
    }

    override fun setUpListeners() {
        binding.saveButton.setOnClickListener { tryUpdateProfile() }
    }

    override fun setUpViewProperties() {
        binding.progressBar.setCustomStyle(requireContext())
    }

    override fun isValidViewInput(): Boolean {
        val requiredFields = listOf(
            binding.nameEditText, binding.emailEditText, binding.oldPasswordEditText
        )

        return !areFieldsEmpty(requiredFields) && isValidNewPassword()
    }

    private fun tryUpdateProfile() {
        if (!isValidViewInput()) return

        // Obtener los nuevos datos o mantener los actuales
        val finalUsername = binding.nameEditText.text.toString()
        val finalEmail = binding.emailEditText.text.toString()
        val oldPassword = binding.oldPasswordEditText.text.toString()
        // Es posible que no se cambie la contraseña
        val newPassword = binding.newPassword1EditText.text.toString().ifEmpty { null }

        viewModel.updateUser(finalUsername, finalEmail, newPassword, oldPassword)
    }

    private fun isValidNewPassword(): Boolean {
        val newPassword1 = binding.newPassword1EditText.text.toString()
        val newPassword2 = binding.newPassword2EditText.text.toString()

        // Si no hay nueva contraseña, es válido
        if (newPassword1.isEmpty() && newPassword2.isEmpty()) {
            return true
        }

        // Si solo uno de los campos está vacío, no es válido
        if (newPassword1.isEmpty() || newPassword2.isEmpty()) {
            if (newPassword1.isEmpty()) binding.newPassword1EditText.error =
                getString(R.string.required)
            if (newPassword2.isEmpty()) binding.newPassword2EditText.error =
                getString(R.string.required)
            return false
        }

        // Verificar que las contraseñas coincidan
        if (newPassword1 != newPassword2) {
            binding.newPassword2EditText.error = getString(R.string.pwd_d_match)
            return false
        }

        return true
    }

    private fun clearPasswordFields() {
        binding.oldPasswordEditText.text.clear()
        binding.newPassword1EditText.text.clear()
        binding.newPassword2EditText.text.clear()
    }

    private fun showUserData(user: User) {
        binding.nameEditText.setText(user.username)
        binding.emailEditText.setText(user.email)
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}