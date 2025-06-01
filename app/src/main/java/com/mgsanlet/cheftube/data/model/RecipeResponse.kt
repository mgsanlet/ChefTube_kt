package com.mgsanlet.cheftube.data.model

/**
 * Representa una receta en la base de datos.
 *
 * @property id Identificador único de la receta
 * @property title Título de la receta
 * @property videoUrl URL del video de la receta
 * @property ingredients Lista de ingredientes necesarios
 * @property steps Lista de pasos para preparar la receta
 * @property categories Categorías a las que pertenece la receta
 * @property comments Lista de comentarios sobre la receta
 * @property favouriteCount Número de veces que la receta ha sido marcada como favorita
 * @property durationMinutes Tiempo de preparación en minutos
 * @property difficulty Nivel de dificultad (0-2)
 * @property authorId Identificador único del autor de la receta
 * @property authorEmail Correo electrónico del autor
 * @property authorName Nombre para mostrar del autor
 * @property authorHasProfilePicture Indica si el autor tiene una imagen de perfil
 */
data class RecipeResponse(
    val id: String = "",
    val title: String = "",
    val videoUrl: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val comments: List<CommentResponse> = emptyList(),
    val favouriteCount: Int = 0,
    val durationMinutes: Int = 0,
    val difficulty: Int = 0,
    val authorId: String = "",
    val authorEmail: String = "",
    val authorName: String = "",
    val authorHasProfilePicture: Boolean = false
)
