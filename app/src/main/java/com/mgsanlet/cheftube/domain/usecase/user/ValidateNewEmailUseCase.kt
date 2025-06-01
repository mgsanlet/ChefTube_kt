package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.PatternValidator
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

/**
 * Caso de uso para validar un nuevo correo electrónico.
 * Verifica que el formato del correo sea válido según un patrón predefinido.
 *
 * @property validator Validador de patrones para verificar el formato del correo
 */
class ValidateNewEmailUseCase @Inject constructor(private val validator: PatternValidator) {
    /**
     * Ejecuta la validación del correo electrónico.
     *
     * @param newEmail Correo electrónico a validar
     * @return [DomainResult] con Unit si el correo es válido o [UserError] si es inválido
     * @throws Exception Si ocurre un error durante la validación
     */
    operator fun invoke(newEmail: String): DomainResult<Unit, UserError> {
        return try {
            validator.isValidEmailPattern(newEmail)
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }
}