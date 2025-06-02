package com.mgsanlet.cheftube.ui.view.customviews

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.setPadding
import com.mgsanlet.cheftube.R

/**
 * Vista de texto personalizada que muestra el nivel de dificultad de una receta.
 *
 * Muestra el nivel de dificultad con un color de fondo que varía según la dificultad:
 * - Fácil: Verde oscuro
 * - Media: Naranja oscuro
 * - Difícil: Rojo oscuro
 *
 * @property context Contexto de la aplicación
 * @property attrs Atributos XML personalizados
 * @property defStyleAttr Estilo por defecto
 */
class DifficultyTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): AppCompatTextView(context, attrs, defStyleAttr) {

    /**
     * Nivel de dificultad actual.
     *
     * Valores posibles:
     * - 0: Fácil
     * - 1: Media
     * - 2: Difícil
     */
    private var currentState = 0

    private val difficultyTexts: Array<String> = context.resources.getStringArray(R.array.difficulty)
    private val backgroundColors: List<Int> = listOf(
        context.getColor(R.color.dark_green),
        context.getColor(R.color.dark_orange),
        context.getColor(R.color.dark_red)
    )

    init {
        setDifficulty(currentState)
        setTextColor(context.getColor(R.color.white))
        setPadding(10)
        textSize = 18f
    }

    /**
     * Establece el nivel de dificultad a mostrar.
     *
     * Actualiza el texto y el color de fondo según el nivel de dificultad.
     * Si el estado proporcionado no es válido, no se realiza ningún cambio.
     *
     * @param state Nivel de dificultad (0-2)
     */
    fun setDifficulty(state: Int) {
        if (state !in 0..2) return
        currentState = state
        text = difficultyTexts[state]
        setBackgroundColor(backgroundColors[state])

    }
}