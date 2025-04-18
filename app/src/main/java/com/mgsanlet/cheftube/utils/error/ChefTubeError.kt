package com.mgsanlet.cheftube.utils.error

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import com.mgsanlet.cheftube.R

open class ChefTubeError(
    @StringRes open val messageRes: Int
) {

    class ApiError(val statusCode: Int) : ChefTubeError(R.string.api_error)
    class UnknownError(val description: String?) : ChefTubeError(R.string.unknown_error)
    object NoInternet : ChefTubeError(R.string.no_internet)

    fun getLocalizedMessage(context: Context): String {
        return try {
            when (this) {
                is ApiError -> context.getString(R.string.api_error, statusCode)
                is UnknownError -> context.getString(R.string.unknown_error, description)
                else -> context.getString(messageRes)
            }

        } catch (e: Resources.NotFoundException) {
            context.getString(R.string.non_translated_error)
        }
    }
}