package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.util.Constants.USERNAME_MAX_LENGTH
import com.mgsanlet.cheftube.domain.util.Constants.USERNAME_MIN_LENGTH
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.PatternValidator
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

/**
 * Caso de uso para validar un nuevo nombre de usuario.
 * Verifica que el nombre cumpla con los requisitos de longitud y formato.
 *
 * @property validator Validador de patrones para verificar el formato del nombre de usuario
 */
class ValidateNewUsernameUseCase @Inject constructor(private val validator: PatternValidator) {
    /**
     * Ejecuta la validación del nombre de usuario.
     *
     * @param newUsername Nombre de usuario a validar
     * @return [DomainResult] con Unit si el nombre es válido o [UserError] si no cumple
     * los requisitos
     * @throws Exception Si ocurre un error durante la validación
     */
    operator fun invoke(newUsername: String): DomainResult<Unit, UserError> {
        return try {
            validator.isValidUsernamePattern(
                newUsername, USERNAME_MIN_LENGTH, USERNAME_MAX_LENGTH
            )
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }
}