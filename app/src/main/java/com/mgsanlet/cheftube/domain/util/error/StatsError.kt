package com.mgsanlet.cheftube.domain.util.error

/**
 * Clase sellada que representa los posibles errores relacionados con las estadísticas.
 * Hereda de [DomainError] para integrarse con el sistema de manejo de errores del dominio.
 */
sealed class StatsError : DomainError {
    /** Error que indica que no se encontraron estadísticas disponibles. */
    data object StatsNotFound : StatsError()
    
    /**
     * Error genérico para errores inesperados relacionados con estadísticas.
     *
     * @property messageArg Mensaje opcional que puede contener detalles adicionales sobre el error.
     */
    data class Unknown(val messageArg: Any? = null) : StatsError()
}
