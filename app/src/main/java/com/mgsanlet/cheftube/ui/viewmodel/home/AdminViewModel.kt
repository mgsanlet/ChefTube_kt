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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
        loadStats()
        loadInactiveUsers()
    }

    private fun loadStats() {
        _uiState.value = AdminState.Loading

        viewModelScope.launch {

            when (val result = getStats()) {
                is DomainResult.Success -> {
                    currentStats = result.data
                    _uiState.value = AdminState.Content(
                        stats = result.data,
                        inactiveUsers = emptyList(),
                        chartEntries = getChartEntries(result.data, currentChartType, currentTimeRange)
                    )
                }

                is DomainResult.Error -> {
                    _uiState.value = AdminState.Error(result.error)
                }
            }
        }
    }

    private fun loadInactiveUsers() {
        viewModelScope.launch {
            when (val result = getInactiveUsers()) {
                is DomainResult.Success -> {
                    val currentState = _uiState.value as? AdminState.Content
                    currentState?.let {
                        _uiState.value = it.copy(inactiveUsers = result.data)
                    }
                }

                is DomainResult.Error -> { _uiState.value = AdminState.Error(result.error) }
            }
        }
    }

    /**
     * Maneja el cambio en el tipo de gráfico seleccionado
     * @param chartType Tipo de gráfico (0: Logins, 1: Interacciones, 2: Escaneos)
     * @param timeRange Rango de tiempo (0: Mensual, 1: Diario)
     */
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

    fun deleteInactiveUser(userId: String) {
        viewModelScope.launch {
            try {
                // TODO: Implementar lógica de eliminación de usuario
                // Por ahora, solo actualizamos la UI simulando la eliminación
                val currentState = _uiState.value as? AdminState.Content ?: return@launch
                val updatedUsers = currentState.inactiveUsers.toMutableList().apply {
                    removeAll { it.id == userId }
                }
                _uiState.value = currentState.copy(inactiveUsers = updatedUsers)
            } catch (e: Exception) {
                _uiState.value = AdminState.Error(
                    error = UserError.Unknown(e.message)
                )
            }
        }
    }

    private fun getChartEntries(
        stats: DomainStats,
        chartType: Int,
        timeRange: Int = 0 // 0 = mensual, 1 = diario
    ): MutableList<BarEntry> {
        val entries = mutableListOf<BarEntry>()

        // Obtener los datos según el tipo de gráfico y rango de tiempo
        val dataMap = when (chartType) {
            CHART_TYPE_LOGINS ->
                if (timeRange == TIME_RANGE_MONTHLY) stats.loginsByMonth else stats.loginsByDay

            CHART_TYPE_INTERACTIONS ->
                if (timeRange == TIME_RANGE_MONTHLY) stats.interactionsByMonth else stats.interactionsByDay

            CHART_TYPE_SCANS ->
                if (timeRange == TIME_RANGE_MONTHLY) stats.scansByMonth else stats.scansByDay

            else -> emptyMap()
        }

        // Ordenar las fechas
        val sortedDates = dataMap.keys.sorted()

        // Limitar a los últimos 12 meses o 30 días según el rango de tiempo
        val limit = if (timeRange == TIME_RANGE_MONTHLY) 12 else 30
        val limitedDates = sortedDates.takeLast(limit)

        // Crear entradas para el gráfico
        limitedDates.forEachIndexed { index, date ->
            val value = dataMap[date]?.toFloat() ?: 0f
            entries.add(BarEntry(index.toFloat(), value, date))
        }

        return entries
    }

    companion object {
        // Constantes para los tipos de gráfico
        const val CHART_TYPE_LOGINS = 0
        const val CHART_TYPE_INTERACTIONS = 1
        const val CHART_TYPE_SCANS = 2

        // Constantes para los rangos de tiempo
        const val TIME_RANGE_MONTHLY = 0
        const val TIME_RANGE_DAILY = 1
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