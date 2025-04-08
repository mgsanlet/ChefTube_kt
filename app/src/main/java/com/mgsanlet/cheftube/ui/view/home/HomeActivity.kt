package com.mgsanlet.cheftube.ui.view.home

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
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
import com.mgsanlet.cheftube.ChefTubeApplication
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.ActivityHomeBinding
import com.mgsanlet.cheftube.utils.FragmentNavigator
import com.mgsanlet.cheftube.utils.SystemUiHelper
import com.mgsanlet.cheftube.ui.view.auth.AuthActivity
import com.yariksoffice.lingver.Lingver
import java.util.Locale
import androidx.core.content.edit

/**
 * HomeActivity contiene la vista principal de la aplicación desde la que el usuario
 * puede navegar entre las diferentes secciones a través del menú de navegación inferior y
 * el menú contextual superior.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private val app by lazy { ChefTubeApplication.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        this.enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar MaterialToolbar
        setSupportActionBar(binding.materialToolbar)
        supportActionBar?.title = ""

        // Configurar BottomNavigationView
        setUpBottomNav()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_contact_us -> onContactUs()
            R.id.action_language -> onLanguage()
            R.id.action_logout -> onLogout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onContactUs() {
        val emailIntent = Intent(
            Intent.ACTION_SENDTO, Uri.fromParts(
                URI_MAIL_TO_SCHEME, SUPPORT_EMAIL, null
            )
        )
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            Toast.makeText(applicationContext, getString(R.string.no_email_app), Toast.LENGTH_SHORT).show()
        }
    }

    private fun onLanguage() {
        val languages = resources.getStringArray(R.array.languages)
        val languageCodes = arrayOf("en", "es", "it")

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_title))
            .setItems(languages) { _: DialogInterface?, indexOfSelected: Int ->
                val selectedLanguage = languageCodes[indexOfSelected]
                Lingver.getInstance().setLocale(this, Locale(selectedLanguage))
                recreate()
                saveLanguage(selectedLanguage)
            }
            .show()
    }

    private fun saveLanguage(languageCode: String) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit {
                putString(LANGUAGE_KEY, languageCode)
            }
    }

    private fun onLogout() {
        app.setCurrentUser(null)
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }

    private fun setUpBottomNav() {
        binding.bottomNavigationView.selectedItemId = R.id.home_item
        binding.bottomNavigationView.setOnApplyWindowInsetsListener(null)
        binding.bottomNavigationView.setPadding(0, 0, 0, 0)

        binding.bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.profile_item -> {
                    FragmentNavigator.loadFragment(
                        this, null, ProfileFragment(), R.id.fragmentContainerView
                    )
                    true
                }
                R.id.home_item -> {
                    FragmentNavigator.loadFragment(
                        this, null, RecipeFeedFragment(), R.id.fragmentContainerView
                    )
                    true
                }
                R.id.scanner_item -> {
                    FragmentNavigator.loadFragment(
                        this, null, ScannerFragment(), R.id.fragmentContainerView
                    )
                    true
                }
                else -> false
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "AppPrefs"
        private const val LANGUAGE_KEY = "language"
        private const val URI_MAIL_TO_SCHEME = "mailto"
        private const val SUPPORT_EMAIL = "support@cheftube.com"
    }
}
