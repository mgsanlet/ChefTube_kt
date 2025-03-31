package com.mgsanlet.cheftube.view.ui.home

import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.local.UserDAO
import com.mgsanlet.cheftube.data.model.User

/**
 * ProfileFragment permite al usuario ver y modificar los detalles de su perfil,
 * incluyendo nombre de usuario, email y contraseña.
 * Se carga desde el BottomNavigationView de HomeActivity
 *
 * @author MarioG
 */
class ProfileFragment : Fragment() {

    private var mLoggedUser: User? = null

    private lateinit var mUsernameEditText    : EditText
    private lateinit var mEmailEditText       : EditText
    private lateinit var mOldPasswordEditText : EditText
    private lateinit var mNewPassword1EditText: EditText
    private lateinit var mNewPassword2EditText: EditText
    private lateinit var mSaveButton          : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mLoggedUser = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getSerializable(ARG_USER, User::class.java) //TODO cambiar a parcelable
            }else{
                @Suppress("DEPRECATION") // Solo se usará para versiones antiguas
                requireArguments().getSerializable(ARG_USER) as User?
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        mUsernameEditText = view.findViewById(R.id.profileNameField)
        mEmailEditText = view.findViewById(R.id.profileEmailField)
        mOldPasswordEditText = view.findViewById(R.id.profilePwdField)
        mNewPassword1EditText = view.findViewById(R.id.profileNewPwdField)
        mNewPassword2EditText = view.findViewById(R.id.profileNewPwd2Field)
        mSaveButton = view.findViewById(R.id.profileSaveBtn)

        // Listeners
        mSaveButton.setOnClickListener {
            if (isValidData) {
                // Guardando datos de usuario actualizados
                mLoggedUser!!.saveNewIdentity(
                    mUsernameEditText.getText().toString(),
                    mEmailEditText.getText().toString()
                )
                if (mNewPassword1EditText.getText().toString().trim { it <= ' ' }.isNotEmpty()) {
                    mLoggedUser!!.saveNewPassword(mNewPassword1EditText.getText().toString())
                }
                UserDAO.updateUser(mLoggedUser, context) // -Updating the user database-
                Toast.makeText(context, getString(R.string.data_saved), Toast.LENGTH_SHORT).show()
            }
        }
        loadUserCurrentData()
        return view
    }

    private fun loadUserCurrentData() {
        mUsernameEditText.setText(mLoggedUser!!.username)
        mEmailEditText.setText(mLoggedUser!!.email)
    }

    private val isValidData: Boolean
        /**
         * Valida los datos introducidos por el usuario
         *
         * @return True si todos los datos son válidos, false si no.
         */
        get() = (!fieldsAreEmpty() &&
                isValidEmail &&
                !isExistentUsername &&
                !isExistentEmail &&
                isValidPwd &&
                passwordsMatch()
                )

    /**
     * Comprueba si los campos están vacíos y si no es así los marca con error
     *
     * @return True si algún campo estaá vacío, false si no.
     */
    private fun fieldsAreEmpty(): Boolean {
        var empty = false
        val requiredMessage = getString(R.string.required)
        if (mUsernameEditText.text.toString().trim { it <= ' ' }.isEmpty()) {
            mUsernameEditText.error = requiredMessage
            empty = true
        }
        if (mEmailEditText.text.toString().trim { it <= ' ' }.isEmpty()) {
            mEmailEditText.error = requiredMessage
            empty = true
        }
        if (mOldPasswordEditText.text.toString().trim { it <= ' ' }.isEmpty()) {
            mOldPasswordEditText.error = requiredMessage
            empty = true
        }
        if (mNewPassword1EditText.text.toString().trim { it <= ' ' }.isNotEmpty() &&
            mNewPassword2EditText.text.toString().trim { it <= ' ' }.isEmpty()
        ) {
            mNewPassword2EditText.error = requiredMessage
            empty = true
        }
        if (mNewPassword2EditText.text.toString().trim { it <= ' ' }.isNotEmpty() &&
            mNewPassword1EditText.text.toString().trim { it <= ' ' }.isEmpty()
        ) {
            mNewPassword1EditText.error = requiredMessage
            empty = true
        }
        return empty
    }

    private val isValidEmail: Boolean
        /**
         * Valida el formato del email usando un patrón.
         *
         * @return True si el formato del email es válido, false si no.
         */
        get() {
            val email = mEmailEditText.text.toString()
            val isValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
            if (!isValid) {
                mEmailEditText.error = getString(R.string.invalid_email)
            }
            return isValid
        }

    private val isExistentEmail: Boolean
        /**
         * Comprueba si el email introducido ya existe en el sistema
         * comparando con los emails de todos los usuarios registrados.
         *
         * @return True si el email ya existe, false si no.
         */
        get() {
            val inputEmail = mEmailEditText.text.toString()
            val isExistent = UserDAO.isExistentEmail(inputEmail, context)
            if (isExistent && inputEmail != mLoggedUser!!.email) {
                mEmailEditText.error = getString(R.string.email_already)
            }
            return isExistent && inputEmail != mLoggedUser!!.email
        }

    private val isExistentUsername: Boolean
        /**
         * Comprueba si el nombre de usuario introducido ya existe en el sistema
         * comparando con los nombres de usuario de todos los usuarios registrados.
         *
         * @return True si el nombre de usuario ya existe, false si no.
         */
        get() {
            val inputUsername = mUsernameEditText.text.toString()
            val isExistent = UserDAO.isExistentUsername(inputUsername, context)
            if (isExistent && inputUsername != mLoggedUser!!.username) {
                mUsernameEditText.error = getString(R.string.username_already)
            }
            return isExistent && inputUsername != mLoggedUser!!.username
        }

    private val isValidPwd: Boolean
        /**
         * Comprueba la longitud de la nueva contraseña.
         *
         * @return True si la nueva contraseña supera la longitud mínima o es vacía
         * (no se desea cambiar la contraseña), false si no.
         */
        get() {
            var isValid = true
            if (mNewPassword1EditText.text.toString().trim { it <= ' ' }.isEmpty()) {
                return true // -If the field is empty, password will remain unchanged-
            }
            if (mNewPassword1EditText.text.toString().length < PASSWORD_MIN_LENGTH) {
                mNewPassword1EditText.error = getString(R.string.short_pwd)
                isValid = false
            }
            return isValid
        }

    /**
     * Verifica si los dos campos de contraseña coinciden.
     *
     * @return True si las contraseñas coinciden, False de lo contrario.
     */
    private fun passwordsMatch(): Boolean {
        var areMatching = true
        if (mOldPasswordEditText.text.toString() != mLoggedUser!!.password) {
            mOldPasswordEditText.error = getString(R.string.wrong_pwd)
            return false
        }
        if (mNewPassword1EditText.text.toString() != mNewPassword2EditText.text.toString()) {
            areMatching = false
            mNewPassword2EditText.error = getString(R.string.pwd_d_match)
        }
        return areMatching
    }

    companion object {
        private const val ARG_USER = "user"
        private const val PASSWORD_MIN_LENGTH = 5

        fun newInstance(user: User?): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putSerializable(
                ARG_USER,
                user
            )
            fragment.arguments = args
            return fragment
        }
    }


}