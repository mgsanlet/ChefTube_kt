package com.mgsanlet.cheftube.ui.view.home

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentProfileBinding
import com.mgsanlet.cheftube.ui.view.base.BaseFormFragment
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * ProfileFragment permite al usuario ver y modificar los detalles de su perfil,
 * incluyendo nombre de usuario, email y contraseña.
 */
@AndroidEntryPoint
class ProfileFragment @Inject constructor() : BaseFormFragment<FragmentProfileBinding>() {

    private val viewModel: ProfileViewModel by viewModels ()

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentProfileBinding = FragmentProfileBinding.inflate(inflater, container, false)

    override fun setUpObservers() {
        viewModel.currentUser.observe(viewLifecycleOwner) {
            if (it == null) {
                return@observe
            }
            binding.nameEditText.setText(it.username)
            binding.emailEditText.setText(it.email)
        }
    }

    override fun setUpListeners() {
        binding.saveButton.setOnClickListener { tryUpdateProfile() }
        binding.keepLoggedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.alternateKeepLoggedIn(isChecked)
        }
    }

    override fun setUpViewProperties() {
        binding.keepLoggedCheckBox.isChecked = viewModel.isUserPersistent()
    }

    override fun isValidViewInput(): Boolean {
        val requiredFields = listOf(
            binding.nameEditText, binding.emailEditText, binding.oldPasswordEditText
        )

        return !areFieldsEmpty(requiredFields) && isValidEmailPattern(binding.emailEditText) && isValidNewPassword()
    }

    private fun tryUpdateProfile() {
        if (!isValidViewInput()) return

        // Obtener los nuevos datos o mantener los actuales
        val finalUsername = binding.nameEditText.text.toString()

        val finalEmail = binding.emailEditText.text.toString()

        val oldPassword = binding.oldPasswordEditText.text.toString()

        if (viewModel.newUsernameAlreadyExists(finalUsername)) {
            binding.nameEditText.error = getString(R.string.username_already)
            return
        }

        if (viewModel.newEmailAlreadyExists(finalEmail)) {
            binding.emailEditText.error = getString(R.string.email_already)
            return
        }

        // Verificar contraseña antigua
        if (!viewModel.verifyPassword(oldPassword)) {
            binding.oldPasswordEditText.error = getString(R.string.wrong_pwd)
            return
        }
        // Determinar qué contraseña usar (la nueva o la antigua)
        val finalPassword = binding.newPassword1EditText.text.toString().ifEmpty {
            oldPassword // Si no hay nueva contraseña, mantenemos la antigua
        }

        viewModel.updateUser(finalUsername, finalEmail, finalPassword, oldPassword)
            .fold(onSuccess = {
                Toast.makeText(context, getString(R.string.data_saved), Toast.LENGTH_SHORT).show()
                clearPasswordFields()
            }, onFailure = { error ->
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            })
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

        // Verificar longitud mínima
        if (!isValidPasswordPattern(binding.newPassword1EditText)) {
            return false
        }

        return true
    }

    private fun clearPasswordFields() {
        binding.oldPasswordEditText.text.clear()
        binding.newPassword1EditText.text.clear()
        binding.newPassword2EditText.text.clear()
    }
}