package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.util.PatternValidator
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

class ValidateNewEmailUseCase @Inject constructor(private val validator: PatternValidator) {
    operator fun invoke(newEmail: String): DomainResult<Unit, UserError> {
        return try {
            validator.isValidEmailPattern(newEmail)
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }
}