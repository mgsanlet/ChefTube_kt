package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.util.Constants.PASSWORD_MIN_LENGTH
import com.mgsanlet.cheftube.domain.util.Constants.PASSWORD_REGEX
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.PatternValidator
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

/**
 * Caso de uso para validar una nueva contraseña.
 * Verifica que la contraseña cumpla con los requisitos mínimos de seguridad:
 * - Longitud mínima definida en [PASSWORD_MIN_LENGTH]
 * - Formato que cumpla con la expresión regular [PASSWORD_REGEX]
 *
 * @property validator Validador de patrones para verificar el formato de la contraseña
 */
class ValidateNewPasswordUseCase @Inject constructor(private val validator: PatternValidator) {
    /**
     * Ejecuta la validación de la contraseña.
     *
     * @param newPassword Contraseña a validar
     * @return [DomainResult] con Unit si la contraseña es válida o [UserError]
     * si no cumple los requisitos
     * @throws Exception Si ocurre un error durante la validación
     */
    operator fun invoke(newPassword: String): DomainResult<Unit, UserError> {
        return try {
            validator.isValidPasswordPattern(
                newPassword, PASSWORD_MIN_LENGTH, Regex(PASSWORD_REGEX)
            )
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }
}