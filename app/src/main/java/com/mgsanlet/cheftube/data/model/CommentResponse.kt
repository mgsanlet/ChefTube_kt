package com.mgsanlet.cheftube.data.model

/**
 * Representa un comentario en la base de datos.
 *
 * @property authorId Identificador único del autor del comentario
 * @property authorName Nombre para mostrar del autor
 * @property authorEmail Correo electrónico del autor
 * @property authorHasProfilePicture Indica si el autor tiene una imagen de perfil
 * @property content Contenido textual del comentario
 * @property timestamp Marca de tiempo en milisegundos
 */
data class CommentResponse (
    val authorId: String = "",
    val authorName: String = "",
    val authorEmail: String = "",
    val authorHasProfilePicture: Boolean = false,
    val content: String = "",
    val timestamp: Long = 0
)