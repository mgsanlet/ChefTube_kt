package com.mgsanlet.cheftube.data.model

import android.content.Context
import java.util.Locale

/**
 * Representa una rectea, incluyendo detalles como título, imagen, ingredientes,
 * pasos de preparación, y una URL de video.
 * @author MarioG
 */
data class RecipeDto(val id: String, val ttlRId: Int, val imgRId: Int, val videoUrl: String) {

    private val ingredientsResIds: MutableList<Int> = ArrayList()
    private val stepsResIds: MutableList<Int> = ArrayList()

    fun getIngredientsResIds(): List<Int> {
        return ingredientsResIds
    }

    fun getStepsResIds(): List<Int> {
        return stepsResIds
    }

    fun addIngredient(ingrRId: Int) {
        ingredientsResIds.add(ingrRId)
    }

    fun addStep(stepRId: Int) {
        stepsResIds.add(stepRId)
    }

    /**
     * Determina si la receta coincide con un string de consulta.
     * La búsqueda comprueba los ingredientes para encontrar coincidencias y es case-insensitive.
     *
     * @param context el contexto de Android, usado para acceder a los recursos
     * @param query   la consulta de búsqueda
     * @return true si la consulta coincide con algún ingrediente
     */
    fun matchesIngredientQuery(context: Context, query: String): Boolean {
        // Convertir la consulta a minúsculas para una comparación case-insensitive
        val lowerCaseQuery = query.lowercase(Locale.getDefault())

        // Comprobar si algún ingrediente coincide con la consulta
        for (ingredientId in ingredientsResIds) {
            val ingredientName = context.resources.getString(ingredientId)
            if (ingredientName.lowercase(Locale.getDefault()).contains(lowerCaseQuery)) {
                return true
            }
        }
        return false
    }
}
