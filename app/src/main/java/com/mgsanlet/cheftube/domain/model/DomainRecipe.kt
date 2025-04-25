package com.mgsanlet.cheftube.domain.model

/**
 * Representa una rectea, incluyendo detalles como título, imagen, ingredientes,
 * pasos de preparación, y una URL de video.
 * @author MarioG
 */
data class DomainRecipe(
    val id: String,
    val ttlRId: Int,
    val imgRId: Int,
    val videoUrl: String,
    val ingredientsResIds: MutableList<Int> = ArrayList(),
    val stepsResIds: MutableList<Int> = ArrayList()
)