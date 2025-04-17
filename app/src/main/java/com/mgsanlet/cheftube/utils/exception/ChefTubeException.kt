package com.mgsanlet.cheftube.utils.exception

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import com.mgsanlet.cheftube.R

open class ChefTubeException(
    @StringRes open val messageRes: Int
) : Exception() {

    class ApiError(val statusCode: Int) : ChefTubeException(R.string.api_error)
    class UnknownError(message: String?) : ChefTubeException(R.string.unknown_error)
    object NoInternet : ChefTubeException(R.string.no_internet)

    fun getLocalizedMessage(context: Context): String {
        return try {
            when (this) {
                is ApiError -> context.getString(R.string.api_error, statusCode)
                is UnknownError -> context.getString(R.string.unknown_error, message)
                else -> context.getString(messageRes)
            }

        } catch (e: Resources.NotFoundException) {
            context.getString(R.string.non_translated_error)
        }
    }
}