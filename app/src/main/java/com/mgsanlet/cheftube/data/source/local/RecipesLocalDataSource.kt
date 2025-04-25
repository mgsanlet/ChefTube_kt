package com.mgsanlet.cheftube.data.source.local

import android.content.Context
import com.mgsanlet.cheftube.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.mgsanlet.cheftube.domain.model.DomainRecipe as Recipe
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

/**
 * Singleton class that manages a collection of Recipe objects.
 * Provides methods to initialize, register, and filter recipes.
 * @author MarioG
 */
class RecipesLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val recipeList = ArrayList<Recipe>()
    private val recipeMap = mutableMapOf<String, Recipe>()

    init {
        CoroutineScope(Dispatchers.Main).launch {
            addRecipesToList()
        }
    }

    private suspend fun addRecipesToList(): List<Recipe> = withContext(Dispatchers.IO) {
        val recipe1 = Recipe(
            "1",
            R.string.recipe_01,
            R.drawable.recipe_01,
            "https://www.youtube.com/embed/Lor3PbTSAho"
        )
        recipe1.ingredientsResIds.add(R.string.ingredient_01_01)
        recipe1.ingredientsResIds.add(R.string.ingredient_01_02)
        recipe1.ingredientsResIds.add(R.string.ingredient_01_03)
        recipe1.ingredientsResIds.add(R.string.ingredient_01_04)
        recipe1.stepsResIds.add(R.string.step_01_01)
        recipe1.stepsResIds.add(R.string.step_01_02)
        recipe1.stepsResIds.add(R.string.step_01_03)
        register(recipe1)

        val recipe2 = Recipe(
            "2",
            R.string.recipe_02,
            R.drawable.recipe_02,
            "https://www.youtube.com/embed/NBjsqZZ2Tc4"
        )
        recipe2.ingredientsResIds.add(R.string.ingredient_02_01)
        recipe2.ingredientsResIds.add(R.string.ingredient_02_02)
        recipe2.ingredientsResIds.add(R.string.ingredient_02_03)
        recipe2.ingredientsResIds.add(R.string.ingredient_02_04)
        recipe2.stepsResIds.add(R.string.step_02_01)
        recipe2.stepsResIds.add(R.string.step_02_02)
        recipe2.stepsResIds.add(R.string.step_02_03)
        register(recipe2)

        val recipe3 = Recipe(
            "3",
            R.string.recipe_03,
            R.drawable.recipe_03,
            "https://www.youtube.com/embed/E0n2ZhOw-MI"
        )
        recipe3.ingredientsResIds.add(R.string.ingredient_03_01)
        recipe3.ingredientsResIds.add(R.string.ingredient_03_02)
        recipe3.ingredientsResIds.add(R.string.ingredient_03_03)
        recipe3.stepsResIds.add(R.string.step_03_01)
        recipe3.stepsResIds.add(R.string.step_03_02)
        recipe3.stepsResIds.add(R.string.step_03_03)
        register(recipe3)

        val recipe4 = Recipe(
            "4",
            R.string.recipe_04,
            R.drawable.recipe_04,
            "https://www.youtube.com/embed/VDX-YXCARpM"
        )
        recipe4.ingredientsResIds.add(R.string.ingredient_04_01)
        recipe4.ingredientsResIds.add(R.string.ingredient_04_02)
        recipe4.ingredientsResIds.add(R.string.ingredient_04_03)
        recipe4.stepsResIds.add(R.string.step_04_01)
        recipe4.stepsResIds.add(R.string.step_04_02)
        recipe4.stepsResIds.add(R.string.step_04_03)
        register(recipe4)

        val recipe5 = Recipe(
            "5",
            R.string.recipe_05,
            R.drawable.recipe_05,
            "https://www.youtube.com/embed/OG4pJdxxmj4"
        )
        recipe5.ingredientsResIds.add(R.string.ingredient_05_01)
        recipe5.ingredientsResIds.add(R.string.ingredient_05_02)
        recipe5.ingredientsResIds.add(R.string.ingredient_05_03)
        recipe5.stepsResIds.add(R.string.step_05_01)
        recipe5.stepsResIds.add(R.string.step_05_02)
        recipe5.stepsResIds.add(R.string.step_05_03)
        register(recipe5)

        val recipe6 = Recipe(
            "6",
            R.string.recipe_06,
            R.drawable.recipe_06,
            "https://www.youtube.com/embed/168HrdzakaA"
        )
        recipe6.ingredientsResIds.add(R.string.ingredient_06_01)
        recipe6.ingredientsResIds.add(R.string.ingredient_06_02)
        recipe6.ingredientsResIds.add(R.string.ingredient_06_03)
        recipe6.stepsResIds.add(R.string.step_06_01)
        recipe6.stepsResIds.add(R.string.step_06_02)
        recipe6.stepsResIds.add(R.string.step_06_03)
        register(recipe6)

        val recipe7 = Recipe(
            "7",
            R.string.recipe_07,
            R.drawable.recipe_07,
            "https://www.youtube.com/embed/Sjx3J6du-gI"
        )
        recipe7.ingredientsResIds.add(R.string.ingredient_07_01)
        recipe7.ingredientsResIds.add(R.string.ingredient_07_02)
        recipe7.ingredientsResIds.add(R.string.ingredient_07_03)
        recipe7.stepsResIds.add(R.string.step_07_01)
        recipe7.stepsResIds.add(R.string.step_07_02)
        recipe7.stepsResIds.add(R.string.step_07_03)
        register(recipe7)

        return@withContext recipeList
    }

    /**
     * Registers a new recipe to the singleton instance list.
     *
     * @param recipe the recipe to be added
     */
    private fun register(recipe: Recipe?) {
        if (recipe != null) {
            recipeList.add(recipe)
            recipeMap[recipe.id] = recipe
        }
    }

    /**
     * Filters recipes based on a search query.
     * The search checks ingredient names and is case-insensitive.
     *
     * @param query   the search query
     * @return a list of recipes that match the query
     */
     fun filterRecipesByIngredient(query: String): List<Recipe> {
        val addedRecipes: MutableSet<Recipe> = HashSet()
        val filteredRecipes: MutableList<Recipe> = ArrayList()
        val lowerCaseQuery = query.lowercase(Locale.getDefault())

        // -Checking for matching recipes-
        for (recipe in recipeList) {
            // Comprobar si algún ingrediente coincide con la consulta
            for (ingredientId in recipe.ingredientsResIds) {
                val ingredientName = context.resources.getString(ingredientId)
                if (ingredientName.lowercase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    if (addedRecipes.add(recipe)) {
                        filteredRecipes.add(recipe)
                    }
                    break
                }
            }
        }
        return filteredRecipes
    }


    suspend fun getById(recipeId: String): Recipe? = withContext(Dispatchers.IO) {
        return@withContext recipeMap[recipeId]
    }

    suspend fun getAll(): List<Recipe> = withContext(Dispatchers.IO) {
        return@withContext recipeList
    }
}
