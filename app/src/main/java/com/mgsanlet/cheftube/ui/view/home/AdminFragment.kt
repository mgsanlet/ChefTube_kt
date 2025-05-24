package com.mgsanlet.cheftube.ui.view.home

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
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
import com.mgsanlet.cheftube.ui.viewmodel.home.AdminViewModel.Companion.TIME_RANGE_DAILY
import com.mgsanlet.cheftube.ui.viewmodel.home.AdminViewModel.Companion.TIME_RANGE_MONTHLY
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminFragment : BaseFragment<FragmentAdminBinding>() {

    private val viewModel: AdminViewModel by viewModels()

    private lateinit var inactiveUsersAdapter: InactiveUsersAdapter
    
    private var currentChartType = CHART_TYPE_LOGINS
    private var currentTimeRange = TIME_RANGE_MONTHLY

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
        // Configurar spinner de tipo de gráfico
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
                updateChart()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        binding.spinnerTimeRange.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentTimeRange = if (position == 0) TIME_RANGE_MONTHLY else TIME_RANGE_DAILY
                updateChart()
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
            
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                axisMinimum = 0f
                granularity = 1f
            }
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelCount = 6
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = true
        }
    }
    
    private fun setUpInactiveUsersList() {
        inactiveUsersAdapter = InactiveUsersAdapter(
            onDeleteClick = { user ->
                //showDeleteConfirmation(user)
            }
        )
        
        binding.recyclerInactiveUsers.apply {
            adapter = inactiveUsersAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
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
                    updateChart(state.chartEntries)
                    updateStats(state.stats)
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

    
    private fun updateChart() {
        viewModel.onChartTypeSelected(currentChartType, currentTimeRange)
    }
    
    private fun updateChart(entries: List<BarEntry>) {

        // Establecer la etiqueta según el tipo de gráfico
        val label: String = when (binding.spinnerChartType.selectedItemPosition) {
            CHART_TYPE_LOGINS -> "Logins"
            CHART_TYPE_INTERACTIONS -> "Interactions"
            CHART_TYPE_SCANS -> "Scans"
            else -> ""
        }

        // Configurar el conjunto de datos del gráfico
        val dataSet = BarDataSet(entries, label).apply {
            color = R.color.primary_orange
            valueTextColor = android.R.color.white
            valueTextSize = 10f
            setDrawValues(true)
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.5f
        }

        with(binding.chart) {
            data = barData
            description.isEnabled = false
            setFitBars(true)
            invalidate()
            animateY(500)
        }
    }
    
    private fun updateStats(stats: DomainStats) {
        // Actualizar estadísticas generales
        binding.apply {
            textTotalUsers.text = stats.loginTimestamps.size.toString()
            textTotalInteractions.text = stats.interactionTimestamps.size.toString()
            textTotalScans.text = stats.scanTimestamps.size.toString()

            val currentState = viewModel.uiState.value as? AdminState.Content
            val inactiveCount = currentState?.inactiveUsers?.size ?: 0
            textInactiveUsers.text = inactiveCount.toString()
            
            // Mostrar u ocultar la sección de usuarios inactivos
            layoutInactiveUsersSection.isVisible = inactiveCount > 0
        }
    }
    
    private fun updateInactiveUsers(users: List<DomainUser>) {
        inactiveUsersAdapter.submitList(users)
        binding.layoutInactiveUsersSection.isVisible = users.isNotEmpty()
    }
    
    private fun showLoading(show: Boolean) {
        if (show) {
            LoadingDialog.show(requireContext(), parentFragmentManager)
        } else {
            LoadingDialog.dismiss(parentFragmentManager)
        }
    }

    private fun showDeleteConfirmationDialog(userId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Configurar el diálogo
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // Configurar las vistas del diálogo
        dialogView.apply {
            findViewById<TextView>(R.id.dialogTitleTextView).text =
                context.getString(R.string.delete_user)
            findViewById<TextView>(R.id.dialogMessageTextView).text =
                context.getString(R.string.delete_user_message)

            val positiveButton = findViewById<Button>(R.id.confirmButton)
            positiveButton.text = getString(R.string.delete)
            positiveButton.setOnClickListener {
                viewModel.deleteInactiveUser(userId)
                dialog.dismiss()
            }

            val negativeButton = findViewById<Button>(R.id.cancelButton)
            negativeButton.text = getString(R.string.cancel)
            negativeButton.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        LoadingDialog.dismiss(parentFragmentManager)
        super.onDestroyView()
    }
    

}