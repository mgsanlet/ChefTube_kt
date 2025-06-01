package com.mgsanlet.cheftube.domain.model

import java.time.Instant

/**
 * Clase de dominio que contiene estadísticas de uso de la aplicación.
 *
 * @property loginTimestamps Lista de marcas de tiempo de los inicios de sesión de usuarios
 * @property interactionTimestamps Lista de marcas de tiempo de interacciones de usuarios
 * @property scanTimestamps Lista de marcas de tiempo de los escaneos de códigos de usuarios
 */
data class DomainStats(
    val loginTimestamps: List<Instant> = emptyList(),
    val interactionTimestamps: List<Instant> = emptyList(),
    val scanTimestamps: List<Instant> = emptyList()
)
