package com.mgsanlet.cheftube.ui.util

import android.content.Context
import androidx.annotation.StringRes
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.domain.repository.ProductRepository.ProductError
import com.mgsanlet.cheftube.domain.repository.UserRepository.UserError

class UiText(
    @StringRes val id: Int,
    val args: Array<Any> = arrayOf()
) {
    fun asString(context: Context): String {
        return context.getString(id, *args)
    }
}

fun UserError.asUiText(): UiText {
    return when (this) {
        UserError.USERNAME_IN_USE -> UiText(R.string.username_already)
        UserError.EMAIL_IN_USE -> UiText(R.string.email_already)
        UserError.USER_NOT_FOUND -> UiText(R.string.user_not_found)
        UserError.WRONG_PASSWORD -> UiText(R.string.wrong_password)
        UserError.UNKNOWN -> UiText(R.string.unknown_error)
    }
}

fun ProductError.asUiText(): UiText {
    return when (this) {
        ProductError.NO_INTERNET -> UiText(R.string.no_internet)
        ProductError.EMPTY_RESPONSE -> UiText(R.string.empty_product_response_error)
        ProductError.NOT_FOUND -> UiText(R.string.product_not_found_error)
        ProductError.API_ERROR -> UiText(R.string.api_error)
        ProductError.UNKNOWN -> UiText(R.string.unknown_error)
    }
}
