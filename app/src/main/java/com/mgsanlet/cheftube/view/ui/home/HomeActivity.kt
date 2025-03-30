package com.mgsanlet.cheftube.view.ui.home

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.model.User
import com.mgsanlet.cheftube.utils.FragmentNavigator
import com.mgsanlet.cheftube.utils.SystemUiHelper
import com.mgsanlet.cheftube.view.ui.auth.AuthActivity
import com.yariksoffice.lingver.Lingver
import java.util.Locale

/**
 * HomeActivity contiene la vista principal de la aplicación desde la que el usuario
 * puede navegar entre las diferentes secciones a travñes del menú de navegación inferior y
 * el menú contextual superior.
 *
 * @author MarioG
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var mTopMaterialToolbar: MaterialToolbar
    private lateinit var mBottomNavView: BottomNavigationView

    private var mLoggedUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        SystemUiHelper.hideSystemBars(window.decorView)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mTopMaterialToolbar = findViewById(R.id.materialToolbar)
        mBottomNavView = findViewById(R.id.bottomNavigationView)

        // Guardar los datos que llegan desde LoginFragment
        mLoggedUser = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            intent.getSerializableExtra(ARG_USER, User::class.java) //TODO cambiar a parcelable
        }else{
            @Suppress("DEPRECATION") // Solo se usará para versiones antiguas
            intent.getSerializableExtra(ARG_USER) as User?
        }

        // Configurar MaterialToolbar
        setSupportActionBar(mTopMaterialToolbar)
        if (supportActionBar != null) {
            supportActionBar!!.title = ""
        }
        // Configurar BottomNavigationView
        setUpBottomNav()
    }

    /* ************************************************************
     *                 TOP MATERIAL TOOLBAR MENU                  *
     ************************************************************ */

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_contact_us) {
            onContactUs()
        }
        if (id == R.id.action_language) {
            onLanguage()
        }
        if (id == R.id.action_logout) {
            onLogout()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Abre una app de correo electrónica para enviar un correo a soporte.
     * Hace uso de un Intent con `ACTION_SENDTO` y el esquema de URI "mailto".
     * Si no hay aplicación de emial disponible, muestra un Toast.
     */
    private fun onContactUs() {
        // Crear Intent para abrir app de email
        val emailIntent = Intent(
            Intent.ACTION_SENDTO, Uri.fromParts(
                URI_MAIL_TO_SCHEME, SUPPORT_EMAIL, null
            )
        )
        // Comprobar si no hay una app adecuada en el package manager del sistema
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            Toast.makeText(applicationContext, getString(R.string.no_email_app), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Muestra un menú desplegable que permite la selección de idiomas
     */
    private fun onLanguage() {
        val languages = resources.getStringArray(R.array.languages)
        // Array de códigos de idioma en formato ISO 639-1
        val languageCodes = arrayOf("en", "es", "it")

        val builder = AlertDialog.Builder(this)

        builder.setTitle(getString(R.string.dialog_title))
        builder.setItems(languages) { _: DialogInterface?, indexOfSelected: Int ->
            // Este código se ejecuta cuando se selecciona un lenguaje
            val selectedLanguage = languageCodes[indexOfSelected]
            Lingver.getInstance().setLocale(this, Locale(selectedLanguage))
            recreate()
            saveLanguage(selectedLanguage)
        }
        builder.show()
    }

    /**
     * Guarda el lenguaje seleccionado en SharedPreferences
     */
    private fun saveLanguage(languageCode: String) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(LANGUAGE_KEY, languageCode)
        editor.apply()
    }

    /**
     * Cierra sesión y navega a la sección de autentificación
     */
    private fun onLogout() {
        mLoggedUser = null
        val authActIntent = Intent(this@HomeActivity, AuthActivity::class.java)
        startActivity(authActIntent)
    }

    /* **********************************************************
     *                  BOTTOM NAVIGATION VIEW                  *
     ********************************************************** */

    /**
     * Configura propiedades visuales y el comportamiento de BottomNavigationView
     */
    private fun setUpBottomNav() {
        mBottomNavView.selectedItemId = R.id.home_item //Predeterminado
        // Eliminar insets y paddings
        mBottomNavView.setOnApplyWindowInsetsListener(null)
        mBottomNavView.setPadding(0, 0, 0, 0)

        // Configurar listeners
        mBottomNavView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.profile_item -> {
                    val profileFragment: Fragment = ProfileFragment.newInstance(mLoggedUser)
                    FragmentNavigator.loadFragmentInstance(
                        this, null, profileFragment, R.id.mainFrContainer
                    )
                    return@setOnItemSelectedListener true
                }
                R.id.home_item -> {
                    FragmentNavigator.loadFragment(
                        this, null, RecipeFeedFragment(), R.id.mainFrContainer
                    )
                    return@setOnItemSelectedListener true
                }
                R.id.scanner_item -> {
                    FragmentNavigator.loadFragment(
                        this, null, ScannerFragment(), R.id.mainFrContainer
                    )
                    return@setOnItemSelectedListener true
                }
                else -> {
                    return@setOnItemSelectedListener false
                }
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "AppPrefs"
        private const val LANGUAGE_KEY = "language"
        private const val ARG_USER = "user"
        private const val SUPPORT_EMAIL = "support@cheftube.com"
        private const val URI_MAIL_TO_SCHEME = "mailto"
    }
}

