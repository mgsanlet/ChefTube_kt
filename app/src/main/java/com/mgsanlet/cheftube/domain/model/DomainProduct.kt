package com.mgsanlet.cheftube.domain.model

/**
 * Clase de dominio que representa un producto alimenticio.
 *
 * @property barcode Código de barras del producto
 * @property englishName Nombre del producto en inglés
 * @property italianName Nombre del producto en italiano
 * @property spanishName Nombre del producto en español
 */
data class DomainProduct(
    val barcode: String,
    val englishName: String,
    val italianName: String,
    val spanishName: String
)
