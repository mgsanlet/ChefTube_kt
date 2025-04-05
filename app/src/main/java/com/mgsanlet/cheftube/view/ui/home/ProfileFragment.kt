package com.mgsanlet.cheftube.view.ui.home

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mgsanlet.cheftube.ChefTubeApplication
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.model.User
import com.mgsanlet.cheftube.databinding.FragmentProfileBinding

/**
 * ProfileFragment permite al usuario ver y modificar los detalles de su perfil,
 * incluyendo nombre de usuario, email y contraseña.
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { ChefTubeApplication.getInstance(requireContext()) }
    private val currentUser get() = app.getCurrentUser()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.saveButton.setOnClickListener { tryUpdateProfile() }
        loadUserCurrentData()
        return binding.root
    }

    private fun loadUserCurrentData() {
        currentUser?.let { user ->
            binding.nameEditText.setText(user.username)
            binding.emailEditText.setText(user.email)
        }
    }

    private fun tryUpdateProfile() {
        if (!isValidData) return

        currentUser?.let { user ->
            // Obtener los nuevos datos o mantener los actuales
            val newUsername = binding.nameEditText.text.toString().takeIf { it != user.username } ?: user.username
            val newEmail = binding.emailEditText.text.toString().takeIf { it != user.email } ?: user.email
            val oldPassword = binding.oldPasswordEditText.text.toString()
            
            // Verificar contraseña antigua
            if (!user.verifyPassword(oldPassword)) {
                binding.oldPasswordEditText.error = getString(R.string.wrong_pwd)
                return
            }

            // Determinar qué contraseña usar (la nueva o la antigua)
            val finalPassword = binding.newPassword1EditText.text.toString().ifEmpty {
                oldPassword // Si no hay nueva contraseña, mantenemos la antigua
            }

            // Crear usuario actualizado con los datos correspondientes
            val updatedUser = User.create(
                username = newUsername,
                email = newEmail,
                password = finalPassword
            ).copy(id = user.id) // Mantener el mismo ID

            app.userRepository.updateUser(updatedUser, oldPassword).fold(
                onSuccess = { newUser ->
                    app.setCurrentUser(newUser)
                    Toast.makeText(context, getString(R.string.data_saved), Toast.LENGTH_SHORT).show()
                    clearPasswordFields()
                },
                onFailure = { error ->
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            )
        }
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
            return if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailEditText.error = getString(R.string.invalid_email)
                false
            } else true
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
                if (newPassword1.isEmpty()) binding.newPassword1EditText.error = getString(R.string.required)
                if (newPassword2.isEmpty()) binding.newPassword2EditText.error = getString(R.string.required)
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