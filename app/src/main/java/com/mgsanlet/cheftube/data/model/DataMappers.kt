package com.mgsanlet.cheftube.data.model

import com.mgsanlet.cheftube.domain.model.DomainProduct
import com.mgsanlet.cheftube.domain.model.DomainUser

fun ProductResponse.toDomainProduct(): DomainProduct {
    val defaultName = product!!.product_name ?: "Unknown Product"

    return DomainProduct(
        barcode = product.code,
        englishName = product.product_name_en ?: defaultName,
        italianName = product.product_name_it ?: defaultName,
        spanishName = product.product_name_es ?: defaultName
    )
}

fun User.toDomainUser(): DomainUser {
    return DomainUser(
        id = id,
        username = username,
        email = email,
        password = passwordHash
    )
}
