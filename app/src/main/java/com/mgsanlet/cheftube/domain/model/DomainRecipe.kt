package com.mgsanlet.cheftube.domain.model

/**
 * Representa una rectea, incluyendo detalles como título, imagen, ingredientes,
 * pasos de preparación, y una URL de video.
 * @author MarioG
 */
data class DomainRecipe(
    val id: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val videoUrl: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val author: DomainUser? = null,
    val favouriteCount: Int = 0
)