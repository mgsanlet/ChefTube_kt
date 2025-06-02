package com.mgsanlet.cheftube.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.ItemRecipeBinding
import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.ui.util.FragmentNavigator
import com.mgsanlet.cheftube.ui.util.loadUrl
import com.mgsanlet.cheftube.ui.util.loadUrlToCircle
import com.mgsanlet.cheftube.ui.view.home.ProfileFragment
import com.mgsanlet.cheftube.ui.view.home.RecipeDetailFragment
import com.mgsanlet.cheftube.ui.view.home.RecipeFeedFragment

/**
 * Adaptador para mostrar una lista de recetas en un RecyclerView.
 * Este adaptador vincula los datos de las recetas con las vistas en el RecyclerView,
 * permitiendo la navegación a los detalles de la receta y al perfil del autor.
 *
 * @property mContext Contexto de la aplicación
 * @property recipeList Lista de recetas a mostrar
 * @property fragmentManager Gestor de fragmentos para la navegación
 */
class RecipeFeedAdapter(
    private val mContext: Context,
    private val recipeList: List<DomainRecipe>,
    private val fragmentManager: FragmentManager,
) : RecyclerView.Adapter<RecipeFeedAdapter.RecipeViewHolder>() {

    /**
     * ViewHolder que contiene las vistas de un elemento de receta en el feed.
     *
     * @property binding Binding que contiene las vistas del elemento de receta
     */
    inner class RecipeViewHolder(var binding: ItemRecipeBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * Crea una nueva instancia de ViewHolder.
     *
     * @param parent El ViewGroup en el que se añadirá la nueva vista
     * @param viewType El tipo de vista del nuevo View
     * @return Un nuevo ViewHolder que contiene la vista del elemento de receta
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(
            LayoutInflater.from(mContext), parent, false
        )
        return RecipeViewHolder(binding)
    }

    /**
     * Actualiza el contenido del ViewHolder en la posición dada.
     *
     * @param holder El ViewHolder que debe actualizarse
     * @param position La posición del elemento en el conjunto de datos
     */
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]
        val binding = holder.binding

        binding.titleTextView.text = recipe.title
        // Cargar imágenes de recetas con esquinas redondeadas usando Glide
        binding.imageView.loadUrl(recipe.imageUrl, mContext)
        binding.imageView.setOnClickListener { navToRecipeDetail(recipe.id) }

        recipe.author?.let { author ->
            binding.authorTextView.text = author.username
            if (author.profilePictureUrl.isNotBlank()) {
                binding.authorImageView.loadUrlToCircle(author.profilePictureUrl, mContext)
            }
            binding.authorTag.setOnClickListener {
                navToAuthorProfile(author.id)
            }
        }

    }

    /**
     * Devuelve el número total de elementos en el conjunto de datos.
     *
     * @return Número total de recetas
     */
    override fun getItemCount(): Int = recipeList.size

    /**
     * Navega a la pantalla de detalle de una receta.
     *
     * @param recipeId ID de la receta a mostrar
     */
    private fun navToRecipeDetail(recipeId: String) {
        // Obtener el fragmento visible actual
        val currentFragment =
            fragmentManager.findFragmentById(R.id.fragmentContainerView) as RecipeFeedFragment?

        if (currentFragment != null && currentFragment.isVisible) {
            val detailFragment = RecipeDetailFragment.newInstance(recipeId)

            FragmentNavigator.loadFragmentInstance(
                null, currentFragment, detailFragment, R.id.fragmentContainerView
            )
        }
    }

    /**
     * Navega al perfil del autor de una receta.
     *
     * @param authorId ID del autor de la receta
     */
    private fun navToAuthorProfile(authorId: String) {
        // Obtener el fragmento visible actual
        val currentFragment =
            fragmentManager.findFragmentById(R.id.fragmentContainerView) as RecipeFeedFragment?
        if (currentFragment != null && currentFragment.isVisible) {
            val profileFragment = ProfileFragment.newInstance(authorId)

            FragmentNavigator.loadFragmentInstance(
                null, currentFragment, profileFragment, R.id.fragmentContainerView
            )
        }
    }
}
