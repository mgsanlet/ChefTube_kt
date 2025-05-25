package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarEntry
import com.mgsanlet.cheftube.domain.model.DomainStats
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.usecase.stats.GetStatsUseCase
import com.mgsanlet.cheftube.domain.usecase.user.GetInactiveUsersUseCase
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.DomainError
import com.mgsanlet.cheftube.domain.util.error.UserError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * ViewModel para el panel de administración que maneja la lógica de negocio
 * relacionada con las estadísticas y usuarios inactivos.
 */
@HiltViewModel
class AdminViewModel @Inject constructor(
    private val getStats: GetStatsUseCase,
    private val getInactiveUsers: GetInactiveUsersUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<AdminState>(AdminState.Loading)
    val uiState: LiveData<AdminState> = _uiState

    private var currentStats: DomainStats? = null
    private var currentChartType: Int = 0
    private var currentTimeRange: Int = 0

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.value = AdminState.Loading

        viewModelScope.launch {
            try {
                // 1. Cargar usuarios inactivos (usamos el caso de uso real)
                val inactiveUsersResult = getInactiveUsers()
                if (inactiveUsersResult is DomainResult.Error) {
                    _uiState.value = AdminState.Error(inactiveUsersResult.error)
                    return@launch
                }

                // 2. Cargar estadísticas (usamos el caso de uso real)
                val statsResult = getStats()
                if (statsResult is DomainResult.Error) {
                    _uiState.value = AdminState.Error(statsResult.error)
                    return@launch
                }

                // 3. Actualizar el estado con los datos de prueba
                if (inactiveUsersResult is DomainResult.Success && statsResult is DomainResult.Success) {
                    currentStats = statsResult.data
                    _uiState.value = AdminState.Content(
                        stats = statsResult.data,
                        inactiveUsers = inactiveUsersResult.data,
                        chartEntries = getChartEntries(
                            statsResult.data,
                            currentChartType,
                            currentTimeRange
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AdminState.Error(UserError.Unknown(e.message))
            }
        }
    }

    fun onChartTypeSelected(chartType: Int, timeRange: Int) {
        currentChartType = chartType
        currentTimeRange = timeRange

        currentStats?.let { stats ->
            val entries = getChartEntries(stats, chartType, timeRange)
            val currentState = _uiState.value as? AdminState.Content
            currentState?.let {
                _uiState.value = it.copy(chartEntries = entries)
            }
        }
    }

    private fun getChartEntries(
        stats: DomainStats,
        chartType: Int,
        timeRange: Int
    ): MutableList<BarEntry> {
        val entries = mutableListOf<BarEntry>()
        val timestamps = when (chartType) {
            CHART_TYPE_LOGINS -> stats.loginTimestamps
            CHART_TYPE_INTERACTIONS -> stats.interactionTimestamps
            CHART_TYPE_SCANS -> stats.scanTimestamps
            else -> emptyList()
        }

        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()
        
        when (timeRange) {
            TIME_RANGE_LAST_24H -> {
                // Agrupar por hora (últimas 24 horas)
                val hourCounts = IntArray(24) { 0 }
                timestamps.forEach { timestamp ->
                    calendar.timeInMillis = timestamp.toEpochMilli()
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    hourCounts[hour]++
                }
                hourCounts.forEachIndexed { index, count ->
                    // Usamos el timestamp del inicio de la hora como dato
                    calendar.timeInMillis = now
                    calendar.set(Calendar.HOUR_OF_DAY, index)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    entries.add(BarEntry(index.toFloat(), count.toFloat(), calendar.timeInMillis))
                }
            }
            TIME_RANGE_LAST_7_DAYS -> {
                // Agrupar por día (últimos 7 días)
                val dayCounts = IntArray(7) { 0 }
                
                timestamps.forEach { timestamp ->
                    calendar.timeInMillis = timestamp.toEpochMilli()
                    val daysAgo = ((now - calendar.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()
                    if (daysAgo in 0..6) {
                        dayCounts[6 - daysAgo]++
                    }
                }
                
                dayCounts.forEachIndexed { index, count ->
                    // Usamos el timestamp del inicio del día como dato
                    calendar.timeInMillis = now
                    calendar.add(Calendar.DAY_OF_YEAR, -6 + index)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    entries.add(BarEntry(index.toFloat(), count.toFloat(), calendar.timeInMillis))
                }
            }
            TIME_RANGE_LAST_30_DAYS -> {
                // Agrupar por día (últimos 30 días)
                val dayCounts = IntArray(30) { 0 }
                
                timestamps.forEach { timestamp ->
                    calendar.timeInMillis = timestamp.toEpochMilli()
                    val daysAgo = ((now - calendar.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()
                    if (daysAgo in 0..29) {
                        dayCounts[29 - daysAgo]++
                    }
                }
                
                dayCounts.forEachIndexed { index, count ->
                    // Usamos el timestamp del inicio del día como dato
                    calendar.timeInMillis = now
                    calendar.add(Calendar.DAY_OF_YEAR, -29 + index)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    entries.add(BarEntry(index.toFloat(), count.toFloat(), calendar.timeInMillis))
                }
            }
            TIME_RANGE_LAST_12_MONTHS -> {
                // Agrupar por mes (últimos 12 meses)
                val monthCounts = IntArray(12) { 0 }
                
                timestamps.forEach { timestamp ->
                    calendar.timeInMillis = timestamp.toEpochMilli()
                    val monthsAgo = ((now - calendar.timeInMillis) / (30.44 * 24 * 60 * 60 * 1000)).toInt()
                    if (monthsAgo in 0..11) {
                        monthCounts[11 - monthsAgo]++
                    }
                }
                
                monthCounts.forEachIndexed { index, count ->
                    // Usamos el timestamp del primer día del mes como dato
                    calendar.timeInMillis = now
                    calendar.add(Calendar.MONTH, -11 + index)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    entries.add(BarEntry(index.toFloat(), count.toFloat(), calendar.timeInMillis))
                }
            }
        }
        
        return entries
    }

    companion object {
        // Constantes para los tipos de gráfico
        const val CHART_TYPE_LOGINS = 0
        const val CHART_TYPE_INTERACTIONS = 1
        const val CHART_TYPE_SCANS = 2

        // Constantes para los rangos de tiempo
        const val TIME_RANGE_LAST_24H = 0
        const val TIME_RANGE_LAST_7_DAYS = 1
        const val TIME_RANGE_LAST_30_DAYS = 2
        const val TIME_RANGE_LAST_12_MONTHS = 3

        private const val HOUR_IN_MILLIS = 60 * 60 * 1000L
        private const val DAY_IN_MILLIS = 24 * HOUR_IN_MILLIS
    }
}

sealed class AdminState {
    object Loading : AdminState()
    data class Content(
        val stats: DomainStats,
        val inactiveUsers: List<DomainUser> = emptyList(),
        val chartEntries: MutableList<BarEntry> = mutableListOf()
    ) : AdminState()

    data class Error(val error: DomainError) : AdminState()
}