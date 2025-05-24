package com.mgsanlet.cheftube.domain.repository

import com.mgsanlet.cheftube.domain.model.DomainStats
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.StatsError

interface StatsRepository {
    suspend fun getStats(): DomainResult<DomainStats, StatsError>
}