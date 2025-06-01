package com.mgsanlet.cheftube.domain.util

/**
 * Objeto que contiene constantes utilizadas en toda la aplicación.
 */
object Constants {
    /** Longitud mínima requerida para una contraseña. */
    const val PASSWORD_MIN_LENGTH = 6
    
    /** Longitud mínima permitida para un nombre de usuario. */
    const val USERNAME_MIN_LENGTH = 4
    
    /** Longitud máxima permitida para un nombre de usuario. */
    const val USERNAME_MAX_LENGTH = 16
    
    /** 
     * Expresión regular para validar contraseñas.
     * Requiere al menos una letra y un número.
     */
    const val PASSWORD_REGEX = "^(?=.*[a-zA-Z])(?=.*\\d).+$"
    
    /** Correo electrónico de soporte de la aplicación. */
    const val SUPPORT_EMAIL = "support@cheftube.com"
}

/**
 * Enumeración que representa los diferentes criterios de filtrado disponibles
 * para las recetas en la aplicación.
 */
enum class FilterCriterion {
    /** Filtra por título de la receta. */
    TITLE,
    
    /** Filtra por ingredientes de la receta. */
    INGREDIENT,
    
    /** Filtra por duración de preparación de la receta. */
    DURATION,
    
    /** Filtra por categoría de la receta. */
    CATEGORY,
    
    /** Filtra por nivel de dificultad de la receta. */
    DIFFICULTY
}