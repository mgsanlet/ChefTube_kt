package com.mgsanlet.cheftube.view.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mgsanlet.cheftube.ChefTubeApplication
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.model.User
import com.mgsanlet.cheftube.utils.FragmentNavigator

/**
 * Un fragmento responsable de manejar el proceso de registro de usuario.
 * Este fragmento permite al usuario registrarse proporcionando un nombre,
 * correo electrónico y contraseña.
 */
class SignUpFragment : Fragment() {

    private lateinit var mNameEditText: EditText
    private lateinit var mEmailEditText: EditText
    private lateinit var mPassword1EditText: EditText
    private lateinit var mPassword2EditText: EditText
    private lateinit var mSaveButton: Button

    private val app by lazy { ChefTubeApplication.getInstance(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        mNameEditText = view.findViewById(R.id.signUpNameField)
        mEmailEditText = view.findViewById(R.id.signUpEmailField)
        mPassword1EditText = view.findViewById(R.id.signUpPwdField)
        mPassword2EditText = view.findViewById(R.id.signUpPwd2Field)
        mSaveButton = view.findViewById(R.id.saveBtn)

        mSaveButton.setOnClickListener { tryRegister() }

        return view
    }

    private fun tryRegister() {
        if (!isValidRegister) return

        app.userRepository.createUser(
            mNameEditText.text.toString(),
            mEmailEditText.text.toString(),
            mPassword1EditText.text.toString()
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

        if (mNameEditText.text.toString().trim().isEmpty()) {
            mNameEditText.error = requiredMessage
            isEmpty = true
        }
        if (mEmailEditText.text.toString().trim().isEmpty()) {
            mEmailEditText.error = requiredMessage
            isEmpty = true
        }
        if (mPassword1EditText.text.toString().trim().isEmpty()) {
            mPassword1EditText.error = requiredMessage
            isEmpty = true
        }
        if (mPassword2EditText.text.toString().trim().isEmpty()) {
            mPassword2EditText.error = requiredMessage
            isEmpty = true
        }

        return isEmpty
    }

    /**
     * Valida que el correo electrónico tenga un formato válido
     */
    private val isValidEmail: Boolean
        get() {
            val email = mEmailEditText.text.toString()
            return when {
                Patterns.EMAIL_ADDRESS.matcher(email).matches() -> true
                else -> {
                    mEmailEditText.error = getString(R.string.invalid_email)
                    false
                }
            }
        }

    /**
     * Valida que la contraseña tenga la longitud mínima requerida
     */
    private val isValidPwd: Boolean
        get() {
            val password = mPassword1EditText.text.toString()
            return when {
                password.length < User.PASSWORD_MIN_LENGTH -> {
                    mPassword1EditText.error = getString(R.string.short_pwd)
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
            mPassword1EditText.text.toString() == mPassword2EditText.text.toString() -> true
            else -> {
                mPassword2EditText.error = getString(R.string.pwd_d_match)
                false
            }
        }
    }
}