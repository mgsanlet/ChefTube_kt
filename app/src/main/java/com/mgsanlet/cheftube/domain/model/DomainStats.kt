package com.mgsanlet.cheftube.domain.model

import com.google.firebase.Timestamp

data class DomainStats(
    // Listas de timestamps para cada tipo de evento
    val loginTimestamps: List<Timestamp> = emptyList(),
    val interactionTimestamps: List<Timestamp> = emptyList(),
    val scanTimestamps: List<Timestamp> = emptyList()
) {
    // Propiedades calculadas para facilitar el acceso a los datos agrupados
    val loginsByDay: Map<String, Int> by lazy { groupTimestampsByDay(loginTimestamps) }
    val loginsByMonth: Map<String, Int> by lazy { groupTimestampsByMonth(loginTimestamps) }
    
    val interactionsByDay: Map<String, Int> by lazy { groupTimestampsByDay(interactionTimestamps) }
    val interactionsByMonth: Map<String, Int> by lazy { groupTimestampsByMonth(interactionTimestamps) }
    
    val scansByDay: Map<String, Int> by lazy { groupTimestampsByDay(scanTimestamps) }
    val scansByMonth: Map<String, Int> by lazy { groupTimestampsByMonth(scanTimestamps) }
    
    private fun groupTimestampsByDay(timestamps: List<Timestamp>): Map<String, Int> {
        return timestamps.groupBy { timestamp ->
            timestamp.toDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .toString()
        }.mapValues { it.value.size }
    }
    
    private fun groupTimestampsByMonth(timestamps: List<Timestamp>): Map<String, Int> {
        return timestamps.groupBy { timestamp ->
            val date = timestamp.toDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            "${date.year}-${date.monthValue.toString().padStart(2, '0')}"
        }.mapValues { it.value.size }
    }
}