package com.mgsanlet.cheftube.ui.view.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mgsanlet.cheftube.ChefTubeApplication
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentRecipeFeedBinding
import com.mgsanlet.cheftube.ui.adapter.RecipeFeedAdapter
import com.mgsanlet.cheftube.ui.view.base.BaseFragment
import com.mgsanlet.cheftube.ui.viewmodel.home.RecipeFeedViewModel
import com.mgsanlet.cheftube.ui.viewmodel.home.RecipeFeedViewModelFactory

/**
 * Un fragmento que muestra una lista de recetas. Cada receta se muestra con su título y una imagen.
 * Cuando se hace clic en una receta, el fragmento navega a una vista detallada de la receta.
 *
 * @author MarioG
 */
class RecipeFeedFragment : BaseFragment<FragmentRecipeFeedBinding, RecipeFeedViewModel>() {

    private val _viewModel: RecipeFeedViewModel by viewModels {
        val app by lazy { ChefTubeApplication.getInstance(requireContext()) }
        RecipeFeedViewModelFactory(app)
    }

    override fun defineViewModel(): RecipeFeedViewModel = _viewModel

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentRecipeFeedBinding = FragmentRecipeFeedBinding.inflate(inflater, container, false)

    override fun setUpObservers() {
        viewModel.recipeList.observe(viewLifecycleOwner) {
//            Toast.makeText(
//                context, getString(R.string.results) + it.size,
//                Toast.LENGTH_SHORT
//            ).show()
            if (it.isEmpty()) {
                displayNoResults() // Mostrar mensaje de sin resultados
            } else {
                val recipeAdapter = RecipeFeedAdapter(requireContext(), it, parentFragmentManager)
                binding.recipeFeedRecyclerView.adapter = recipeAdapter
                binding.noResultsTextView.visibility =
                    View.GONE // Ocultar mensaje de sin resultados
                binding.recipeFeedRecyclerView.visibility = View.VISIBLE // Mostrar RecyclerView
            }
        }
    }

    override fun setUpListeners() {
        binding.searchButton.setOnClickListener { setUpSearchBtn() }
    }

    override fun setUpViewProperties() {
        binding.recipeFeedRecyclerView.setLayoutManager(LinearLayoutManager(context))

        binding.noResultsTextView.visibility = View.GONE // Oculta mensaje de sin resultados-
        binding.recipeFeedRecyclerView.visibility = View.VISIBLE // Mostrar RecyclerView
    }

    /**
     * Muestra un mensaje indicando que no se encontraron resultados.
     */
    private fun displayNoResults() {
        binding.noResultsTextView.visibility = View.VISIBLE // Mostra el mensaje de sin resultados
        binding.recipeFeedRecyclerView.visibility = View.GONE // Ocultar el RecyclerView
    }

    private fun setUpSearchBtn() {
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