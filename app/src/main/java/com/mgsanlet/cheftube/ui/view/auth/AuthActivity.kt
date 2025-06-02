package com.mgsanlet.cheftube.ui.view.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.ui.util.hideSystemBars
import com.mgsanlet.cheftube.ui.view.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Actividad principal de autenticación que maneja el flujo de inicio de sesión y registro.
 * Contiene una imagen de fondo, el logo de la aplicación y un contenedor de fragmentos
 * donde se cargan los fragmentos de inicio de sesión y registro.
 */
@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    /**
     * Se llama cuando se crea la actividad.
     * Configura la vista, el tema de pantalla completa y los listeners de insets.
     *
     * @param savedInstanceState Bundle que contiene el estado anterior de la actividad
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_auth)
        
        // Configura la pantalla completa
        window.decorView.hideSystemBars()
        
        // Maneja los insets para el diseño edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) {
            v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Navega a la pantalla principal de la aplicación y finaliza esta actividad.
     * Se llama cuando la autenticación es exitosa.
     */
    fun navToHomePage() {
        val mainActIntent = Intent(this, HomeActivity::class.java)
        startActivity(mainActIntent)
        this.finish()
    }
}