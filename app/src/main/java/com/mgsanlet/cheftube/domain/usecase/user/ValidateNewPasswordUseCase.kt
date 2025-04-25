package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.util.Constants.PASSWORD_MIN_LENGTH
import com.mgsanlet.cheftube.domain.util.Constants.PASSWORD_REGEX
import com.mgsanlet.cheftube.domain.util.PatternValidator
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

class ValidateNewPasswordUseCase @Inject constructor(private val validator: PatternValidator) {
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