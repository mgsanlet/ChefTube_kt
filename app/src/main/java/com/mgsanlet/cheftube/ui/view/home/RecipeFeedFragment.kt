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
import com.mgsanlet.cheftube.ui.util.hideKeyboard
import com.mgsanlet.cheftube.ui.view.base.BaseFragment
import com.mgsanlet.cheftube.ui.view.dialogs.LoadingDialog
import com.mgsanlet.cheftube.ui.view.dialogs.SearchDialog
import com.mgsanlet.cheftube.ui.viewmodel.home.RecipeFeedState
import com.mgsanlet.cheftube.ui.viewmodel.home.RecipeFeedViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.mgsanlet.cheftube.domain.model.DomainRecipe as Recipe

/**
 * Fragmento que muestra una lista de recetas en un RecyclerView con funcionalidad de búsqueda.
 * 
 * Este fragmento es responsable de:
 * - Mostrar una lista de recetas con imágenes y títulos
 * - Permitir la búsqueda de recetas por diferentes criterios
 * - Manejar los estados de carga y error
 * - Navegar al detalle de una receta al hacer clic en ella
 * 
 * Utiliza un ViewModel para separar la lógica de negocio de la interfaz de usuario.
 * 
 * @constructor Crea una nueva instancia del fragmento de feed de recetas
 */
@AndroidEntryPoint
class RecipeFeedFragment @Inject constructor() : BaseFragment<FragmentRecipeFeedBinding>() {

    /** ViewModel que maneja la lógica de negocio del feed de recetas. */
    private val viewModel: RecipeFeedViewModel by viewModels()
    
    /** Diálogo de búsqueda para filtrar recetas. */
    private lateinit var searchDialog: SearchDialog

    /**
     * Se llama después de que la vista ha sido creada.
     * 
     * Inicializa el diálogo de búsqueda y carga las recetas iniciales o las recetas
     * enviadas a través de los argumentos.
     * 
     * @param view La vista devuelta por onCreateView
     * @param savedInstanceState Estado previamente guardado de la instancia
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchDialog = SearchDialog(requireContext())
        searchDialog.setOnSearchQuerySubmittedListener { searchParams ->
            view.hideKeyboard()
            viewModel.performSearch(searchParams)
        }
        arguments?.let{
            val recipeIds = it.getStringArrayList(ARG_RECIPE_LIST)
            recipeIds?.let{
                viewModel.loadSentRecipes(recipeIds)
            } ?: viewModel.loadInitialRecipes()
        } ?: viewModel.loadInitialRecipes()
    }

    /**
     * Infla y devuelve el binding para el layout del fragmento.
     * 
     * @param inflater El LayoutInflater usado para inflar la vista
     * @param container El ViewGroup padre al que se adjuntará la vista
     * @return Instancia del binding inflado
     */
    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentRecipeFeedBinding = FragmentRecipeFeedBinding.inflate(inflater, container, false)

    /**
     * Configura los observadores para los estados del ViewModel.
     * 
     * Maneja los diferentes estados de la UI:
     * - InitialLoad: Muestra la lista inicial de recetas
     * - Loading: Muestra un indicador de carga
     * - SomeResults: Muestra los resultados de búsqueda
     * - Error: Muestra mensajes de error apropiados
     */
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

                is RecipeFeedState.Error -> {
                    showLoading(false)
                    when (state.error) {
                        is RecipeError.NoResults -> {
                            showNoResults()
                        }
                        else -> {
                            Toast.makeText(
                                context,
                                state.error.asMessage(requireContext()),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * Configura los listeners para los elementos de la interfaz de usuario.
     * 
     * Actualmente configura el botón de búsqueda para mostrar el diálogo de búsqueda.
     */
    override fun setUpListeners() {
        binding.searchButton.setOnClickListener {
            searchDialog.show()
        }
    }

    /**
     * Configura las propiedades iniciales de la vista.
     * 
     * Inicializa el RecyclerView con un LinearLayoutManager y oculta el mensaje de "no hay resultados".
     */
    override fun setUpViewProperties() {
        binding.recipeFeedRecyclerView.setLayoutManager(LinearLayoutManager(context))
        binding.noResultsTextView.visibility = View.GONE // Oculta mensaje de sin resultados
        binding.recipeFeedRecyclerView.visibility = View.VISIBLE // Mostrar RecyclerView
    }

    /**
     * Carga la lista de recetas en el RecyclerView.
     * 
     * Crea un nuevo adaptador con la lista de recetas y lo asigna al RecyclerView.
     * 
     * @param recipeList Lista de recetas a mostrar
     */
    private fun loadRecycler(recipeList: List<Recipe>) {
        val recipeAdapter = RecipeFeedAdapter(
            requireContext(), recipeList, parentFragmentManager
        )
        binding.recipeFeedRecyclerView.adapter = recipeAdapter
        binding.noResultsTextView.visibility = View.GONE
        binding.recipeFeedRecyclerView.visibility = View.VISIBLE
    }

    /**
     * Muestra un mensaje indicando que no se encontraron resultados.
     * 
     * Oculta el RecyclerView y muestra un mensaje al usuario.
     */
    private fun showNoResults() {
        binding.noResultsTextView.visibility = View.VISIBLE
        binding.recipeFeedRecyclerView.visibility = View.GONE
    }

    /**
     * Muestra el número de resultados encontrados.
     * 
     * Muestra un mensaje al usuario con el número de resultados encontrados.
     * 
     * @param resultNumber Número de resultados a mostrar
     */
    private fun showResultNumber(resultNumber: Int) {
        Toast.makeText(context, getString(R.string.results) + " $resultNumber", Toast.LENGTH_SHORT).show()
    }

    /**
     * Muestra u oculta el indicador de carga.
     * 
     * Muestra u oculta el indicador de carga según el parámetro show.
     * 
     * @param show true para mostrar el indicador de carga, false para ocultarlo
     */
    private fun showLoading(show: Boolean) {
        if (show) {
            LoadingDialog.show(requireContext(), parentFragmentManager)
            binding.recipeFeedRecyclerView.visibility = View.GONE
            binding.noResultsTextView.visibility = View.GONE
        } else {
            LoadingDialog.dismiss(parentFragmentManager)
        }
    }

    /**
     * Se llama cuando la vista del fragmento está a punto de ser destruida.
     * 
     * Realiza la limpieza de recursos como cerrar el diálogo de carga si está visible.
     * Es importante llamar al método de la superclase para asegurar una limpieza adecuada.
     */
    override fun onDestroyView() {
        LoadingDialog.dismiss(parentFragmentManager)
        super.onDestroyView()
    }


    companion object {

        /**
         * Crea una nueva instancia del fragmento con una lista de IDs de recetas.
         *
         * @param recipeIds Lista de IDs de recetas a mostrar en el feed. Si la lista está vacía,
         *                 se cargarán las recetas iniciales según la configuración por defecto.
         * @return Nueva instancia de RecipeFeedFragment configurada con los IDs proporcionados
         */
        fun newInstance(recipeIds: ArrayList<String>): RecipeFeedFragment {
            val fragment = RecipeFeedFragment()
            val args = Bundle()
            args.putStringArrayList(ARG_RECIPE_LIST, recipeIds)
            fragment.arguments = args
            return fragment
        }
    }
}