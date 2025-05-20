package com.mgsanlet.cheftube.ui.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentRecipeFeedBinding
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import com.mgsanlet.cheftube.ui.adapter.RecipeFeedAdapter
import com.mgsanlet.cheftube.ui.util.Constants.ARG_RECIPE_LIST
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.util.setCustomStyle
import com.mgsanlet.cheftube.ui.view.base.BaseFragment
import com.mgsanlet.cheftube.ui.view.dialogs.SearchDialog
import com.mgsanlet.cheftube.ui.viewmodel.home.RecipeFeedState
import com.mgsanlet.cheftube.ui.viewmodel.home.RecipeFeedViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.mgsanlet.cheftube.domain.model.DomainRecipe as Recipe

/**
 * Un fragmento que muestra una lista de recetas. Cada receta se muestra con su título y una imagen.
 * Cuando se hace clic en una receta, el fragmento navega a una vista detallada de la receta.
 *
 * @author MarioG
 */
@AndroidEntryPoint
class RecipeFeedFragment @Inject constructor() : BaseFragment<FragmentRecipeFeedBinding>() {

    private val viewModel: RecipeFeedViewModel by viewModels()
    private lateinit var searchDialog: SearchDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchDialog = SearchDialog(requireContext())
        searchDialog.setOnSearchQuerySubmittedListener { searchParams ->
            viewModel.performSearch(searchParams)
        }
        arguments?.let{
            val recipeIds = it.getStringArrayList(ARG_RECIPE_LIST)
            recipeIds?.let{
                viewModel.loadSentRecipes(recipeIds)
            } ?: viewModel.loadInitialRecipes()
        } ?: viewModel.loadInitialRecipes()
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentRecipeFeedBinding = FragmentRecipeFeedBinding.inflate(inflater, container, false)

    override fun setUpObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RecipeFeedState.InitialLoad -> {
                    showLoading(false)
                    loadRecycler(state.recipeList)
                }

                is RecipeFeedState.Loading -> showLoading(true)

                is RecipeFeedState.SomeResults -> {
                    showLoading(false)
                    loadRecycler(state.recipeList)
                    showResultNumber(state.recipeList.size)
                }

                is RecipeFeedState.Error ->
                    when (state.error) {
                        is RecipeError.NoResults -> {
                            showNoResults()
                            showLoading(false)
                        }

                        else -> Toast.makeText(
                            context,
                            state.error.asMessage(requireContext()),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    override fun setUpListeners() {
        binding.searchButton.setOnClickListener {
            searchDialog.show()
        }
    }

    override fun setUpViewProperties() {
        binding.progressBar.setCustomStyle(requireContext())
        binding.recipeFeedRecyclerView.setLayoutManager(LinearLayoutManager(context))

        binding.noResultsTextView.visibility = View.GONE // Oculta mensaje de sin resultados-
        binding.recipeFeedRecyclerView.visibility = View.VISIBLE // Mostrar RecyclerView
    }

    private fun loadRecycler(recipeList: List<Recipe>) {
        val recipeAdapter = RecipeFeedAdapter(
            requireContext(), recipeList, parentFragmentManager
        )
        binding.recipeFeedRecyclerView.adapter = recipeAdapter
        binding.noResultsTextView.visibility = View.GONE
        binding.recipeFeedRecyclerView.visibility = View.VISIBLE
    }

    private fun showNoResults() {
        binding.noResultsTextView.visibility = View.VISIBLE
        binding.recipeFeedRecyclerView.visibility = View.GONE
    }

    private fun showResultNumber(size: Int) {
        Toast.makeText(context, getString(R.string.results) + " $size", Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.progressBar.visibility = View.VISIBLE
            binding.recipeFeedRecyclerView.visibility = View.GONE
            binding.noResultsTextView.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }



    companion object {

        fun newInstance(recipeIds: ArrayList<String>): RecipeFeedFragment {
            val fragment = RecipeFeedFragment()
            val args = Bundle()
            args.putStringArrayList(ARG_RECIPE_LIST, recipeIds)
            fragment.arguments = args
            return fragment
        }
    }
}