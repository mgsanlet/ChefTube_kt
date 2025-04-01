package com.mgsanlet.cheftube.view.ui.home

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

/**
 * ProfileFragment permite al usuario ver y modificar los detalles de su perfil,
 * incluyendo nombre de usuario, email y contraseña.
 */
class ProfileFragment : Fragment() {

    private lateinit var mUsernameEditText: EditText
    private lateinit var mEmailEditText: EditText
    private lateinit var mOldPasswordEditText: EditText
    private lateinit var mNewPassword1EditText: EditText
    private lateinit var mNewPassword2EditText: EditText
    private lateinit var mSaveButton: Button

    private val app by lazy { ChefTubeApplication.getInstance(requireContext()) }
    private val currentUser get() = app.getCurrentUser()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        mUsernameEditText = view.findViewById(R.id.profileNameField)
        mEmailEditText = view.findViewById(R.id.profileEmailField)
        mOldPasswordEditText = view.findViewById(R.id.profilePwdField)
        mNewPassword1EditText = view.findViewById(R.id.profileNewPwdField)
        mNewPassword2EditText = view.findViewById(R.id.profileNewPwd2Field)
        mSaveButton = view.findViewById(R.id.profileSaveBtn)

        mSaveButton.setOnClickListener { tryUpdateProfile() }
        loadUserCurrentData()
        return view
    }

    private fun loadUserCurrentData() {
        currentUser?.let { user ->
            mUsernameEditText.setText(user.username)
            mEmailEditText.setText(user.email)
        }
    }

    private fun tryUpdateProfile() {
        if (!isValidData) return

        currentUser?.let { user ->
            // Obtener los nuevos datos o mantener los actuales
            val newUsername = mUsernameEditText.text.toString().takeIf { it != user.username } ?: user.username
            val newEmail = mEmailEditText.text.toString().takeIf { it != user.email } ?: user.email
            val oldPassword = mOldPasswordEditText.text.toString()
            
            // Verificar contraseña antigua
            if (!user.verifyPassword(oldPassword)) {
                mOldPasswordEditText.error = getString(R.string.wrong_pwd)
                return
            }

            // Determinar qué contraseña usar (la nueva o la antigua)
            val finalPassword = mNewPassword1EditText.text.toString().ifEmpty {
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
        mOldPasswordEditText.text.clear()
        mNewPassword1EditText.text.clear()
        mNewPassword2EditText.text.clear()
    }

    private val isValidData: Boolean
        get() = !fieldsAreEmpty() &&
                isValidEmail &&
                isValidNewPassword

    private fun fieldsAreEmpty(): Boolean {
        var empty = false
        val requiredMessage = getString(R.string.required)

        if (mUsernameEditText.text.toString().trim().isEmpty()) {
            mUsernameEditText.error = requiredMessage
            empty = true
        }
        if (mEmailEditText.text.toString().trim().isEmpty()) {
            mEmailEditText.error = requiredMessage
            empty = true
        }
        if (mOldPasswordEditText.text.toString().trim().isEmpty()) {
            mOldPasswordEditText.error = requiredMessage
            empty = true
        }

        return empty
    }

    private val isValidEmail: Boolean
        get() {
            val email = mEmailEditText.text.toString()
            return if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mEmailEditText.error = getString(R.string.invalid_email)
                false
            } else true
        }

    private val isValidNewPassword: Boolean
        get() {
            val newPassword1 = mNewPassword1EditText.text.toString()
            val newPassword2 = mNewPassword2EditText.text.toString()

            // Si no hay nueva contraseña, es válido
            if (newPassword1.isEmpty() && newPassword2.isEmpty()) {
                return true
            }

            // Si solo uno de los campos está vacío, no es válido
            if (newPassword1.isEmpty() || newPassword2.isEmpty()) {
                if (newPassword1.isEmpty()) mNewPassword1EditText.error = getString(R.string.required)
                if (newPassword2.isEmpty()) mNewPassword2EditText.error = getString(R.string.required)
                return false
            }

            // Verificar que las contraseñas coincidan
            if (newPassword1 != newPassword2) {
                mNewPassword2EditText.error = getString(R.string.pwd_d_match)
                return false
            }

            // Verificar longitud mínima
            if (newPassword1.length < User.PASSWORD_MIN_LENGTH) {
                mNewPassword1EditText.error = getString(R.string.short_pwd)
                return false
            }

            return true
        }
}