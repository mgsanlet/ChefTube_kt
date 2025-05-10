package com.mgsanlet.cheftube.ui.view.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentRecipeFormBinding
import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.ui.adapter.DifficultySpinnerAdapter
import com.mgsanlet.cheftube.ui.util.Constants.ARG_RECIPE
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.util.asStringList
import com.mgsanlet.cheftube.ui.util.dpToPx
import com.mgsanlet.cheftube.ui.util.loadUrl
import com.mgsanlet.cheftube.ui.util.removeLastChild
import com.mgsanlet.cheftube.ui.util.setCustomStyle
import com.mgsanlet.cheftube.ui.view.base.BaseFormFragment
import com.mgsanlet.cheftube.ui.viewmodel.home.RecipeFormState
import com.mgsanlet.cheftube.ui.viewmodel.home.RecipeFormViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecipeFormFragment @Inject constructor() : BaseFormFragment<FragmentRecipeFormBinding>() {

    private val viewModel: RecipeFormViewModel by viewModels()

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRecipeFormBinding = FragmentRecipeFormBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val recipeId = it.getString(ARG_RECIPE)
            recipeId?.let {
                viewModel.loadRecipe(recipeId)
            }
        }
    }

    override fun setUpViewProperties() {
        binding.progressBar.setCustomStyle(requireContext())
        binding.difficultySpinner.adapter = DifficultySpinnerAdapter(
            requireContext(),
            resources.getStringArray(R.array.difficulty).toList()
        )
    }

    override fun setUpObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RecipeFormState.Loading -> showLoading(true)

                is RecipeFormState.LoadSuccess -> {
                    showLoading(false)
                    viewModel.recipe.value?.let { showRecipeData(it) }
                }

                is RecipeFormState.Error ->
                    when (state.error) {

                        else -> Toast.makeText(
                            context,
                            state.error.asMessage(requireContext()),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                is RecipeFormState.SaveSuccess -> TODO()
            }
        }
    }

    override fun setUpListeners() {
        binding.saveButton.setOnClickListener {
            if (isValidViewInput()) {
                if (binding.customVideoLoader.state == VideoUrlState.INITIAL ||
                    binding.customVideoLoader.state == VideoUrlState.VALID) {
                    viewModel.trySaveRecipe(
                        binding.titleEditText.text.toString(),
                        binding.customVideoLoader.getText(),
                        binding.recipeImageView.toString(),
                        binding.durationEditText.text.toString(),
                        binding.difficultySpinner.selectedItemPosition,
                        binding.categoriesInnerContainer.asStringList(),
                        binding.ingredientsInnerContainer.asStringList(),
                        binding.stepsInnerContainer.asStringList()
                    )
                } else {
                    binding.customVideoLoader.setError(getString(R.string.invalid_video_url))
                    binding.customVideoLoader.requestFocus()
                }
            }
        }
        binding.cancelButton.setOnClickListener {
            (activity as? HomeActivity)?.onBackPressedDispatcher?.onBackPressed()
        }

        binding.loadImageButton.setOnClickListener {
            Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show()
        }

        binding.categoriesAddButton.setOnClickListener {
            if (binding.categoriesInnerContainer.childCount < 10) {
                binding.categoriesInnerContainer.addView(
                    createCustomEditText(getString(R.string.new_category))
                )
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.maximum_10_categories),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.ingredientsAddButton.setOnClickListener {
            if (binding.ingredientsInnerContainer.childCount < 10) {
                binding.ingredientsInnerContainer.addView(
                    createCustomEditText(getString(R.string.new_ingredient))
                )
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.maximum_10_ingredients),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.stepsAddButton.setOnClickListener {
            if (binding.stepsInnerContainer.childCount < 10) {
                binding.stepsInnerContainer.addView(
                    createCustomEditText(getString(R.string.new_step))
                )
            } else {
                Toast.makeText(context, getString(R.string.maximum_10_steps), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding.categoriesRemoveButton.setOnClickListener { binding.categoriesInnerContainer.removeLastChild() }
        binding.ingredientsRemoveButton.setOnClickListener { binding.ingredientsInnerContainer.removeLastChild() }
        binding.stepsRemoveButton.setOnClickListener { binding.stepsInnerContainer.removeLastChild() }
    }

    override fun isValidViewInput(): Boolean {
        var isValid: Boolean

        var requiredFields = listOf(
            binding.titleEditText,
            binding.durationEditText
        )
        binding.categoriesInnerContainer.children.forEach {
            if (it is EditText) requiredFields = requiredFields.plus(it)
        }
        binding.ingredientsInnerContainer.children.forEach {
            if (it is EditText) requiredFields = requiredFields.plus(it)
        }
        binding.stepsInnerContainer.children.forEach {
            if (it is EditText) requiredFields = requiredFields.plus(it)
        }

        // Comprobamos si hay campos requeridos vacíos y mostramos errores
        isValid = !areFieldsEmpty(requiredFields)

        if (!isValid) return false

        // Comprobamos requerimientos de longitud y mínimos y mostramos errores
        if (binding.titleEditText.text.toString().length !in 5..30) {
            binding.titleEditText.error = getString(R.string.invalid_title_length)
            isValid = false
        }
        if (binding.durationEditText.text.isNotBlank()) {
            if (binding.durationEditText.text.toString().toInt() <= 0) {
                binding.durationEditText.error = getString(R.string.invalid_minutes)
                isValid = false
            }
        }

        if (binding.ingredientsInnerContainer.childCount <= 0) {
            val newIngredient = createCustomEditText(getString(R.string.new_ingredient))
            newIngredient.error = getString(R.string.at_least_1_ingredient)
            binding.ingredientsInnerContainer.addView(newIngredient)
            binding.ingredientsInnerContainer.requestFocus()
            isValid = false
        }

        if (binding.stepsInnerContainer.childCount <= 0) {
            val newStep = createCustomEditText(getString(R.string.new_step))
            newStep.error = getString(R.string.at_least_1_step)
            binding.stepsInnerContainer.addView(newStep)
            binding.stepsInnerContainer.requestFocus()
            isValid = false
        }

        return isValid
    }

    private fun showRecipeData(recipe: DomainRecipe) {
        binding.titleEditText.setText(recipe.title)
        binding.customVideoLoader.setText(recipe.videoUrl)
        binding.recipeImageView.loadUrl(recipe.imageUrl, requireContext())
        binding.durationEditText.setText(recipe.durationMinutes)
        binding.difficultySpinner.setSelection(recipe.difficulty)
        fillContainer(binding.categoriesInnerContainer, recipe.categories)
        fillContainer(binding.ingredientsInnerContainer, recipe.ingredients)
        fillContainer(binding.stepsInnerContainer, recipe.steps)
    }

    private fun fillContainer(container: LinearLayout, list: List<String>) {
        list.forEach {
            createCustomEditText().apply {
                setText(it)
                container.addView(this)
            }
        }
    }

    private fun createCustomEditText(hintText: String = ""): EditText {
        return EditText(context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                val marginStart = 24.dpToPx(context)
                val marginTop = 8.dpToPx(context)
                val marginEnd = 8.dpToPx(context)
                setMargins(marginStart, marginTop, marginEnd, 0)
            }
            setTextAppearance(R.style.AuthFields)
            background =
                ContextCompat.getDrawable(requireContext(), R.drawable.base_field_shapes)
            setPadding(
                16.dpToPx(context),
                10.dpToPx(context),
                16.dpToPx(context),
                10.dpToPx(context)
            )
            hint = hintText
            textSize = 16f
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.progressBar.visibility = View.VISIBLE
            binding.scrollView.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.scrollView.visibility = View.VISIBLE
        }
    }

    companion object {

        fun newInstance(recipeId: String): RecipeFormFragment {
            val fragment = RecipeFormFragment()
            val args = Bundle()
            args.putString(ARG_RECIPE, recipeId)
            fragment.arguments = args
            return fragment
        }
    }

}