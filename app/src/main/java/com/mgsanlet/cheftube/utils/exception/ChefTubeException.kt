package com.mgsanlet.cheftube.utils.exception

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import com.mgsanlet.cheftube.R

open class ChefTubeException(
    @StringRes open val messageRes: Int
) : Exception() {

    class ApiError(val statusCode: Int) : ProductException(R.string.api_error)
    object UnknownError : ProductException(R.string.unknown_error)

    fun getLocalizedMessage(context: Context): String {
        return try {
            when (this) {
                is ApiError -> context.getString(R.string.api_error, statusCode)
                else -> context.getString(messageRes)
            }

        } catch (e: Resources.NotFoundException) {
            context.getString(R.string.non_translated_error)
        }
    }
}