package com.mgsanlet.cheftube.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.utils.SystemUiHelper

/**
 * SplashActivity es la pantalla introductoria que aparece cuando la aplicación se inicia.
 * Muestra una pantalla de presentación con un logo animado y un título. Las animaciones se aplican
 * a las imágenes del logo y del título utilizando la clase `ViewPropertyAnimator`, y después de que
 * las animaciones finalizan, la actividad transiciona a la siguiente pantalla, `AuthActivity`.
 * @author MarioG
 */
@SuppressLint("CustomSplashScreen")
/* No se usará la SplashScreenAPI de Android disponible desde API 31 ya que se necesita asegurar
la compatibilidad con versiones desde la API 26 y se prefiere centralizar la logica de la
pantalla de presentación en esta clase para que sea más mantenible*/
class SplashActivity : AppCompatActivity() {
    // Elementos UI
    private lateinit var mLogoImageView: ImageView
    private lateinit var mTitleImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        SystemUiHelper.hideSystemBars(window.decorView)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) {
            v: View, insets: WindowInsetsCompat ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
        }

        mLogoImageView = findViewById(R.id.splash_logo)
        mTitleImageView = findViewById(R.id.splash_title)

        mLogoImageView.alpha = 0f
        mTitleImageView.alpha = 0f

        mLogoImageView.animate()
            .alpha(1f)
            .setDuration(ANIMATION_DURATION)

        mTitleImageView.animate()
            .alpha(1f)
            .setDuration(ANIMATION_DURATION)
            .withEndAction {
                // Iniciar AuthActivity después de que finalice la animación del título
                startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
                finish()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancela las animaciones en curso para prevenir fugas de memoria
        mLogoImageView.animate().cancel()
        mTitleImageView.animate().cancel()
    }

    companion object {
        private const val ANIMATION_DURATION = 500L
    }
}