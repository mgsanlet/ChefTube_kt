package com.mgsanlet.cheftube.ui.view.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.ViewAccountSettingsBinding

/**
 * Vista personalizada que muestra las opciones de configuración de la cuenta de usuario.
 *
 * Esta vista incluye funcionalidades para:
 * - Expandir/contraer la sección de configuración
 * - Cambiar la contraseña
 * - Eliminar la cuenta
 *
 * @property context Contexto de la aplicación
 * @property attrs Atributos XML personalizados
 * @property defStyleAttr Estilo por defecto
 */
class AccountSettingsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewAccountSettingsBinding = ViewAccountSettingsBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    init {
        setUpListeners()
    }

    /**
     * Configura los listeners para los elementos interactivos de la vista.
     *
     * Incluye la lógica para expandir/contraer la sección de configuración
     * y actualizar el ícono del botón correspondientemente.
     */
    private fun setUpListeners() {
        binding.expandToggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.accountBody.visibility = VISIBLE
                binding.expandToggleButton.setBackgroundResource(R.drawable.ic_arrow_v3_up_24)
            } else {
                binding.accountBody.visibility = GONE
                binding.expandToggleButton.setBackgroundResource(R.drawable.ic_arrow_v3_down_24)
            }
        }
    }

    /**
     * Establece el listener para el botón de cambio de contraseña.
     *
     * @param listener Función que se ejecutará cuando se haga clic en el botón de cambiar contraseña
     */
    fun setOnPasswordClickListener(listener: () -> Unit) {
        binding.changePasswordButton.setOnClickListener {
            listener()
        }
    }

    /**
     * Establece el listener para el botón de eliminar cuenta.
     *
     * @param listener Función que se ejecutará cuando se haga clic en el botón de eliminar cuenta
     */
    fun setOnDeleteClickListener(listener: () -> Unit) {
        binding.deleteAccountButton.setOnClickListener {
            listener()
        }
    }

    /**
     * Establece el listener para el botón de expandir/contraer.
     *
     * @param listener Función que se ejecutará cuando se intente expandir la vista
     *                 (solo cuando la vista está contraída)
     */
    fun setOnExpandDownClickListener(listener: () -> Unit) {
        binding.expandToggleButton.setOnClickListener {
            if (binding.accountBody.isVisible) {
                listener()
            }
        }
    }
}