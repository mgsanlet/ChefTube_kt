package com.mgsanlet.cheftube.domain.usecase.stats

import com.mgsanlet.cheftube.domain.model.DomainStats
import com.mgsanlet.cheftube.domain.repository.StatsRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.StatsError
import javax.inject.Inject

class GetStatsUseCase @Inject constructor(private val statsRepository: StatsRepository) {
    suspend operator fun invoke(): DomainResult<DomainStats, StatsError> = statsRepository.getStats()
}