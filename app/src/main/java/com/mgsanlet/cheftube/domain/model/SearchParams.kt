package com.mgsanlet.cheftube.domain.model

import com.mgsanlet.cheftube.domain.util.FilterCriterion

data class SearchParams(
    val criterion: FilterCriterion,
    val query: String = "",
    val minDuration: String = "",
    val maxDuration: String = "",
    val difficulty: Int = -1
)
