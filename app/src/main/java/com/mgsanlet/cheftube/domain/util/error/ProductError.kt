package com.mgsanlet.cheftube.domain.util.error

/**
 * Clase sellada que representa los posibles errores relacionados con productos.
 * Hereda de [DomainError] para integrarse con el sistema de manejo de errores del dominio.
 */
sealed class ProductError: DomainError {
    /** Error que indica que no hay conexión a Internet disponible. */
    data object NoInternet: ProductError()
    
    /** Error que indica que la respuesta del servidor está vacía. */
    data object EmptyResponse: ProductError()
    
    /** Error que indica que el producto no fue encontrado. */
    data object NotFound: ProductError()
    
    /**
     * Error que indica un problema con la API de productos.
     *
     * @property messageArg Detalles adicionales sobre el error de la API.
     */
    data class ApiError(val messageArg: Any? = ""): ProductError()
    
    /**
     * Error genérico para errores inesperados relacionados con productos.
     *
     * @property messageArg Mensaje opcional que puede contener detalles adicionales sobre el error.
     */
    data class Unknown(val messageArg: Any? = ""): ProductError()
}