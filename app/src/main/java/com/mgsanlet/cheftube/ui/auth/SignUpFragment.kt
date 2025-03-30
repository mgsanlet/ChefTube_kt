package com.mgsanlet.cheftube.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.local.UserDAO
import com.mgsanlet.cheftube.data.model.User
import com.mgsanlet.cheftube.ui.auth.LoginFragment.Companion.newInstance
import com.mgsanlet.cheftube.utils.FragmentNavigator

/**
 * Un fragmento responsable de manejar el proceso de registro de usuario.
 * Este fragmento permite al usuario registrarse proporcionando un nombre, correo electrónico y contraseña.
 * Valida los datos de entrada y asegura que el correo electrónico no esté ya en uso.
 * Después de un registro exitoso, el usuario es redirigido al fragmento de inicio de sesión
 * con credenciales prellenadas.
 *
 * @author MarioG
 */
class SignUpFragment : Fragment() {
    // Miembros UI
    private lateinit var mNameEditText:  EditText
    private lateinit var mEmailEditText: EditText
    private lateinit var mPassword1EditText:   EditText
    private lateinit var mPassword2EditText:  EditText
    private lateinit var mSaveButton:    Button

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

        // Listeners
        mSaveButton.setOnClickListener {
            if (isValidRegister) {
                loadLoginFr()
            }
        }

        return view
    }

    /**
     * Valida la entrada del usuario para el registro. Asegura que todos los campos estén llenos,
     * el formato del correo electrónico sea correcto, que el correo electrónico no esté ya en uso,
     * que la contraseña sea válida y que los campos de contraseña coincidan.
     *
     * @return True si todas las validaciones pasan, False de lo contrario.
     */
    private val isValidRegister: Boolean
        get() = (!fieldsAreEmpty() &&
                isValidEmail &&
                !isExistentEmail &&
                !isExistentUsername &&
                isValidPwd &&
                passwordsMatch()
                )

    /**
     * Verifica si alguno de los campos requeridos (nombre, correo electrónico, contraseña) está vacío.
     * Establece un mensaje de error en el campo correspondiente si está vacío.
     *
     * @return True si algún campo está vacío, False de lo contrario.
     */
    private fun fieldsAreEmpty(): Boolean {
        return when {
            mNameEditText.text.toString().trim { it <= ' ' }.isEmpty() ||
            mEmailEditText.text.toString().trim { it <= ' ' }.isEmpty() ||
            mPassword1EditText.text.toString().trim { it <= ' ' }.isEmpty() ||
            mPassword2EditText.text.toString().trim { it <= ' ' }.isEmpty() -> {
                val requiredMessage = getString(R.string.required)
                if (mNameEditText.text.toString().trim { it <= ' ' }.isEmpty()) {
                    mNameEditText.error = requiredMessage
                }
                if (mEmailEditText.text.toString().trim { it <= ' ' }.isEmpty()) {
                    mEmailEditText.error = requiredMessage
                }
                if (mPassword1EditText.text.toString().trim { it <= ' ' }.isEmpty()) {
                    mPassword1EditText.error = requiredMessage
                }
                if (mPassword2EditText.text.toString().trim { it <= ' ' }.isEmpty()) {
                    mPassword2EditText.error = requiredMessage
                }
                true
            }
            else -> false
        }
    }

    /**
     * Valida que el correo electrónico ingresado esté en un formato válido utilizando expresiones regulares.
     *
     * @return True si el correo electrónico es válido, False de lo contrario.
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
     * Verifica si el correo electrónico ingresado ya existe en el sistema comparándolo con
     * los correos electrónicos de todos los usuarios registrados.
     *
     * @return True si el correo electrónico ya existe, False de lo contrario.
     */
    private val isExistentEmail: Boolean
        get() {
            val inputEmail = mEmailEditText.text.toString()

            return when {
                UserDAO.isExistentEmail(inputEmail, context) -> {
                    mEmailEditText.error = getString(R.string.email_already)
                    true
                }
                else -> false
            }
        }

    /**
     * Verifica si el nombre de usuario ingresado ya existe en el sistema comparándolo con
     * los nombres de usuario de todos los usuarios registrados.
     *
     * @return True si el nombre de usuario ya existe, False de lo contrario.
     */
    private val isExistentUsername: Boolean
        get() {
            val inputUsername = mNameEditText.text.toString()

            return when {
                UserDAO.isExistentUsername(inputUsername, context) -> {
                    mNameEditText.error = getString(R.string.username_already)
                    true
                }
                else -> false
            }
        }

    /**
     * Valida la contraseña para asegurar que tenga al menos 5 caracteres de longitud.
     *
     * @return True si la contraseña es válida, False de lo contrario.
     */
    private val isValidPwd: Boolean
        get() {
            return when {
                mPassword1EditText.text.toString().length < 5 -> {
                    mPassword1EditText.error = getString(R.string.short_pwd)
                    false
                }
                else -> true
            }
        }

    /**
     * Verifica si los dos campos de contraseña coinciden.
     *
     * @return True si las contraseñas coinciden, False de lo contrario.
     */
    private fun passwordsMatch(): Boolean {
        return when {
            mPassword1EditText.text.toString() == mPassword2EditText.text.toString() -> {
                true
            }
            else -> {
                mPassword2EditText.error = getString(R.string.pwd_d_match)
                false
            }
        }
    }

    /**
     * Carga el LoginFragment después de un registro exitoso.
     * El nuevo usuario se registra en el UserDAO y el usuario es redirigido a la página de inicio de sesión
     * con credenciales prellenadas.
     */
    private fun loadLoginFr() {
        cleanErrors()
        val newUser = newUser
        UserDAO.register(newUser, context)
        val loginFr = newInstance(newUser)
        FragmentNavigator.loadFragmentInstance(null, this, loginFr, R.id.authFrContainer)
    }

    /**
     * Limpia todos los mensajes de error de los campos de entrada (nombre, correo electrónico, contraseña).
     */
    private fun cleanErrors() {
        mNameEditText.error = null
        mEmailEditText.error = null
        mPassword1EditText.error = null
        mPassword2EditText.error = null
    }

    /**
     * Crea un nuevo objeto de usuario con los campos de nombre, correo electrónico y contraseña proporcionados.
     *
     * @return Un nuevo objeto de usuario que contiene los detalles de registro.
     */
    private val newUser: User
        get() = User(
            mNameEditText.text.toString(),
            mEmailEditText.text.toString(),
            mPassword1EditText.text.toString()
        )
}