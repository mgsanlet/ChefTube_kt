package com.mgsanlet.cheftube.data.model

import com.google.firebase.Timestamp

/**
 * Contiene estadísticas de interacciones de usuarios con la aplicación.
 *
 * @property loginTimestamps Lista de marcas de tiempo de inicio de sesión de usuarios
 * @property interactionTimestamps Lista de marcas de tiempo de interacciones de usuarios
 * @property scanTimestamps Lista de marcas de tiempo de escaneos de productos realizados
 */
data class StatsResponse(
    val loginTimestamps: List<Timestamp> = emptyList(),
    val interactionTimestamps: List<Timestamp> = emptyList(),
    val scanTimestamps: List<Timestamp> = emptyList()
)