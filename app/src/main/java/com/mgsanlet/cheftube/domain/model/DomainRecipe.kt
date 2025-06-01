package com.mgsanlet.cheftube.domain.model

/**
 * Clase de dominio que representa una receta en la aplicación.
 *
 * @property id Identificador único de la receta
 * @property title Título de la receta
 * @property imageUrl URL de la imagen principal de la receta
 * @property videoUrl URL del video de la receta (opcional)
 * @property ingredients Lista de ingredientes necesarios
 * @property steps Lista de pasos para preparar la receta
 * @property categories Categorías a las que pertenece la receta
 * @property comments Lista de comentarios sobre la receta
 * @property favouriteCount Número de veces que la receta ha sido marcada como favorita
 * @property durationMinutes Tiempo de preparación en minutos
 * @property difficulty Nivel de dificultad (0-2)
 * @property author Usuario que creó la receta
 */
data class DomainRecipe(
    val id: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val videoUrl: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val comments: List<DomainComment> = emptyList(),
    val favouriteCount: Int = 0,
    val durationMinutes: Int = 0,
    val difficulty: Int = 0,
    val author: DomainUser? = null
)