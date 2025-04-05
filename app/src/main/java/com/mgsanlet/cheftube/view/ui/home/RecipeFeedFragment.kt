package com.mgsanlet.cheftube.view.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.model.Recipe
import com.mgsanlet.cheftube.data.repository.RecipeRepository
import com.mgsanlet.cheftube.databinding.FragmentRecipeFeedBinding
import com.mgsanlet.cheftube.view.adapter.RecipeFeedAdapter

/**
 * Un fragmento que muestra una lista de recetas. Cada receta se muestra con su título y una imagen.
 * Cuando se hace clic en una receta, el fragmento navega a una vista detallada de la receta.
 *
 * @author MarioG
 */
class RecipeFeedFragment : Fragment() {

    private var _binding: FragmentRecipeFeedBinding? = null
    private val binding get() = _binding!!

    private var mRecipeList: List<Recipe>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeFeedBinding.inflate(inflater, container, false)

        binding.recipeFeedRecyclerView.setLayoutManager(LinearLayoutManager(context))

        // Listeners
        binding.searchButton.setOnClickListener { setUpSearchBtn() }

        // Verificar si recipeList es nulo y recuperar recetas predeterminadas de RecipeRepository
        verifyRecipeList()

        val adapter = RecipeFeedAdapter(mRecipeList!!, parentFragmentManager)
        binding.recipeFeedRecyclerView.setAdapter(adapter)
        binding.noResultsTextView.visibility = View.GONE // Oculta mensaje de sin resultados-
        binding.recipeFeedRecyclerView.visibility = View.VISIBLE // Mostrar RecyclerView

        return binding.root
    }

    /**
     * Verifica la disponibilidad de la lista de recetas, recurriendo a RecipeRepository si es necesario.
     */
    private fun verifyRecipeList() {
        if (this.mRecipeList == null) {
            this.mRecipeList = RecipeRepository.getInstance()
        }
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
            val filteredRecipes = RecipeRepository.getFilteredRecipes(context, query)

            Toast.makeText(
                context, getString(R.string.results) + filteredRecipes.size,
                Toast.LENGTH_SHORT
            ).show()
            if (filteredRecipes.isEmpty()) {
                displayNoResults() // Mostrar mensaje de sin resultados
            } else {
                val adapter = RecipeFeedAdapter(filteredRecipes, parentFragmentManager)
                binding.recipeFeedRecyclerView.adapter = adapter
                binding.noResultsTextView.visibility = View.GONE // Ocultar mensaje de sin resultados
                binding.recipeFeedRecyclerView.visibility = View.VISIBLE // Mostrar RecyclerView
            }

            searchDialog.dismiss() // Descartar el diálogo después de la búsqueda
        }
    }
}