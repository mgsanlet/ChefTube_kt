package com.mgsanlet.cheftube.domain.util.error

sealed class UserError: DomainError {
    data object UsernameInUse: UserError()
    data object EmailInUse: UserError()
    data object UserNotFound: UserError()
    data object WrongPassword: UserError()
    data object PasswordTooShort: UserError()
    data object InvalidPasswordPattern: UserError()
    data object InvalidEmailPattern: UserError()
    data object InvalidUsernamePattern: UserError()
    data class Unknown(val messageArg:Any? = ""): UserError()
}