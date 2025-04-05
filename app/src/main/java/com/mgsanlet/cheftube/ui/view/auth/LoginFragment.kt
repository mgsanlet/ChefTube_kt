package com.mgsanlet.cheftube.ui.view.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mgsanlet.cheftube.ChefTubeApplication
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentLoginBinding
import com.mgsanlet.cheftube.ui.view.home.HomeActivity
import com.mgsanlet.cheftube.utils.FragmentNavigator

/**
 * Fragmento que maneja el proceso de inicio de sesión para la aplicación.
 * Permite a los usuarios ingresar sus credenciales (email y contraseña) y
 * los autentica en la aplicación.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { ChefTubeApplication.getInstance(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        // Listeners
        binding.signInButton.setOnClickListener { tryLogin() }
        binding.signUpLink.setOnClickListener {
            cleanErrors()
            FragmentNavigator.loadFragment(null, this, SignUpFragment(), R.id.authFrContainer)
        }

        return binding.root
    }

    /**
     * Intenta iniciar sesión validando las credenciales del usuario
     */
    private fun tryLogin() {
        if (fieldsAreEmpty()) return

        val email = binding.identityEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        
        Log.d(TAG, "Intentando login con email: $email")

        app.userRepository.loginUser(email, password).fold(
            onSuccess = { user ->
                Log.d(TAG, "Login exitoso para usuario: ${user.username}")
                app.setCurrentUser(user)
                navToHomePage()
            },
            onFailure = { error ->
                Log.e(TAG, "Error en login: ${error.message}")
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    /**
     * Verifica si los campos de inicio de sesión están vacíos.
     * @return True si algún campo está vacío, false de lo contrario.
     */
    private fun fieldsAreEmpty(): Boolean {
        return when {
            binding.identityEditText.text.toString().trim().isEmpty() -> {
                binding.identityEditText.error = getString(R.string.required)
                true
            }
            binding.passwordEditText.text.toString().trim().isEmpty() -> {
                binding.passwordEditText.error = getString(R.string.required)
                true
            }
            else -> false
        }
    }

    /**
     * Limpia los mensajes de error de los campos de entrada
     */
    private fun cleanErrors() {
        binding.identityEditText.error = null
        binding.passwordEditText.error = null
    }

    /**
     * Navega a la página de inicio (HomeActivity)
     */
    private fun navToHomePage() {
        val mainActIntent = Intent(activity, HomeActivity::class.java)
        startActivity(mainActIntent)
        activity?.finish()
    }

    companion object {
        private const val TAG = "LoginFragment"
    }
}