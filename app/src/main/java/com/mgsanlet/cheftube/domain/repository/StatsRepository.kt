package com.mgsanlet.cheftube.domain.repository

import com.mgsanlet.cheftube.domain.model.DomainStats
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.StatsError

/**
 * Interfaz que define las operaciones disponibles para la gestión de estadísticas.
 * Todas las operaciones son suspendidas para permitir operaciones asíncronas.
 */
interface StatsRepository {
    /**
     * Obtiene las estadísticas de uso de la aplicación.
     *
     * @return [DomainResult] con las estadísticas o error
     */
    suspend fun getStats(): DomainResult<DomainStats, StatsError>
}