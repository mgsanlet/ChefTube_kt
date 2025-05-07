package com.mgsanlet.cheftube.ui.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentRecipeFormBinding
import com.mgsanlet.cheftube.domain.model.DomainRecipe
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
                viewModel.trySaveRecipe(
                    binding.titleEditText.text.toString(),
                    binding.videoUrlEditText.text.toString(),
                    binding.recipeImageView.toString(),
                    binding.durationEditText.text.toString(),
                    binding.difficultySpinner.selectedItemPosition,
                    binding.categoriesInnerContainer.asStringList(),
                    binding.ingredientsInnerContainer.asStringList(),
                    binding.stepsInnerContainer.asStringList()
                )
            }
        }
        binding.cancelButton.setOnClickListener {
            (activity as? HomeActivity)?.onBackPressedDispatcher?.onBackPressed()
        }
        binding.loadImageButton.setOnClickListener {
            Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show()
        }
        binding.categoriesAddButton.setOnClickListener {
            if (binding.categoriesInnerContainer.childCount < 10){
                binding.categoriesInnerContainer.addView(
                    createCustomEditText()
                )
            }else{
                Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show()
            }
        }
        binding.ingredientsAddButton.setOnClickListener {
            if (binding.ingredientsInnerContainer.childCount < 10){
                binding.ingredientsInnerContainer.addView(
                    createCustomEditText()
                )
            }else{
                Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show()
            }
            binding.ingredientsInnerContainer.addView(
                createCustomEditText()
            )
        }
        binding.stepsAddButton.setOnClickListener {
            if (binding.stepsInnerContainer.childCount < 10){
                binding.stepsInnerContainer.addView(
                    createCustomEditText()
                )
            }else{
                Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show()
            }
        }
        binding.categoriesRemoveButton.setOnClickListener { binding.categoriesInnerContainer.removeLastChild() }
        binding.ingredientsRemoveButton.setOnClickListener { binding.ingredientsInnerContainer.removeLastChild() }
        binding.stepsRemoveButton.setOnClickListener { binding.stepsInnerContainer.removeLastChild() }
    }

    override fun isValidViewInput(): Boolean {
        var isValid = true

        // Comprobamos requerimientos de longitud y mínimos y mostramos errores
        if (binding.titleEditText.text.toString().length !in 5..30) {
            isValid = false
        }
        try {
            if (binding.durationEditText.text.toString().toInt() <= 0) {
                binding.durationEditText.error = "TODO"
                isValid = false
            }
        } catch (_: Exception) {
            binding.durationEditText.error = "TODO"
            isValid = false
        }


        if (binding.categoriesInnerContainer.childCount <= 0) {
            Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (binding.ingredientsInnerContainer.childCount <= 0) {
            Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        var requiredFields = listOf(
            binding.titleEditText,
            binding.videoUrlEditText,
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
        return isValid
    }

    private fun showRecipeData(recipe: DomainRecipe) {
        binding.titleEditText.setText(recipe.title)
        binding.videoUrlEditText.setText(recipe.videoUrl)
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

    private fun createCustomEditText(): EditText {
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
            textSize = 20f
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