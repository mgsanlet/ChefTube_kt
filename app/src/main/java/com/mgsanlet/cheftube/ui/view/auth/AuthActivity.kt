package com.mgsanlet.cheftube.ui.view.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseApp
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.source.remote.FirebaseRecipeProvider
import com.mgsanlet.cheftube.ui.util.hideSystemBars
import com.mgsanlet.cheftube.ui.view.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * AuthActivity contiene una imagen de fondo, el logo de la aplicación y las imágenes del título,
 * así como un contenedor de fragmentos en el que se cargan el fragmento de inicio de sesión y el fragmento de registro.
 * @autor MarioG
 */
@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_auth)
        window.decorView.hideSystemBars()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val provider = FirebaseRecipeProvider()
        provider.getById("recipe01")
        provider.getAll()
    }

    fun navToHomePage() {
        val mainActIntent = Intent(this, HomeActivity::class.java)
        startActivity(mainActIntent)
        this.finish()
    }
}