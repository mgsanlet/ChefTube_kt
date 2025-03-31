package com.mgsanlet.cheftube.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.model.Recipe
import com.mgsanlet.cheftube.utils.FragmentNavigator
import com.mgsanlet.cheftube.view.adapter.RecipeFeedAdapter.RecipeViewHolder
import com.mgsanlet.cheftube.view.ui.home.RecipeDetailFragment
import com.mgsanlet.cheftube.view.ui.home.RecipeFeedFragment

/**
 * Adaptador para mostrar una lista de recetas en un RecyclerView.
 * Este adaptador vincula los datos de las recetas con las vistas en el RecyclerView.
 * @author MarioG
 */
class RecipeFeedAdapter(
    private val recipeList: List<Recipe>,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<RecipeViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipeList[position])
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
            .findFragmentById(R.id.mainFrContainer) as RecipeFeedFragment?

        if (currentFragment != null && currentFragment.isVisible) {
            val detailFragment = RecipeDetailFragment.newInstance(recipe)

            FragmentNavigator.loadFragmentInstance(
                null, currentFragment, detailFragment, R.id.mainFrContainer
            )
        }
    }

    inner class RecipeViewHolder(recipeView: View) : RecyclerView.ViewHolder(recipeView) {
        private var title: TextView = recipeView.findViewById(R.id.recipeTitle)
        private var image: ImageView = recipeView.findViewById(R.id.recipeImage)

        fun bind(recipe: Recipe) {
            title.setText(recipe.ttlRId)
            // Cargar imágenes de recetas con esquinas redondeadas usando Glide
            Glide.with(itemView.context)
                .load(recipe.imgRId)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(40)))
                .into(image)

            image.setOnClickListener { navToRecipeDetail(recipe) }
        }
    }
}
