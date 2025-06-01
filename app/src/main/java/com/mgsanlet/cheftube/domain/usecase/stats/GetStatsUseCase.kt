package com.mgsanlet.cheftube.domain.usecase.stats

import com.mgsanlet.cheftube.domain.model.DomainStats
import com.mgsanlet.cheftube.domain.repository.StatsRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.StatsError
import javax.inject.Inject

/**
 * Caso de uso para obtener las estadísticas generales de la aplicación.
 * Incluye métricas como número total de usuarios, recetas, etc.
 *
 * @property statsRepository Repositorio de estadísticas para obtener los datos
 */
class GetStatsUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    /**
     * Ejecuta el caso de uso para obtener las estadísticas.
     *
     * @return [DomainResult] con las estadísticas en un objeto [DomainStats] o [StatsError] si hay un error
     */
    suspend operator fun invoke(): DomainResult<DomainStats, StatsError> = statsRepository.getStats()
}