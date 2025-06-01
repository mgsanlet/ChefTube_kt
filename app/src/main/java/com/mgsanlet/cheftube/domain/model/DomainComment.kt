package com.mgsanlet.cheftube.domain.model

/**
 * Clase de dominio que representa un comentario en una receta.
 *
 * @property author Usuario que realizó el comentario
 * @property content Contenido textual del comentario
 * @property timestamp Marca de tiempo en milisegundos
 */
data class DomainComment(
    val author: DomainUser = DomainUser(),
    val content: String = "",
    val timestamp: Long = 0
)
