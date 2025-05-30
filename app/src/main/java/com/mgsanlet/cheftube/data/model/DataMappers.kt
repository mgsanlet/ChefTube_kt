package com.mgsanlet.cheftube.data.model

import com.mgsanlet.cheftube.domain.model.DomainProduct

fun ProductResponse.toDomainProduct(): DomainProduct {
    val defaultName = product!!.product_name ?: "Unknown Product"

    return DomainProduct(
        barcode = product.code,
        englishName = product.product_name_en ?: defaultName,
        italianName = product.product_name_it ?: defaultName,
        spanishName = product.product_name_es ?: defaultName
    )
}

//fun RecipeResponse.toDomainRecipe(): DomainRecipe {
//    return DomainRecipe(
//        id = id,
//        title = title,
//        imageUrl = imageUrl,
//        videoUrl = videoUrl,
//        ingredients = ingredients,
//        steps = steps
//    )
//}
