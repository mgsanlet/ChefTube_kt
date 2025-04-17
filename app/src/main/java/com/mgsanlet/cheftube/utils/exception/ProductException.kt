package com.mgsanlet.cheftube.utils.exception

import androidx.annotation.StringRes
import com.mgsanlet.cheftube.R

sealed class ProductException(
    @StringRes override val messageRes: Int
) : ChefTubeException(messageRes) {
    object NotFound : ProductException(R.string.product_not_found_error)
    object EmptyResponse : ProductException(R.string.empty_product_response_error)

}
