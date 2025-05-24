package com.mgsanlet.cheftube.data.model

import com.google.firebase.Timestamp

data class StatsResponse(
    val loginTimestamps: List<Timestamp> = emptyList(),
    val interactionTimestamps: List<Timestamp> = emptyList(),
    val scanTimestamps: List<Timestamp> = emptyList()
)