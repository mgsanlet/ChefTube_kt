package com.mgsanlet.cheftube.ui.view.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.model.Recipe
import com.mgsanlet.cheftube.databinding.FragmentRecipeFeedBinding
import com.mgsanlet.cheftube.ui.adapter.RecipeFeedAdapter
import com.mgsanlet.cheftube.ui.view.base.BaseFragment
import com.mgsanlet.cheftube.ui.viewmodel.home.RecipeFeedState
import com.mgsanlet.cheftube.ui.viewmodel.home.RecipeFeedViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Un fragmento que muestra una lista de recetas. Cada receta se muestra con su título y una imagen.
 * Cuando se hace clic en una receta, el fragmento navega a una vista detallada de la receta.
 *
 * @author MarioG
 */
@AndroidEntryPoint
class RecipeFeedFragment @Inject constructor() : BaseFragment<FragmentRecipeFeedBinding>() {

    private val viewModel: RecipeFeedViewModel by viewModels()

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentRecipeFeedBinding = FragmentRecipeFeedBinding.inflate(inflater, container, false)

    override fun setUpObservers() {
        viewModel.recipeFeedState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RecipeFeedState.InitialLoad -> {
                    showLoading(false)
                    loadRecycler(state.recipeList)
                }

                is RecipeFeedState.Loading -> showLoading(true)

                is RecipeFeedState.NoResults -> {
                    showLoading(false)
                    showNoResults()
                }

                is RecipeFeedState.SomeResults -> {
                    showLoading(false)
                    loadRecycler(state.recipeList)
                    showResultNumber(state.recipeList.size)
                }
            }
        }
    }

    override fun setUpListeners() {
        binding.searchButton.setOnClickListener { showSearchDialog() }
    }

    override fun setUpViewProperties() {
        setUpProgressBar(binding.progressBar)
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

    private fun showSearchDialog() {
        val searchDialogBuilder = AlertDialog.Builder(requireContext())

        // Inflar un diseño personalizado para el diálogo de búsqueda
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_search, null)
        searchDialogBuilder.setView(dialogView)

        // Obtener una referencia a los elementos de la interfaz de usuario en el diseño personalizado
        val input = dialogView.findViewById<EditText>(R.id.queryEditText)
        val okButton = dialogView.findViewById<Button>(R.id.okButton)

        val searchDialog = searchDialogBuilder.create()
        searchDialog.show()

        okButton.setOnClickListener {
            val query = input.text.toString().trim { it <= ' ' }
            // Filtrar las recetas según la consulta de entrada
            viewModel.filterRecipesByIngredient(requireContext(), query)
            searchDialog.dismiss() // Descartar el diálogo después de la búsqueda

        }
    }
}