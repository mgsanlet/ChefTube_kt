package com.mgsanlet.cheftube.ui.view.home

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mgsanlet.cheftube.ChefTubeApplication
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.model.User
import com.mgsanlet.cheftube.databinding.FragmentProfileBinding
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileViewModel
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileViewModelFactory

/**
 * ProfileFragment permite al usuario ver y modificar los detalles de su perfil,
 * incluyendo nombre de usuario, email y contraseña.
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        val app by lazy { ChefTubeApplication.getInstance(requireContext()) }
        ProfileViewModelFactory(app)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        setupObservers()
        binding.saveButton.setOnClickListener { tryUpdateProfile() }
        return binding.root
    }

    private fun setupObservers() {
        viewModel.currentUser.observe(viewLifecycleOwner) {
            binding.nameEditText.setText(it.username)
            binding.emailEditText.setText(it.email)
        }
    }

    private fun tryUpdateProfile() {
        if (!isValidData) return

        // Obtener los nuevos datos o mantener los actuales
        val finalUsername = binding.nameEditText.text.toString()

        val finalEmail = binding.emailEditText.text.toString()

        val oldPassword = binding.oldPasswordEditText.text.toString()

        // Verificar contraseña antigua
        if (!viewModel.verifyPassword(oldPassword)) {
            binding.oldPasswordEditText.error = getString(R.string.wrong_pwd)
            return
        }
        // Determinar qué contraseña usar (la nueva o la antigua)
        val finalPassword = binding.newPassword1EditText.text.toString().ifEmpty {
            oldPassword // Si no hay nueva contraseña, mantenemos la antigua
        }

        // Crear usuario actualizado con los datos correspondientes
         // Mantener el mismo ID

        viewModel.updateUser(finalUsername, finalEmail,finalPassword, oldPassword).fold(
            onSuccess = {
                Toast.makeText(context, getString(R.string.data_saved), Toast.LENGTH_SHORT)
                    .show()
                clearPasswordFields()
            },
            onFailure = { error ->
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        )
    }


    private fun clearPasswordFields() {
        binding.oldPasswordEditText.text.clear()
        binding.newPassword1EditText.text.clear()
        binding.newPassword2EditText.text.clear()
    }

    private val isValidData: Boolean
        get() = !fieldsAreEmpty() &&
                isValidEmail &&
                isValidNewPassword

    private fun fieldsAreEmpty(): Boolean {
        var empty = false
        val requiredMessage = getString(R.string.required)

        if (binding.nameEditText.text.toString().trim().isEmpty()) {
            binding.nameEditText.error = requiredMessage
            empty = true
        }
        if (binding.emailEditText.text.toString().trim().isEmpty()) {
            binding.emailEditText.error = requiredMessage
            empty = true
        }
        if (binding.oldPasswordEditText.text.toString().trim().isEmpty()) {
            binding.oldPasswordEditText.error = requiredMessage
            empty = true
        }

        return empty
    }

    private val isValidEmail: Boolean
        get() {
            val email = binding.emailEditText.text.toString()
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailEditText.error = getString(R.string.invalid_email)
                return false
            }
            if (viewModel.newEmailAlreadyExists(email)){
                binding.emailEditText.error = getString(R.string.email_already)
                return false
            }
            return true
        }

    private val isValidNewPassword: Boolean
        get() {
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
            if (newPassword1.length < User.PASSWORD_MIN_LENGTH) {
                binding.newPassword1EditText.error = getString(R.string.short_pwd)
                return false
            }

            return true
        }
}