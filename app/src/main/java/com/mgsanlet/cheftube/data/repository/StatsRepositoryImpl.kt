package com.mgsanlet.cheftube.data.repository

import com.google.firebase.Timestamp
import com.mgsanlet.cheftube.data.model.StatsResponse
import com.mgsanlet.cheftube.data.source.remote.FirebaseApi
import com.mgsanlet.cheftube.domain.model.DomainStats
import com.mgsanlet.cheftube.domain.repository.StatsRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.StatsError
import java.time.Instant
import javax.inject.Inject

class StatsRepositoryImpl @Inject constructor(
    private val api: FirebaseApi
) : StatsRepository {

    override suspend fun getStats(): DomainResult<DomainStats, StatsError> {
        // TODO: Eliminar esta línea para usar datos reales
        return DomainResult.Success(generateTestData())
        
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

private fun generateTestData(): DomainStats {
    val now = Instant.now()
    val calendar = java.util.Calendar.getInstance()
    
    // Generar datos de prueba para los últimos 30 días
    val loginTimestamps = List(500) { now.minusSeconds((0L..(30L * 24 * 60 * 60)).random()) }
    val interactionTimestamps = List(1500) { now.minusSeconds((0L..(30L * 24 * 60 * 60)).random()) }
    val scanTimestamps = List(200) { now.minusSeconds((0L..(30L * 24 * 60 * 60)).random()) }
    
    return DomainStats(loginTimestamps, interactionTimestamps, scanTimestamps)
}

private fun StatsResponse.toDomain() = DomainStats(
    loginTimestamps = this.loginTimestamps.map { it.toDate().toInstant() },
    interactionTimestamps = this.interactionTimestamps.map { it.toDate().toInstant() },
    scanTimestamps = this.scanTimestamps.map { it.toDate().toInstant() }
)