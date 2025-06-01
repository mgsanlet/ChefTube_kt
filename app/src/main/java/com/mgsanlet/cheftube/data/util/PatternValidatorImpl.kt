package com.mgsanlet.cheftube.data.util

import android.util.Patterns
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.PatternValidator
import com.mgsanlet.cheftube.domain.util.error.UserError

/**
 * Implementación de la interfaz PatternValidator que proporciona métodos para validar patrones de email, contraseña y nombre de usuario.
 */
class PatternValidatorImpl : PatternValidator {
    /**
     * Valida si una dirección de email tiene un formato correcto.
     *
     * @param email La dirección de email a validar
     * @return [DomainResult.Success] si el email es válido,
     *         DomainResult.Error con UserError.InvalidEmailPattern si el formato es inválido
     */
    override fun isValidEmailPattern(email: String): DomainResult<Unit, UserError> {
        return if (Patterns.EMAIL_ADDRESS.matcher(email).matches())
            DomainResult.Success(Unit)
        else
            DomainResult.Error(UserError.InvalidEmailPattern)
    }

    /**
     * Valida si una contraseña cumple con los requisitos de seguridad.
     * Verifica tanto la longitud mínima como el patrón de la contraseña.
     *
     * @param password La contraseña a validar
     * @param minLength Longitud mínima requerida para la contraseña
     * @param regex Expresión regular que define el patrón que debe cumplir la contraseña
     * @return [DomainResult.Success] si la contraseña es válida,
     *         DomainResult.Error con UserError.PasswordTooShort si es demasiado corta,
     *         o UserError.InvalidPasswordPattern si no cumple el patrón requerido
     */
    override fun isValidPasswordPattern(
        password: String,
        minLength: Int,
        regex: Regex
    ): DomainResult<Unit, UserError> {
        return if (password.length < minLength) DomainResult.Error(UserError.PasswordTooShort)
        else if (!password.matches(regex)) DomainResult.Error(UserError.InvalidPasswordPattern)
        else DomainResult.Success(Unit)
    }

    /**
     * Valida si un nombre de usuario cumple con los requisitos de longitud.
     * Verifica que la longitud del nombre de usuario esté dentro del rango especificado.
     *
     * @param username El nombre de usuario a validar
     * @param minLength Longitud mínima permitida
     * @param maxLength Longitud máxima permitida
     * @return [DomainResult.Success] si el nombre de usuario es válido,
     *         DomainResult.Error con UserError.InvalidUsernamePattern si está fuera de los límites
     */
    override fun isValidUsernamePattern(
        username: String,
        minLength: Int,
        maxLength: Int
    ): DomainResult<Unit, UserError> {
        return if (username.length < minLength || username.length > maxLength) {
            DomainResult.Error(UserError.InvalidUsernamePattern)
        } else {
            DomainResult.Success(Unit)
        }
    }
}