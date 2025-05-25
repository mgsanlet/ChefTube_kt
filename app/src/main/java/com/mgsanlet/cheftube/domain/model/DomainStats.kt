package com.mgsanlet.cheftube.domain.model

import java.time.Instant
import java.time.ZoneId

data class DomainStats(
    // Listas de timestamps para cada tipo de evento
    val loginTimestamps: List<Instant> = emptyList(),
    val interactionTimestamps: List<Instant> = emptyList(),
    val scanTimestamps: List<Instant> = emptyList()
)
