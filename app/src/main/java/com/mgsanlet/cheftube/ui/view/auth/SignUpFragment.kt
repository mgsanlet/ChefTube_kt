package com.mgsanlet.cheftube.ui.view.auth

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
import com.mgsanlet.cheftube.databinding.FragmentSignUpBinding
import com.mgsanlet.cheftube.utils.FragmentNavigator

/**
 * Un fragmento responsable de manejar el proceso de registro de usuario.
 * Este fragmento permite al usuario registrarse proporcionando un nombre,
 * correo electrónico y contraseña.
 */
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { ChefTubeApplication.getInstance(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        binding.saveButton.setOnClickListener { tryRegister() }

        return binding.root
    }

    private fun tryRegister() {
        if (!isValidRegister) return

        app.userRepository.createUser(
            binding.nameEditText.text.toString(),
            binding.emailEditText.text.toString(),
            binding.password1EditText.text.toString()
        ).fold(
            onSuccess = { user ->
                app.setCurrentUser(user)
                FragmentNavigator.loadFragment(null, this, LoginFragment(), R.id.authFrContainer)
            },
            onFailure = { error ->
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    /**
     * Valida la entrada del usuario para el registro
     */
    private val isValidRegister: Boolean
        get() = !fieldsAreEmpty() &&
                isValidEmail &&
                isValidPwd &&
                passwordsMatch()

    /**
     * Verifica si alguno de los campos requeridos está vacío
     */
    private fun fieldsAreEmpty(): Boolean {
        val requiredMessage = getString(R.string.required)
        var isEmpty = false

        if (binding.nameEditText.text.toString().trim().isEmpty()) {
            binding.nameEditText.error = requiredMessage
            isEmpty = true
        }
        if (binding.emailEditText.text.toString().trim().isEmpty()) {
            binding.emailEditText.error = requiredMessage
            isEmpty = true
        }
        if (binding.password1EditText.text.toString().trim().isEmpty()) {
            binding.password1EditText.error = requiredMessage
            isEmpty = true
        }
        if (binding.password2EditText.text.toString().trim().isEmpty()) {
            binding.password2EditText.error = requiredMessage
            isEmpty = true
        }

        return isEmpty
    }

    /**
     * Valida que el correo electrónico tenga un formato válido
     */
    private val isValidEmail: Boolean
        get() {
            val email = binding.emailEditText.text.toString()
            return when {
                Patterns.EMAIL_ADDRESS.matcher(email).matches() -> true
                else -> {
                    binding.emailEditText.error = getString(R.string.invalid_email)
                    false
                }
            }
        }

    /**
     * Valida que la contraseña tenga la longitud mínima requerida
     */
    private val isValidPwd: Boolean
        get() {
            val password = binding.password1EditText.text.toString()
            return when {
                password.length < User.PASSWORD_MIN_LENGTH -> {
                    binding.password1EditText.error = getString(R.string.short_pwd)
                    false
                }
                else -> true
            }
        }

    /**
     * Verifica que las contraseñas coincidan
     */
    private fun passwordsMatch(): Boolean {
        return when {
            binding.password1EditText.text.toString() == binding.password2EditText.text.toString() -> true
            else -> {
                binding.password2EditText.error = getString(R.string.pwd_d_match)
                false
            }
        }
    }
}