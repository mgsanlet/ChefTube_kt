package com.mgsanlet.cheftube.view.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mgsanlet.cheftube.ChefTubeApplication
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.view.ui.home.HomeActivity
import com.mgsanlet.cheftube.utils.FragmentNavigator

/**
 * Fragmento que maneja el proceso de inicio de sesión para la aplicación.
 * Permite a los usuarios ingresar sus credenciales (email y contraseña) y
 * los autentica en la aplicación.
 */
class LoginFragment : Fragment() {
    companion object {
        private const val TAG = "LoginFragment"
    }

    private lateinit var mIdentityEditText: EditText
    private lateinit var mPasswordEditText: EditText
    private lateinit var mLoginButton: Button
    private lateinit var mSignUpLink: TextView

    private val app by lazy { ChefTubeApplication.getInstance(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        mIdentityEditText = view.findViewById(R.id.loginIdentityField)
        mPasswordEditText = view.findViewById(R.id.loginPwdField)
        mLoginButton = view.findViewById(R.id.signInBtn)
        mSignUpLink = view.findViewById(R.id.loginSignUpLink)

        // Listeners
        mLoginButton.setOnClickListener { tryLogin() }
        mSignUpLink.setOnClickListener {
            cleanErrors()
            FragmentNavigator.loadFragment(null, this, SignUpFragment(), R.id.authFrContainer)
        }

        return view
    }

    /**
     * Intenta iniciar sesión validando las credenciales del usuario
     */
    private fun tryLogin() {
        if (fieldsAreEmpty()) return

        val email = mIdentityEditText.text.toString()
        val password = mPasswordEditText.text.toString()
        
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
            mIdentityEditText.text.toString().trim().isEmpty() -> {
                mIdentityEditText.error = getString(R.string.required)
                true
            }
            mPasswordEditText.text.toString().trim().isEmpty() -> {
                mPasswordEditText.error = getString(R.string.required)
                true
            }
            else -> false
        }
    }

    /**
     * Limpia los mensajes de error de los campos de entrada
     */
    private fun cleanErrors() {
        mIdentityEditText.error = null
        mPasswordEditText.error = null
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