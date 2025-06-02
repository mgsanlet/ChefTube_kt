package com.mgsanlet.cheftube.ui.util

import android.content.Context
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.domain.util.Constants.USERNAME_MAX_LENGTH
import com.mgsanlet.cheftube.domain.util.Constants.USERNAME_MIN_LENGTH
import com.mgsanlet.cheftube.domain.util.error.ProductError
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import com.mgsanlet.cheftube.domain.util.error.StatsError
import com.mgsanlet.cheftube.domain.util.error.UserError

/**
 * Extensión para convertir un error de usuario en un mensaje legible para el usuario.
 *
 * @param context Contexto de la aplicación para acceder a los recursos de cadena
 * @return Mensaje de error traducido y formateado
 */
fun UserError.asMessage(context: Context): String {
    return when (this) {
        is UserError.UsernameInUse -> context.getString(R.string.username_already)
        is UserError.EmailInUse -> context.getString(R.string.email_already)
        is UserError.UserNotFound -> context.getString(R.string.user_not_found)
        is UserError.WrongCredentials -> context.getString(R.string.wrong_credentials)
        is UserError.Unknown -> context.getString(R.string.unknown_error, this.messageArg)
        is UserError.InvalidEmailPattern -> context.getString(R.string.invalid_email)
        is UserError.PasswordTooShort -> context.getString(R.string.short_pwd)
        is UserError.InvalidPasswordPattern -> context.getString(R.string.pwd_pattern)
        UserError.InvalidUsernamePattern -> context.getString(
            R.string.invalid_username_length,
            USERNAME_MIN_LENGTH,
            USERNAME_MAX_LENGTH
        )
    }
}

/**
 * Extensión para convertir un error de receta en un mensaje legible para el usuario.
 *
 * @param context Contexto de la aplicación para acceder a los recursos de cadena
 * @return Mensaje de error traducido y formateado
 */
fun RecipeError.asMessage(context: Context): String {
    return when (this) {
        is RecipeError.NoResults -> context.getString(R.string.no_results)
        is RecipeError.Unknown -> context.getString(R.string.unknown_error, this.messageArg)
        is RecipeError.RecipeNotFound -> context.getString(R.string.recipe_not_found_error)
        is RecipeError.CommentNotFound -> context.getString(R.string.error_comment_not_found)
    }
}

/**
 * Extensión para convertir un error de producto en un mensaje legible para el usuario.
 *
 * @param context Contexto de la aplicación para acceder a los recursos de cadena
 * @return Mensaje de error traducido y formateado
 */
fun ProductError.asMessage(context: Context): String {
    return when (this) {
        is ProductError.NoInternet -> context.getString(R.string.no_internet)
        is ProductError.EmptyResponse -> context.getString(R.string.empty_product_response_error)
        is ProductError.NotFound -> context.getString(R.string.product_not_found_error)
        is ProductError.ApiError -> context.getString(R.string.api_error, this.messageArg)
        is ProductError.Unknown -> context.getString(R.string.unknown_error, this.messageArg)
    }
}

/**
 * Extensión para convertir un error de estadísticas en un mensaje legible para el usuario.
 *
 * @param context Contexto de la aplicación para acceder a los recursos de cadena
 * @return Mensaje de error traducido y formateado
 */
fun StatsError.asMessage(context: Context): String {
    return when (this) {
        is StatsError.StatsNotFound -> context.getString(R.string.stats_not_found)
        is StatsError.Unknown -> context.getString(R.string.unknown_error, this.messageArg)
    }
}

