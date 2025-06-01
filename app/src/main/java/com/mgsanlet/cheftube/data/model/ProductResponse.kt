package com.mgsanlet.cheftube.data.model

import com.google.gson.annotations.SerializedName

/**
 * Respuesta de la API de Open Food Facts que contiene la información de un producto.
 *
 * @property status Estado de la respuesta (ej: "success", "product not found")
 * @property product Datos del producto, o null si no se encontró
 */
data class ProductResponse(
    val status: String,
    val product: Product?
)

/**
 * Representa un producto alimenticio obtenido de Open Food Facts.
 *
 * @property code Código de barras del producto
 * @property name Nombre del producto en el idioma principal
 * @property nameEn Nombre del producto en inglés (opcional)
 * @property nameIt Nombre del producto en italiano (opcional)
 * @property nameEs Nombre del producto en español (opcional)
 */
data class Product(
    val code: String,
    
    @SerializedName("product_name")
    val name: String?,
    
    @SerializedName("product_name_en")
    val nameEn: String?,
    
    @SerializedName("product_name_it")
    val nameIt: String?,
    
    @SerializedName("product_name_es")
    val nameEs: String?
)
