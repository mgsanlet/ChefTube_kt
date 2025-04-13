package com.mgsanlet.cheftube.data.model


data class ProductResponse(
    val code: String,
    val status: Int,
    val product: Product
)


data class Product(
    val product_name: String?,
    val product_name_en: String?,
    val product_name_it: String?,
    val product_name_es: String?
)
