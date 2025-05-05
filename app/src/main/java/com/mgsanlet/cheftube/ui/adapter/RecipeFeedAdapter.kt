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
import com.mgsanlet.cheftube.ui.view.home.ProfileFragment
import com.mgsanlet.cheftube.ui.view.home.RecipeDetailFragment
import com.mgsanlet.cheftube.ui.view.home.RecipeFeedFragment

/**
 * Adaptador para mostrar una lista de recetas en un RecyclerView.
 * Este adaptador vincula los datos de las recetas con las vistas en el RecyclerView.
 * @author MarioG
 */
class RecipeFeedAdapter(
    private val mContext: Context,
    private val recipeList: List<DomainRecipe>,
    private val fragmentManager: FragmentManager,
) : RecyclerView.Adapter<RecipeFeedAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(var binding: ItemRecipeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(
            LayoutInflater.from(mContext), parent, false
        )
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]
        val binding = holder.binding

        binding.titleTextView.setText(recipe.title)
        // Cargar imágenes de recetas con esquinas redondeadas usando Glide
        binding.imageView.loadUrl(recipe.imageUrl, mContext)
        binding.imageView.setOnClickListener { navToRecipeDetail(recipe.id) }

        recipe.author?.let { author ->
            binding.authorTextView.text = author.username
            //binding.authorImageView.loadUrl(author.avatarUrl, mContext)
            binding.authorTag.setOnClickListener {
                navToAuthorProfile(author.id)
            }
        }

    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

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
