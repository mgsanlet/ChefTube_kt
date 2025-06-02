package com.mgsanlet.cheftube.ui.util

/**
 * Objeto que contiene constantes utilizadas en la capa de UI de la aplicación.
 * Agrupa valores estáticos que se utilizan en múltiples componentes de la interfaz de usuario.
 */
object Constants {
    /**
     * Esquema URI para abrir la aplicación de correo electrónico.
     */
    const val URI_MAIL_TO_SCHEME = "mailto"

    /**
     * Clave para pasar el ID de una receta como argumento en un Bundle o NavArgs.
     */
    const val ARG_RECIPE = "recipeId"

    /**
     * Clave para pasar una lista de IDs de recetas como argumento en un Bundle o NavArgs.
     */
    const val ARG_RECIPE_LIST = "recipeIds"

    /**
     * Clave para pasar el ID de un usuario como argumento en un Bundle o NavArgs.
     */
    const val ARG_USER_ID = "userId"

    /**
     * Expresión regular para validar IDs de YouTube.
     * Los IDs de YouTube deben tener exactamente 11 caracteres alfanuméricos, guiones bajos o guiones medios.
     */
    const val YOUTUBE_ID_REGEX = "^[a-zA-Z0-9_-]{11}$"
}