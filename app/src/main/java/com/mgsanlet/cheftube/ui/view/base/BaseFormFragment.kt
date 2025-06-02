package com.mgsanlet.cheftube.ui.view.base

import android.widget.EditText
import androidx.viewbinding.ViewBinding
import com.mgsanlet.cheftube.R

/**
 * Clase base abstracta para fragmentos que contienen formularios.
 * Proporciona funcionalidades comunes para la validación de campos de entrada.
 *
 * @param T Tipo de binding generado para el layout del fragmento
 */
abstract class BaseFormFragment<T : ViewBinding> : BaseFragment<T>() {

    /**
     * Valida si los campos del formulario tienen valores aceptables.
     * Debe ser implementado por las clases hijas para definir la lógica de validación específica.
     *
     * @return true si todos los campos son válidos, false en caso contrario
     */
    protected abstract fun isValidViewInput(): Boolean

    /**
     * Verifica si alguno de los campos de texto proporcionados está vacío.
     * Establece un mensaje de error en los campos vacíos.
     *
     * @param fields Lista de EditText a validar
     * @return true si al menos un campo está vacío, false si todos tienen contenido
     */
    protected fun areFieldsEmpty(fields: List<EditText>): Boolean {
        var hasEmptyFields = false
        for (field in fields) {

            if (field.text.isBlank()) {
                field.error = getString(R.string.required)
                hasEmptyFields = true
            }

        }
        return hasEmptyFields
    }
}