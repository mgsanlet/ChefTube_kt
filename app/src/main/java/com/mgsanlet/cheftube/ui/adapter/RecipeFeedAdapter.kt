package com.mgsanlet.cheftube.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.model.Recipe
import com.mgsanlet.cheftube.databinding.ItemRecipeBinding
import com.mgsanlet.cheftube.utils.FragmentNavigator
import com.mgsanlet.cheftube.ui.view.home.RecipeDetailFragment
import com.mgsanlet.cheftube.ui.view.home.RecipeFeedFragment

/**
 * Adaptador para mostrar una lista de recetas en un RecyclerView.
 * Este adaptador vincula los datos de las recetas con las vistas en el RecyclerView.
 * @author MarioG
 */
class RecipeFeedAdapter(
    private val mContext: Context,
    private val recipeList: List<Recipe>,
    private val fragmentManager: FragmentManager,
) : RecyclerView.Adapter<RecipeFeedAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(var binding: ItemRecipeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]
        val binding = holder.binding

        binding.titleTextView.setText(recipe.ttlRId)
        // Cargar imágenes de recetas con esquinas redondeadas usando Glide
        Glide.with(mContext)
            .load(recipe.imgRId)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(40)))
            .into(binding.imageView)

        binding.imageView.setOnClickListener { navToRecipeDetail(recipe) }
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    /**
     * Gestiona la navegación a la vista detallada de la receta seleccionada.
     *
     * @param recipe La receta cuyos detalles se mostrarán.
     */
    private fun navToRecipeDetail(recipe: Recipe) {
        // Obtener el fragmento visible actual
        val currentFragment = fragmentManager
            .findFragmentById(R.id.fragmentContainerView) as RecipeFeedFragment?

        if (currentFragment != null && currentFragment.isVisible) {
            val detailFragment = RecipeDetailFragment.newInstance(recipe)

            FragmentNavigator.loadFragmentInstance(
                null, currentFragment, detailFragment, R.id.fragmentContainerView
            )
        }
    }
}
