package com.mgsanlet.cheftube.view.ui.auth

import android.content.Intent
import android.os.Build
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
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.local.UserDAO
import com.mgsanlet.cheftube.data.model.User
import com.mgsanlet.cheftube.view.ui.home.HomeActivity
import com.mgsanlet.cheftube.utils.FragmentNavigator

/**
 * Fragmento que maneja el proceso de inicio de sesión para la aplicación.
 * Permite a los usuarios ingresar sus credenciales (nombre de usuario y contraseña) y
 * los autentica en la aplicación. Además, proporciona un enlace para navegar
 * al fragmento de registro si el usuario no tiene una cuenta.
 *
 * @autor MarioG
 */
class LoginFragment : Fragment() {

    private var mRegisteredUser: User? = null

    private lateinit var mIdentityEditText: EditText
    private lateinit var mPasswordEditText: EditText
    private lateinit var mLoginButton: Button
    private lateinit var mSignUpLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtener argumentos pasados al fragmento
        arguments?.let {
            mRegisteredUser = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getSerializable(ARG_USER, User::class.java) //TODO cambiar a parcelable
            }else{
                @Suppress("DEPRECATION") // Solo se usará para versiones antiguas
                requireArguments().getSerializable(ARG_USER) as User?
            }
        }
    }

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

        // Se colocan el usuario y contraseña si han sido pasados desde el fragmento de SignUp
        mRegisteredUser?.let {
            mIdentityEditText.setText(mRegisteredUser!!.username)
            mPasswordEditText.setText(mRegisteredUser!!.password)
        }

        // Listeners
        mLoginButton.setOnClickListener { tryLogin() }

        mSignUpLink.setOnClickListener {
            cleanErrors()
            FragmentNavigator.loadFragment(null, this, SignUpFragment(), R.id.authFrContainer)
        }
        Log.i("dbtest", "Starting authentication activity...")
        UserDAO.logDBContent(context)
        return view
    }

    /**
     * Intenta iniciar sesión al validar la entrada del usuario y comenzando la actividad principal
     * si las credenciales son correctas. Muestra un mensaje de toast si el inicio de sesión falla.
     */
    private fun tryLogin() {
        when {
            fieldsAreEmpty() -> return
            else -> {
                // Obtener el usuario válido si las credenciales coinciden
                val validUser = UserDAO.getValidUser(
                    mIdentityEditText.text.toString(),
                    mPasswordEditText.text.toString(),
                    context
                )

                if (validUser == null) {
                    Toast.makeText(context, getString(R.string.invalid_login), Toast.LENGTH_SHORT).show()
                } else {
                    navToHomePage(validUser)
                }
            }
        }
    }

    /**
     * Verifica si los campos de inicio de sesión (nombre de usuario y contraseña) están vacíos.
     * Si algún campo está vacío, se muestra un mensaje de error para el campo correspondiente.
     *
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
     * Limpia los mensajes de error de los campos de entrada (nombre de usuario y contraseña).
     */
    private fun cleanErrors() {
        mIdentityEditText.error = null
        mPasswordEditText.error = null
    }

    /**
     * Navega a la página de inicio (HomeActivity) con los datos del usuario válido.
     *
     * @param validUser El objeto [User] que representa al usuario que inició sesión correctamente.
     */
    private fun navToHomePage(validUser: User) {
        Log.i("dbtest", "Login success: " + validUser.id)
        val mainActIntent = Intent(activity, HomeActivity::class.java)
        mainActIntent.putExtra("user", validUser)
        startActivity(mainActIntent)
        activity?.finish()
    }

    companion object {
        private const val ARG_USER = "user"

        fun newInstance(user: User?): LoginFragment {
            val fragment = LoginFragment()
            val args = Bundle()
            args.putSerializable(ARG_USER, user)
            fragment.arguments = args
            return fragment
        }
    }
}