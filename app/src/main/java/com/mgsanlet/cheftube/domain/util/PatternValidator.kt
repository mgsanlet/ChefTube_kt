package com.mgsanlet.cheftube.domain.util

import com.mgsanlet.cheftube.domain.util.error.UserError

interface PatternValidator {
    fun isValidEmailPattern(email: String): DomainResult<Unit, UserError>
    fun isValidPasswordPattern(password: String, minLength: Int, regex: Regex): DomainResult<Unit, UserError>
}