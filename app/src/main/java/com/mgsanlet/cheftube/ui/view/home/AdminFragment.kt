package com.mgsanlet.cheftube.ui.view.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.snackbar.Snackbar
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentAdminBinding
import com.mgsanlet.cheftube.domain.model.DomainStats
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.util.error.StatsError
import com.mgsanlet.cheftube.domain.util.error.UserError
import com.mgsanlet.cheftube.ui.adapter.BaseSpinnerAdapter
import com.mgsanlet.cheftube.ui.adapter.InactiveUsersAdapter
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.view.base.BaseFragment
import com.mgsanlet.cheftube.ui.view.dialogs.LoadingDialog
import com.mgsanlet.cheftube.ui.viewmodel.home.AdminState
import com.mgsanlet.cheftube.ui.viewmodel.home.AdminViewModel
import com.mgsanlet.cheftube.ui.viewmodel.home.AdminViewModel.Companion.CHART_TYPE_INTERACTIONS
import com.mgsanlet.cheftube.ui.viewmodel.home.AdminViewModel.Companion.CHART_TYPE_LOGINS
import com.mgsanlet.cheftube.ui.viewmodel.home.AdminViewModel.Companion.CHART_TYPE_SCANS
import com.mgsanlet.cheftube.ui.viewmodel.home.AdminViewModel.Companion.TIME_RANGE_LAST_24H
import com.mgsanlet.cheftube.ui.viewmodel.home.AdminViewModel.Companion.TIME_RANGE_LAST_7_DAYS
import com.mgsanlet.cheftube.ui.viewmodel.home.AdminViewModel.Companion.TIME_RANGE_LAST_30_DAYS
import com.mgsanlet.cheftube.ui.viewmodel.home.AdminViewModel.Companion.TIME_RANGE_LAST_12_MONTHS
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import androidx.core.graphics.toColorInt
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment
import com.github.mikephil.charting.components.Legend.LegendOrientation
import com.github.mikephil.charting.components.Legend.LegendVerticalAlignment

@AndroidEntryPoint
class AdminFragment : BaseFragment<FragmentAdminBinding>() {

    private val viewModel: AdminViewModel by viewModels()

    private lateinit var inactiveUsersAdapter: InactiveUsersAdapter
    
    private var currentChartType = CHART_TYPE_LOGINS
    private var currentTimeRange = TIME_RANGE_LAST_24H

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAdminBinding = FragmentAdminBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSpinners()
        setUpChart()
        setUpInactiveUsersList()
    }
    
    private fun setUpSpinners() {

        binding.spinnerChartType.adapter = BaseSpinnerAdapter(
            requireContext(),
            resources.getStringArray(R.array.chart_types).toList()
        )
        
        binding.spinnerTimeRange.adapter = BaseSpinnerAdapter(
            requireContext(),
            resources.getStringArray(R.array.time_ranges).toList()
        )
        
        // Configurar listeners para los spinners
        binding.spinnerChartType.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentChartType = when (position) {
                    0 -> CHART_TYPE_LOGINS
                    1 -> CHART_TYPE_INTERACTIONS
                    2 -> CHART_TYPE_SCANS
                    else -> CHART_TYPE_LOGINS
                }
                updateChartType()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        binding.spinnerTimeRange.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentTimeRange = when (position) {
                    0 -> TIME_RANGE_LAST_24H
                    1 -> TIME_RANGE_LAST_7_DAYS
                    2 -> TIME_RANGE_LAST_30_DAYS
                    3 -> TIME_RANGE_LAST_12_MONTHS
                    else -> TIME_RANGE_LAST_24H
                }
                updateChartType()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setUpChart() {
        with(binding.chart) {
            description.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            setExtraOffsets(0f, 10f, 0f, 10f)
            
            // Configurar eje Y izquierdo
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                axisMinimum = 0f
                axisLineWidth = 1f
                textColor = Color.WHITE
                textSize = 10f
                axisLineColor = Color.WHITE
                gridColor = "#4DFFFFFF".toColorInt() // 30% white for grid
                
                // Configurar valores redondos en el eje Y
                setLabelCount(6, true) // Mostrar 6 etiquetas
                
                // Configurar la granularidad del eje Y
                granularity = granularity
                isGranularityEnabled = true
                isGranularityEnabled = true
                
                // Forzar números enteros redondos
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "%,.0f".format(Locale.getDefault(), value)
                    }
                }
                
                // Calcular valores redondos para el eje Y
                setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                xOffset = 15f // Espacio adicional entre las etiquetas y el eje
                
                // Deshabilitar el ajuste automático de los valores
                isGranularityEnabled = true
                granularity = 1f
            }
            
            // Configurar eje X
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelRotationAngle = -45f
                textColor = Color.WHITE
                textSize = 10f
                axisLineColor = Color.WHITE
                yOffset = 2f // Añadir espacio adicional entre las etiquetas y el eje
                
                // Ajustar el número de etiquetas según el rango de tiempo
                when (currentTimeRange) {
                    TIME_RANGE_LAST_24H -> labelCount = 12 // Mostrar etiquetas cada 2 horas
                    TIME_RANGE_LAST_7_DAYS -> labelCount = 7 // Una etiqueta por día
                    TIME_RANGE_LAST_30_DAYS -> labelCount = 6 // Aprox. una etiqueta cada 5 días
                    TIME_RANGE_LAST_12_MONTHS -> labelCount = 12 // Mostrar los 12 meses
                }
            }
            
            // Configurar leyenda
            legend.apply {
                isEnabled = true
                textSize = 12f
                textColor = Color.WHITE
                verticalAlignment = LegendVerticalAlignment.TOP
                horizontalAlignment = LegendHorizontalAlignment.RIGHT
                orientation = LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }
            
            axisRight.isEnabled = false
            setNoDataText(getString(R.string.no_chart_data_available))
            setNoDataTextColor(Color.WHITE)
            
            // Asegurar que el eje Y tenga suficiente espacio
            axisLeft.spaceMin = 0.5f
            axisLeft.spaceMax = 0.5f
            
            // La configuración de desplazamiento horizontal se manejará en updateChart
        }
    }
    
    private fun setUpInactiveUsersList() {
        binding.recyclerInactiveUsers.apply {
            inactiveUsersAdapter = InactiveUsersAdapter()
            adapter = inactiveUsersAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
            // Establecer una altura mínima para evitar que se colapse completamente cuando no hay elementos
            minimumHeight = 0
        }
    }
    
    override fun setUpObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AdminState.Loading -> {
                    showLoading(true)
                }

                is AdminState.Content -> {
                    showLoading(false)
                    updateChartContent(state.chartEntries)
                    updateStats(state.stats, state.inactiveUsers.size)
                    updateInactiveUsers(state.inactiveUsers)
                }

                is AdminState.Error -> {
                    showLoading(false)
                    when (state.error) {
                        is UserError -> showError(state.error.asMessage(requireContext()))
                        is StatsError -> showError(state.error.asMessage(requireContext()))
                    }
                }
            }
        }

    }

    
    private fun updateChartType() {
        viewModel.onChartTypeSelected(currentChartType, currentTimeRange)
    }

    private fun calculateYAxisValues(maxValue: Float): Pair<Float, Float> {
        val step = when {
            maxValue <= 10 -> 1f
            maxValue <= 20 -> 2f
            maxValue <= 50 -> 5f
            maxValue <= 100 -> 10f
            maxValue <= 200 -> 20f
            maxValue <= 500 -> 50f
            maxValue <= 1000 -> 100f
            maxValue <= 2000 -> 200f
            maxValue <= 5000 -> 500f
            maxValue <= 10000 -> 1000f
            else -> 5000f
        }
        
        // Calcular el máximo redondeado al siguiente múltiplo del paso
        val roundedMax = if (maxValue % step == 0f) maxValue 
                        else ((maxValue / step).toInt() + 1) * step
        
        return step to maxOf(roundedMax, step)  // Asegurar al menos un paso
    }

    private fun updateChartContent(entries: List<BarEntry>) {
        if (entries.isEmpty()) {
            binding.chart.clear()
            binding.chart.invalidate()
            return
        }

        // Convertir BarEntry a Entry para LineChart
        val lineEntries = entries.mapIndexed { index, barEntry ->
            Entry(barEntry.x, barEntry.y, barEntry.data)
        }
        
        // Encontrar el valor máximo y calcular la granularidad
        val maxValue = entries.maxOfOrNull { it.y } ?: 0f
        var (granularity, maxY) = calculateYAxisValues(maxValue)

        // Establecer la etiqueta según el tipo de gráfico
        val label: String = resources.getStringArray(R.array.chart_types)[currentChartType]

        // Configurar el conjunto de datos del gráfico de líneas
        val dataSet = LineDataSet(lineEntries, label).apply {
            color = resources.getColor(R.color.primary_orange, null)
            lineWidth = 2.5f
            setDrawCircles(true)
            setDrawCircleHole(false)
            circleRadius = 4f
            circleHoleRadius = 2f
            circleHoleColor = Color.WHITE
            circleColors = listOf(resources.getColor(R.color.primary_orange, null))
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
            setDrawFilled(true)
            fillDrawable =  ContextCompat.getDrawable(requireContext(), R.drawable.chart_gradient)
            setDrawValues(false)
            setDrawHorizontalHighlightIndicator(false)
            highlightLineWidth = 1.5f
            setDrawHighlightIndicators(true)
            highLightColor = Color.WHITE
        }


        val lineData = LineData(dataSet).apply {
            setValueTextSize(10f)
            setValueTextColor(Color.WHITE)
        }

        with(binding.chart) {
            data = lineData
            
            // Configurar el eje X según el rango de tiempo
            xAxis.apply {
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    private val hourFormat = java.text.SimpleDateFormat("HH", Locale.getDefault())
                    private val dayFormat = java.text.SimpleDateFormat("dd/MM", Locale.getDefault())
                    private val monthFormat = java.text.SimpleDateFormat("MM/yy", Locale.getDefault())
                    
                    override fun getFormattedValue(value: Float): String {
                        val entry = entries.getOrNull(value.toInt()) ?: return ""
                        val timestamp = entry.data as? Long ?: return ""
                        val date = java.util.Date(timestamp)
                        
                        return when (currentTimeRange) {
                            TIME_RANGE_LAST_24H -> hourFormat.format(date) + "h"
                            TIME_RANGE_LAST_7_DAYS -> dayFormat.format(date)
                            TIME_RANGE_LAST_30_DAYS -> dayFormat.format(date)
                            TIME_RANGE_LAST_12_MONTHS -> monthFormat.format(date)
                            else -> dayFormat.format(date)
                        }
                    }
                }
                
                // Ajustar el espacio entre etiquetas
                when (currentTimeRange) {
                    TIME_RANGE_LAST_24H -> {
                        granularity = 4f // Mostrar etiquetas cada 4 horas
                        setCenterAxisLabels(false)
                        labelCount = 6 // Mostrar 6 etiquetas (cada 4 horas)
                    }
                    TIME_RANGE_LAST_7_DAYS -> {
                        granularity = 1f // Mostrar etiquetas para cada día
                        setCenterAxisLabels(true)
                        labelCount = 7 // Mostrar los 7 días
                    }
                    TIME_RANGE_LAST_30_DAYS -> {
                        granularity = 5f // Mostrar etiquetas cada 5 días
                        setCenterAxisLabels(true)
                        labelCount = 6 // Mostrar 6 etiquetas (cada 5 días)
                    }
                    TIME_RANGE_LAST_12_MONTHS -> {
                        granularity = 1f // Mostrar etiquetas para cada mes
                        setCenterAxisLabels(true)
                        labelCount = 12 // Mostrar los 12 meses
                    }
                }
                
                // Asegurar que todas las etiquetas se muestren
                setAvoidFirstLastClipping(true)
            }
            
            // Configuración adicional para LineChart
            setDrawGridBackground(false)
            setDrawBorders(false)
            setBorderColor(Color.WHITE)
            setBorderWidth(1f)
            
            // Asegurar la visualización
            setVisibleXRangeMaximum(entries.size.toFloat() * 1.1f)
            moveViewToX(0f)

            // Asegurar que los valores del eje Y sean redondos
            setScaleMinima(1f, 1f)
            setVisibleYRangeMinimum(0f, YAxis.AxisDependency.LEFT)
            
            // Configurar los límites del eje Y
            axisLeft.axisMaximum = maxY
            axisLeft.axisMinimum = 0f
            axisLeft.granularity = granularity
            
            // Forzar la actualización del eje
            axisLeft.setDrawGridLines(true)
            axisLeft.resetAxisMinimum()
            axisLeft.resetAxisMaximum()
            
            // Configurar la descripción
            description.textColor = Color.WHITE
            
            // Animación
            animateY(1000, Easing.EaseInOutQuad)
            
            // Actualizar la leyenda
            legend.apply {
                textColor = Color.WHITE
                textSize = 12f
            }
            
            invalidate()
        }
    }
    
    private fun updateStats(stats: DomainStats, inactiveUsers: Int) {
        // Actualizar estadísticas generales
        binding.apply {
            textTotalUsers.text = stats.loginTimestamps.size.toString()
            textTotalInteractions.text = stats.interactionTimestamps.size.toString()
            textTotalScans.text = stats.scanTimestamps.size.toString()

            textInactiveUsers.text = inactiveUsers.toString()
            // La visibilidad se maneja en updateInactiveUsers
        }
    }
    
    private fun updateInactiveUsers(users: List<DomainUser>) {
        val isVisible = users.isNotEmpty()
        binding.apply {
            layoutInactiveUsersSection.isVisible = isVisible
            if (isVisible) {
                inactiveUsersAdapter.submitList(users)
                updateRecyclerViewHeight(users.size)
            } else {
                inactiveUsersAdapter.submitList(emptyList())
            }
        }
    }

    private fun updateRecyclerViewHeight(itemCount: Int) {
        if (itemCount == 0) {
            binding.recyclerInactiveUsers.layoutParams.height = 0
            return
        }
        
        // Obtener la altura de un elemento
        val itemHeight = resources.getDimensionPixelSize(R.dimen.inactive_user_item_height)
        val dividerHeight = resources.getDimensionPixelSize(R.dimen.divider_height)
        val padding = resources.getDimensionPixelSize(R.dimen.default_padding)
        
        // Calcular la altura total (altura del ítem + margen) * número de ítems (máximo 3)
        val totalItems = minOf(itemCount, 3)
        val totalHeight = (itemHeight + dividerHeight) * totalItems + padding
        
        // Establecer la altura calculada
        binding.recyclerInactiveUsers.layoutParams.height = totalHeight
        binding.recyclerInactiveUsers.requestLayout()
    }
    
    private fun showLoading(show: Boolean) {
        if (show) {
            LoadingDialog.show(requireContext(), parentFragmentManager)
        } else {
            LoadingDialog.dismiss(parentFragmentManager)
        }
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        LoadingDialog.dismiss(parentFragmentManager)
        super.onDestroyView()
    }
}