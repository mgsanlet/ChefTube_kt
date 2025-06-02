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
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.ActivityHomeBinding
import com.mgsanlet.cheftube.domain.util.Constants.SUPPORT_EMAIL
import com.mgsanlet.cheftube.ui.util.Constants.URI_MAIL_TO_SCHEME
import com.mgsanlet.cheftube.ui.util.FragmentNavigator
import com.mgsanlet.cheftube.ui.view.auth.AuthActivity
import com.mgsanlet.cheftube.ui.viewmodel.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

/**
 * Actividad principal que sirve como contenedor para los fragmentos de la aplicación.
 * 
 * Esta actividad maneja la navegación principal a través de un menú inferior (BottomNavigationView)
 * y un menú contextual superior (Toolbar). También gestiona la autenticación del usuario,
 * el cambio de idioma y la navegación entre las diferentes secciones de la aplicación.
 * 
 * Las secciones principales incluyen:
 * - Feed de recetas
 * - Escáner de códigos QR
 * - Perfil de usuario
 * - Panel de administración (solo para administradores)
 */
@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {


    /** Binding para las vistas de la actividad. */
    private lateinit var binding: ActivityHomeBinding
    
    /** ViewModel que maneja la lógica de negocio de la actividad. */
    private val viewModel: HomeViewModel by viewModels()

    /**
     * Se llama cuando se crea la actividad.
     * 
     * Configura la interfaz de usuario, inicializa el ViewModel, configura la barra de herramientas
     * y la navegación inferior. También verifica los permisos de administrador del usuario.
     *
     * @param savedInstanceState Estado anterior de la actividad, si existe
     */
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

        viewModel.isAdmin.observe(this) { isAdmin ->
            invalidateOptionsMenu() // Esto forzará a que se vuelva a crear el menú
        }
        
        Toast.makeText(applicationContext, getString(R.string.welcome_message), Toast.LENGTH_SHORT)
            .show()
    }

    /**
     * Infla el menú de opciones de la barra de herramientas.
     * 
     * @param menu Menú en el que se colocarán los elementos
     * @return true para mostrar el menú, false en caso contrario
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_bar_menu, menu)
        
        // Ocultar/mostrar el ítem de administrador según el rol
        val adminItem = menu.findItem(R.id.admin_panel_item)
        adminItem?.isVisible = viewModel.isAdmin.value == true
        
        return true
    }

    /**
     * Maneja la selección de elementos del menú de opciones.
     * 
     * @param item Elemento del menú que se ha seleccionado
     * @return true si el evento se consumió, false en caso contrario
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_contact_us -> onContactUs()
            R.id.action_language -> onLanguage()
            R.id.action_logout -> onLogout()
            R.id.admin_panel_item -> onAdminPanel()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Abre la aplicación de correo electrónico predeterminada con un nuevo correo dirigido al soporte.
     * Si no hay ninguna aplicación de correo electrónico disponible, muestra un mensaje al usuario.
     */
    private fun onContactUs() {
        val emailIntent = Intent(
            Intent.ACTION_SENDTO, Uri.fromParts(
                URI_MAIL_TO_SCHEME, SUPPORT_EMAIL, null
            )
        )
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            Toast.makeText(applicationContext, getString(R.string.no_email_app), Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * Muestra un diálogo para seleccionar el idioma de la aplicación.
     * Actualiza la configuración regional y reinicia la actividad para aplicar los cambios.
     */
    private fun onLanguage() {
        val languages = resources.getStringArray(R.array.languages)
        val languageCodes = arrayOf("en", "es", "it")

        AlertDialog.Builder(this).setTitle(getString(R.string.dialog_title))
            .setItems(languages) { _: DialogInterface?, indexOfSelected: Int ->
                val selectedLanguage = languageCodes[indexOfSelected]
                viewModel.setLocale(Locale(selectedLanguage))
                recreate()
            }.show()
    }

    /**
     * Cierra la sesión del usuario actual y redirige a la pantalla de autenticación.
     * Muestra un diálogo de confirmación antes de proceder con el cierre de sesión.
     */
    private fun onLogout() {
        viewModel.handleLogout()
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }

    /**
     * Navega al panel de administración.
     * Solo está disponible para usuarios con permisos de administrador.
     */
    private fun onAdminPanel() {
        FragmentNavigator.loadFragment(
            this, null, AdminFragment(), R.id.fragmentContainerView
        )
    }

    /**
     * Configura la navegación inferior de la aplicación.
     * 
     * Establece los listeners para los elementos del menú de navegación y carga el fragmento
     * correspondiente a la opción seleccionada. También actualiza el título de la barra de herramientas
     * según la sección actual.
     */
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
}
