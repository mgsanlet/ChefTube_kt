package com.mgsanlet.cheftube.domain.model

import com.mgsanlet.cheftube.domain.util.FilterCriterion

/**
 * Clase que representa los parámetros de búsqueda para filtrar recetas.
 *
 * @property criterion Criterio de filtrado (por título, ingredientes, etc.)
 * @property query Término de búsqueda
 * @property minDuration Duración mínima de la receta (en formato de texto)
 * @property maxDuration Duración máxima de la receta (en formato de texto)
 * @property difficulty Nivel de dificultad para filtrar (-1 para no filtrar por dificultad)
 */
data class SearchParams(
    val criterion: FilterCriterion,
    val query: String = "",
    val minDuration: String = "",
    val maxDuration: String = "",
    val difficulty: Int = -1
)
