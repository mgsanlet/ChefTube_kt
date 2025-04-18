package com.mgsanlet.cheftube.utils.error

import androidx.annotation.StringRes
import com.mgsanlet.cheftube.R

sealed class UserError(
@StringRes override val messageRes: Int
) : ChefTubeError(messageRes) {
    object UsernameAlreadyInUse : UserError(R.string.username_already)
    object EmailAlreadyInUse : UserError(R.string.email_already)
    object InvalidLogin : UserError(R.string.invalid_login)
    object WrongPassword : UserError(R.string.wrong_pwd)
    object UserNotFound : UserError(R.string.user_not_found)
}