package com.mgsanlet.cheftube.domain.util

import com.mgsanlet.cheftube.domain.util.error.UserError

/**
 * Interfaz que define los métodos para validar patrones comunes en la aplicación.
 * Se utiliza para validar formatos de correo electrónico, contraseñas y nombres de usuario.
 */
interface PatternValidator {
    /**
     * Valida si una dirección de correo electrónico tiene un formato válido.
     *
     * @param email Dirección de correo electrónico a validar
     * @return [DomainResult] con Unit si el correo es válido, o un error [UserError] si no lo es
     */
    fun isValidEmailPattern(email: String): DomainResult<Unit, UserError>
    /**
     * Valida si una contraseña cumple con los requisitos mínimos.
     *
     * @param password Contraseña a validar
     * @param minLength Longitud mínima requerida para la contraseña
     * @param regex Expresión regular que debe cumplir la contraseña
     * @return [DomainResult] con Unit si la contraseña es válida, o un error [UserError] si no lo es
     */
    fun isValidPasswordPattern(password: String, minLength: Int, regex: Regex): DomainResult<Unit, UserError>
    /**
     * Valida si un nombre de usuario cumple con los requisitos de formato.
     *
     * @param username Nombre de usuario a validar
     * @param minLength Longitud mínima permitida
     * @param maxLength Longitud máxima permitida
     * @return [DomainResult] con Unit si el nombre de usuario es válido, o un error [UserError] si no lo es
     */
    fun isValidUsernamePattern(username: String, minLength: Int, maxLength: Int): DomainResult<Unit, UserError>
}