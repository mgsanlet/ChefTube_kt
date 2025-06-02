package com.mgsanlet.cheftube.ui.view.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mgsanlet.cheftube.databinding.ActivitySplashBinding
import com.mgsanlet.cheftube.ui.util.hideSystemBars

/**
 * Actividad de presentación que se muestra al iniciar la aplicación.
 * 
 * Muestra una pantalla con el logo y el título de la aplicación con animaciones.
 * Utiliza animaciones personalizadas en lugar de la SplashScreenAPI de Android (API 31+)
 * para mantener la compatibilidad con versiones anteriores (hasta API 26).
 */
@SuppressLint("CustomSplashScreen")
// Se usa implementación personalizada para mayor control y compatibilidad
class SplashActivity : AppCompatActivity() {

    /** Binding para las vistas de la actividad. */
    private lateinit var binding: ActivitySplashBinding

    /**
     * Se llama cuando se crea la actividad.
     * Configura la interfaz de usuario, inicia las animaciones y programa la transición.
     *
     * @param savedInstanceState Estado anterior de la actividad, si existe
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        this.enableEdgeToEdge()
        window.decorView.hideSystemBars()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.logoImageView.alpha = 0f
        binding.titleImageView.alpha = 0f

        binding.logoImageView.alpha = 0f // Comienza completamente transparente
        binding.logoImageView.scaleX = 0.5f // Comienza más pequeño
        binding.logoImageView.scaleY = 0.5f // Comienza más pequeño

        binding.logoImageView.animate().alpha(1f).scaleX(1.5f).scaleY(1.5f).rotation(360f)
            .setDuration(ANIMATION_DURATION)

        binding.titleImageView.alpha = 0f // Comienza completamente transparente
        binding.titleImageView.scaleX = 0.5f // Comienza más pequeño
        binding.titleImageView.scaleY = 0.5f // Comienza más pequeño

        binding.titleImageView.animate().alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(ANIMATION_DURATION).withEndAction {
                // Iniciar AuthActivity después de que finalice la animación del título
                startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
                finish()
            }
    }

    /**
     * Se llama cuando la actividad está a punto de ser destruida.
     * Cancela las animaciones en curso para prevenir fugas de memoria.
     */
    override fun onDestroy() {
        super.onDestroy()
        // Cancela las animaciones en curso para prevenir fugas de memoria
        binding.logoImageView.animate().cancel()
        binding.titleImageView.animate().cancel()
    }

    companion object {
        /** Duración en milisegundos para las animaciones de la pantalla de presentación. */
        private const val ANIMATION_DURATION = 1000L
    }
}