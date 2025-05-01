package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.util.Constants.USERNAME_MAX_LENGTH
import com.mgsanlet.cheftube.domain.util.Constants.USERNAME_MIN_LENGTH
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.PatternValidator
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

class ValidateNewUsernameUseCase @Inject constructor(private val validator: PatternValidator) {
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