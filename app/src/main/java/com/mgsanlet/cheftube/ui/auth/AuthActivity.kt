package com.mgsanlet.cheftube.ui.auth

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.utils.SystemUiHelper

/**
 * AuthActivity contiene una imagen de fondo, el logo de la aplicación y las imágenes del título,
 * así como un contenedor de fragmentos en el que se cargan el fragmento de inicio de sesión y el fragmento de registro.
 * @autor MarioG
 */
class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_auth)
        SystemUiHelper.hideSystemBars(window.decorView)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}