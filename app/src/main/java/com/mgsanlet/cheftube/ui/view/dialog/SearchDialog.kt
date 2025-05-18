package com.mgsanlet.cheftube.ui.view.dialog

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.DialogSearchBinding
import com.mgsanlet.cheftube.domain.model.SearchParams
import com.mgsanlet.cheftube.domain.util.FilterCriterion
import com.mgsanlet.cheftube.ui.adapter.BaseSpinnerAdapter

class SearchDialog @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var dialog: AlertDialog

    private val binding: DialogSearchBinding = DialogSearchBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    private var onSearchQuerySubmitted: ((SearchParams) -> Unit)? = null

    init {
        setUpViews()
        setUpListeners()
    }

    private fun setUpViews() {
        binding.searchCriteriaSpinner.adapter = BaseSpinnerAdapter(
            context,
            resources.getStringArray(R.array.search_criteria).toList()
        )

        binding.difficultySpinner.adapter = BaseSpinnerAdapter(
            context,
            resources.getStringArray(R.array.difficulty).toList()
        )

        // Configurar la visibilidad de los elementos según el criterio seleccionado
        binding.searchCriteriaSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedCriteria = FilterCriterion.entries[position]
                    when (selectedCriteria) {
                        FilterCriterion.DURATION -> {
                            binding.rangeEditText.visibility = VISIBLE
                            binding.queryEditText.inputType = EditorInfo.TYPE_CLASS_NUMBER
                            binding.queryEditText.hint = context.getString(R.string.range_hint_low)
                            binding.difficultySpinner.visibility = GONE
                        }

                        FilterCriterion.DIFFICULTY -> {
                            binding.queryEditText.visibility = GONE
                            binding.rangeEditText.visibility = GONE
                            binding.difficultySpinner.visibility = VISIBLE
                        }

                        else -> {
                            binding.queryEditText.hint =
                                context.getString(R.string.write_your_search)
                            binding.queryEditText.visibility = VISIBLE
                            binding.queryEditText.inputType = EditorInfo.TYPE_CLASS_TEXT
                            binding.rangeEditText.visibility = GONE
                            binding.difficultySpinner.visibility = GONE
                        }
                    }
                }


                override fun onNothingSelected(parent: AdapterView<*>) {
                    binding.rangeEditText.visibility = GONE
                    binding.difficultySpinner.visibility = GONE
                }
            }
    }

    private fun setUpListeners() {
        binding.searchButton.setOnClickListener { onSearch() }
        binding.cancelButton.setOnClickListener { dismiss() }
    }

    private fun onSearch() {
        val criterion = FilterCriterion.entries[binding.searchCriteriaSpinner.selectedItemPosition]
        val query = binding.queryEditText.text.toString().trim()

        val searchParams = when (criterion) {
            FilterCriterion.DURATION -> {
                val minDuration = query
                val maxDuration = binding.rangeEditText.text.toString().trim()

                if (minDuration.isNotEmpty() && maxDuration.isNotEmpty() &&
                    minDuration.toInt() > maxDuration.toInt()
                ) {
                    binding.rangeEditText.error = context.getString(R.string.duration_range_error)
                    return
                }

                SearchParams(
                    criterion = criterion,
                    minDuration = minDuration,
                    maxDuration = maxDuration
                )
            }

            FilterCriterion.DIFFICULTY -> {
                SearchParams(
                    criterion = criterion,
                    difficulty = binding.difficultySpinner.selectedItemPosition
                )
            }

            else -> {
                SearchParams(
                    criterion = criterion,
                    query = query
                )
            }
        }

        onSearchQuerySubmitted?.invoke(searchParams)
        dismiss()
    }

    private fun dismiss() {
        binding.queryEditText.text?.clear()
        binding.rangeEditText.text?.clear()
        dialog.dismiss()
    }

    fun setOnSearchQuerySubmittedListener(listener: (SearchParams) -> Unit) {
        onSearchQuerySubmitted = listener
    }

    fun show() {
        parent?.let { (it as ViewGroup).removeView(this) }
        val searchDialogBuilder = AlertDialog.Builder(context)
        searchDialogBuilder.setView(this)
        dialog = searchDialogBuilder.create()
        dialog.show()
    }
}
