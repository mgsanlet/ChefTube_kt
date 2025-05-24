package com.mgsanlet.cheftube.domain.util.error

/**
 * Errores específicos del dominio de estadísticas
 */
sealed class StatsError : DomainError {
    object StatsNotFound : StatsError()
    data class Unknown(val messageArg: Any? = null) : StatsError()
}
