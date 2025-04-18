package com.mgsanlet.cheftube.utils.error

import androidx.annotation.StringRes
import com.mgsanlet.cheftube.R

sealed class ProductError(
    @StringRes override val messageRes: Int
) : ChefTubeError(messageRes) {
    object NotFound : ProductError(R.string.product_not_found_error)
    object EmptyResponse : ProductError(R.string.empty_product_response_error)

}
