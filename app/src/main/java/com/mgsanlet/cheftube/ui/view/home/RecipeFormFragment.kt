package com.mgsanlet.cheftube.ui.view.home

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentRecipeFormBinding
import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import com.mgsanlet.cheftube.domain.util.error.UserError
import com.mgsanlet.cheftube.ui.adapter.BaseSpinnerAdapter
import com.mgsanlet.cheftube.ui.util.Constants.ARG_RECIPE
import com.mgsanlet.cheftube.ui.util.FragmentNavigator
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.util.asStringList
import com.mgsanlet.cheftube.ui.util.dpToPx
import com.mgsanlet.cheftube.ui.util.removeLastChild
import com.mgsanlet.cheftube.ui.view.base.BaseFormFragment
import com.mgsanlet.cheftube.ui.view.customviews.VideoUrlState
import com.mgsanlet.cheftube.ui.view.dialogs.LoadingDialog
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

    private var isNewRecipe: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            val recipeId = bundle.getString(ARG_RECIPE)
            if (!recipeId.isNullOrEmpty()) {
                isNewRecipe = false
                viewModel.loadRecipe(recipeId)
            }
        }

        // Show delete button only for existing recipes
        binding.deleteButton.visibility = if (isNewRecipe) View.GONE else View.VISIBLE
    }

    override fun setUpViewProperties() {
        binding.difficultySpinner.adapter = BaseSpinnerAdapter(
            requireContext(),
            resources.getStringArray(R.array.difficulty).toList()
        )
        binding.imageLoaderView.setActivityResultRegistry(requireActivity().activityResultRegistry)
    }

    override fun setUpObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RecipeFormState.Loading -> showLoading(true)

                is RecipeFormState.LoadSuccess -> {
                    showLoading(false)
                    viewModel.recipe.value?.let { showRecipeData(it) }
                }

                is RecipeFormState.Error -> {
                    showLoading(false)
                    var errorMessage = ""
                    when (state.error) {
                        is RecipeError -> errorMessage = state.error.asMessage(requireContext())

                        is UserError -> errorMessage = state.error.asMessage(requireContext())
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }

                is RecipeFormState.SaveSuccess -> {
                    Toast.makeText(context, getString(R.string.data_saved), Toast.LENGTH_LONG)
                        .show()
                    try {
                        val detailFragment = RecipeDetailFragment.newInstance(
                            state.newRecipeId ?: (viewModel.recipe.value!!.id)
                        )
                        FragmentNavigator.loadFragmentInstance(
                            null, this, detailFragment, R.id.fragmentContainerView
                        )
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            getString(R.string.unknown_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                is RecipeFormState.DeleteSuccess -> {
                    showLoading(false)
                    Toast.makeText(context, getString(R.string.recipe_deleted), Toast.LENGTH_LONG)
                        .show()
                    FragmentNavigator.loadFragment(
                        null,
                        this,
                        RecipeFeedFragment(),
                        R.id.fragmentContainerView
                    )
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
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
                getString(R.string.delete_recipe_title)
            findViewById<TextView>(R.id.dialogMessageTextView).text =
                getString(R.string.delete_recipe_message)

            val positiveButton = findViewById<Button>(R.id.confirmButton)
            positiveButton.text = getString(R.string.delete)
            positiveButton.setOnClickListener {
                viewModel.deleteRecipe()
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

    override fun setUpListeners() {
        binding.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.saveButton.setOnClickListener {
            if (isValidViewInput()) {
                if (binding.videoLoaderView.state != VideoUrlState.INITIAL &&
                    binding.videoLoaderView.state != VideoUrlState.VALID
                ) {
                    binding.videoLoaderView.setError(getString(R.string.invalid_video_url))
                    binding.videoLoaderView.requestFocus()
                    return@setOnClickListener
                }
                if (!binding.imageLoaderView.validateNotInitial()) {
                    return@setOnClickListener
                }

                viewModel.trySaveRecipe(
                    binding.titleEditText.text.toString(),
                    binding.videoLoaderView.getEmbedVideoUrl() ?: binding.videoLoaderView.getText(),
                    binding.imageLoaderView.getNewImage(),
                    binding.durationEditText.text.toString().toInt(),
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

        binding.categoriesAddButton.setOnClickListener {
            if (binding.categoriesInnerContainer.childCount < 10) {
                val newView = createCustomEditText(getString(R.string.new_category))
                binding.categoriesInnerContainer.addView(newView)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    binding.scrollView.scrollToDescendant(newView)
                }
                newView.requestFocus()
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
                val newView = createCustomEditText(getString(R.string.new_ingredient))
                binding.ingredientsInnerContainer.addView(newView)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    binding.scrollView.scrollToDescendant(newView)
                }
                newView.requestFocus()
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
                val newView = createCustomEditText(getString(R.string.new_step))
                binding.stepsInnerContainer.addView(newView)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    binding.scrollView.scrollToDescendant(newView)
                }
                newView.requestFocus()
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.maximum_10_steps),
                    Toast.LENGTH_SHORT
                )
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
        binding.videoLoaderView.setText(recipe.videoUrl)
        binding.imageLoaderView.loadUrl(recipe.imageUrl)
        binding.durationEditText.setText(recipe.durationMinutes.toString())
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
                val marginEnd = 24.dpToPx(context)
                setMargins(marginStart, marginTop, marginEnd, 0)
            }
            setTextAppearance(R.style.ChefTubeEditText)
            background =
                ContextCompat.getDrawable(requireContext(), R.drawable.shape_round_corner_15)
            backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.primary_green)
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
            binding.scrollView.visibility = View.INVISIBLE
            binding.submitContainer.visibility = View.INVISIBLE
            LoadingDialog.show(requireContext(), parentFragmentManager)
        } else {
            binding.scrollView.visibility = View.VISIBLE
            binding.submitContainer.visibility = View.VISIBLE
            LoadingDialog.dismiss(parentFragmentManager)
        }
    }

    override fun onDestroyView() {
        LoadingDialog.dismiss(parentFragmentManager)
        super.onDestroyView()
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