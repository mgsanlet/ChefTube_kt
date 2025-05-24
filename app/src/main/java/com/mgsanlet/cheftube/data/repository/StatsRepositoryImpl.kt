package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.model.StatsResponse
import com.mgsanlet.cheftube.data.source.remote.FirebaseApi
import com.mgsanlet.cheftube.domain.model.DomainStats
import com.mgsanlet.cheftube.domain.repository.StatsRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.StatsError
import javax.inject.Inject

class StatsRepositoryImpl @Inject constructor(
    private val api: FirebaseApi
) : StatsRepository {

    override suspend fun getStats(): DomainResult<DomainStats, StatsError> {
        return try {
            // Obtener estadísticas de Firebase
            api.getStats().fold(
                onSuccess = { stats -> DomainResult.Success(stats.toDomain()) },
                onError = { error -> DomainResult.Error(error) }
            )
        } catch (e: Exception) {
            DomainResult.Error(StatsError.Unknown(e.message))
        }
    }
}

private fun StatsResponse.toDomain() = DomainStats(
    loginTimestamps = this.loginTimestamps,
    interactionTimestamps = this.interactionTimestamps,
    scanTimestamps = this.scanTimestamps
)