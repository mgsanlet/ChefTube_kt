package com.mgsanlet.cheftube.data.util

import android.util.Patterns
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.PatternValidator
import com.mgsanlet.cheftube.domain.util.error.UserError

class PatternValidatorImpl : PatternValidator {
    override fun isValidEmailPattern(email: String): DomainResult<Unit, UserError> {
        return if (Patterns.EMAIL_ADDRESS.matcher(email).matches())
            DomainResult.Success(Unit)
        else
            DomainResult.Error(UserError.InvalidEmailPattern)
    }

    override fun isValidPasswordPattern(
        password: String,
        minLength: Int,
        regex: Regex
    ): DomainResult<Unit, UserError> {
        return if (password.length < minLength) DomainResult.Error(UserError.PasswordTooShort)
        else if (!password.matches(regex)) DomainResult.Error(UserError.InvalidPasswordPattern)
        else DomainResult.Success(Unit)
    }

    override fun isValidUsernamePattern(
        username: String,
        minLength: Int,
        maxLength: Int
    ): DomainResult<Unit, UserError> {
        return  if (username.length < minLength || username.length > maxLength) {
            DomainResult.Error(UserError.InvalidUsernamePattern)
        } else {
            DomainResult.Success(Unit)
        }
    }
}