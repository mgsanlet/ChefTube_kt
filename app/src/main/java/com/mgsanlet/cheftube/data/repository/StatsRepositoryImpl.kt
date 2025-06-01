package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.model.StatsResponse
import com.mgsanlet.cheftube.data.source.remote.FirebaseApi
import com.mgsanlet.cheftube.domain.model.DomainStats
import com.mgsanlet.cheftube.domain.repository.StatsRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.StatsError
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de [StatsRepository] que gestiona las estadísticas de la aplicación.
 * Actualmente incluye datos de prueba que deben ser reemplazados por datos reales de Firebase.
 *
 * @property api Cliente de Firebase para operaciones de base de datos
 */
@Singleton
class StatsRepositoryImpl @Inject constructor(
    private val api: FirebaseApi
) : StatsRepository {

    /**
     * Obtiene las estadísticas de la aplicación.
     * Actualmente devuelve datos de prueba. Para usar datos reales, eliminar
     * la primera línea que devuelve [generateTestData()].
     *
     * @return [DomainResult] con las estadísticas o un error
     */
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

/**
 * Genera datos de prueba para las estadísticas.
 * Crea timestamps aleatorios en los últimos 30 días.
 *
 * @return [DomainStats] con datos de prueba
 */
private fun generateTestData(): DomainStats {
    val now = Instant.now()
    
    // Generar datos de prueba para los últimos 30 días
    val loginTimestamps = List(24) { now.minusSeconds((0L..(30L * 24 * 60 * 60)).random()) }
    val interactionTimestamps = List(83) { now.minusSeconds((0L..(30L * 24 * 60 * 60)).random()) }
    val scanTimestamps = List(15) { now.minusSeconds((0L..(30L * 24 * 60 * 60)).random()) }
    
    return DomainStats(loginTimestamps, interactionTimestamps, scanTimestamps)
}

/**
 * Convierte un [StatsResponse] a un [DomainStats].
 *
 * @receiver Respuesta de la API a convertir
 * @return [DomainStats] con los datos de las estadísticas
 */
private fun StatsResponse.toDomain() = DomainStats(
    loginTimestamps = this.loginTimestamps.map { it.toDate().toInstant() },
    interactionTimestamps = this.interactionTimestamps.map { it.toDate().toInstant() },
    scanTimestamps = this.scanTimestamps.map { it.toDate().toInstant() }
)