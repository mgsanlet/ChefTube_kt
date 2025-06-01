package com.mgsanlet.cheftube.domain.util

import com.mgsanlet.cheftube.domain.util.error.DomainError

/**
 * Alias para el tipo base de errores del dominio.
 * Representa el tipo raíz del que deben heredar todos los errores específicos del dominio.
 */
typealias RootError = DomainError

/**
 * Clase sellada que representa el resultado de una operación de dominio.
 *
 * @param D Tipo de datos de éxito
 * @param E Tipo de error que hereda de [RootError]
 */
sealed class DomainResult<out D, out E : RootError> {
    /**
     * Representa un resultado exitoso que contiene datos.
     *
     * @property data Datos resultantes de la operación exitosa
     */
    data class Success<out D, out E : RootError>(val data: D) : DomainResult<D, E>()

    /**
     * Representa un resultado fallido que contiene un error.
     *
     * @property error Error que ocurrió durante la operación
     */
    data class Error<out D, out E : RootError>(val error: E) : DomainResult<D, E>()

    /**
     * Aplica una función diferente según si el resultado es un éxito o un error.
     *
     * @param onSuccess Función a ejecutar si el resultado es exitoso
     * @param onError Función a ejecutar si el resultado es un error
     * @return Resultado de aplicar la función correspondiente
     */
    inline fun <R : Any> fold(
        onSuccess: (D) -> R,
        onError: (E) -> R
    ): R {
        return when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(error)
        }
    }
}