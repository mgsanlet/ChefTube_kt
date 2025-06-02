package com.mgsanlet.cheftube.ui.view.customviews

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.setPadding
import com.mgsanlet.cheftube.R

/**
 * Vista de texto personalizada que muestra la duración de una receta en un formato legible.
 *
 * Muestra la duración en formato de horas y minutos, mostrando solo las horas si los minutos son cero,
 * solo los minutos si es menos de una hora, o ambos si es necesario.
 *
 * @property context Contexto de la aplicación
 * @property attrs Atributos XML personalizados
 * @property defStyleAttr Estilo por defecto
 */
class DurationTextView  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): AppCompatTextView(context, attrs, defStyleAttr) {

    /** Duración actual en minutos */
    private var currentMinutes = 0
    
    /** Duración actual en horas (calculada a partir de los minutos) */
    private var currentHours = 0

    init {
        setDuration(currentMinutes)
        setTextColor(context.getColor(R.color.white))
        setPadding(10)
        textSize = 18f
    }

    /**
     * Establece la duración a mostrar.
     *
     * Actualiza el texto mostrado según la duración proporcionada en minutos.
     * El formato del texto dependerá de si la duración es menor a una hora, exactamente
     * en horas, o una combinación de horas y minutos.
     *
     * @param minutes Duración en minutos (debe ser un valor no negativo)
     */
    fun setDuration(minutes: Int) {
        currentHours = minutes / 60
        currentMinutes = minutes % 60
        text = if (currentHours > 0){
            if (currentMinutes > 0){
                context.getString(R.string.duration_hours_minutes, currentHours, currentMinutes )
            }else{
                context.getString(R.string.duration_hours, currentHours )
            }
        }else{
            context.getString(R.string.duration_minutes, currentMinutes)
        }
    }
}