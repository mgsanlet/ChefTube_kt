package com.mgsanlet.cheftube.ui.utils

import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
/**
 * SystemUiHelper es una clase de utilidad que proporciona métodos para gestionar la visibilidad
 * de elementos del sistema
 *
 * @autor MarioG
 */
object SystemUiHelper {
    /**
     * Oculta las barras de estado y navegación en la vista proporcionada.
     *
     * Este método utiliza el controlador de insets de la ventana para ocultar las barras del sistema
     * en dispositivos que ejecutan Android S (API 31) y versiones posteriores. Para versiones anteriores,
     * se utilizan banderas de visibilidad de sistema UI.
     *
     * @param view La vista en la que se desea ocultar las barras del sistema.
     */
    @Suppress("DEPRECATION") // Las funciones obsoletas solo se usarán para versiones antiguas
    fun hideSystemBars(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val windowInsetsController = view.windowInsetsController
            windowInsetsController?.hide(
                WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            windowInsetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
        } else {
            view.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }
}