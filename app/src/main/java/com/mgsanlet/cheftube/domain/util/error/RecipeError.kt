package com.mgsanlet.cheftube.domain.util.error

/**
 * Clase sellada que representa los posibles errores relacionados con recetas.
 * Hereda de [DomainError] para integrarse con el sistema de manejo de errores del dominio.
 */
sealed class RecipeError: DomainError {
    /** Error que indica que la receta solicitada no fue encontrada. */
    data object RecipeNotFound: RecipeError()
    
    /** Error que indica que el comentario solicitado no fue encontrado. */
    data object CommentNotFound: RecipeError()
    
    /** Error que indica que no se encontraron resultados para la búsqueda realizada. */
    data object NoResults: RecipeError()
    
    /**
     * Error genérico para errores inesperados relacionados con recetas.
     *
     * @property messageArg Mensaje opcional que puede contener detalles adicionales sobre el error.
     */
    data class Unknown(val messageArg: Any? = ""): RecipeError()
}