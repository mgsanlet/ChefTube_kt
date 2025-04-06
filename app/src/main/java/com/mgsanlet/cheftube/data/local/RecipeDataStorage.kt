package com.mgsanlet.cheftube.data.local

import android.content.Context
import androidx.media3.common.util.Log
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Singleton class that manages a collection of [Recipe] objects.
 * Provides methods to initialize, register, and filter recipes.
 * @author MarioG
 */
object RecipeDataStorage {
    private val recipeList = ArrayList<Recipe>()
    private val recipeMap = mutableMapOf<String, Recipe>()

    suspend fun addRecipesToList(): List<Recipe> = withContext(Dispatchers.IO) {
        val recipe1 = Recipe(
            "1",
            R.string.recipe_01, R.drawable.recipe_01,
            "https://www.youtube.com/embed/Lor3PbTSAho"
        )
        recipe1.addIngredient(R.string.ingredient_01_01)
        recipe1.addIngredient(R.string.ingredient_01_02)
        recipe1.addIngredient(R.string.ingredient_01_03)
        recipe1.addIngredient(R.string.ingredient_01_04)
        recipe1.addStep(R.string.step_01_01)
        recipe1.addStep(R.string.step_01_02)
        recipe1.addStep(R.string.step_01_03)
        register(recipe1)

        val recipe2 = Recipe(
            "2",
            R.string.recipe_02, R.drawable.recipe_02,
            "https://www.youtube.com/embed/NBjsqZZ2Tc4"
        )
        recipe2.addIngredient(R.string.ingredient_02_01)
        recipe2.addIngredient(R.string.ingredient_02_02)
        recipe2.addIngredient(R.string.ingredient_02_03)
        recipe2.addIngredient(R.string.ingredient_02_04)
        recipe2.addStep(R.string.step_02_01)
        recipe2.addStep(R.string.step_02_02)
        recipe2.addStep(R.string.step_02_03)
        register(recipe2)

        val recipe3 = Recipe(
            "3",
            R.string.recipe_03, R.drawable.recipe_03,
            "https://www.youtube.com/embed/E0n2ZhOw-MI"
        )
        recipe3.addIngredient(R.string.ingredient_03_01)
        recipe3.addIngredient(R.string.ingredient_03_02)
        recipe3.addIngredient(R.string.ingredient_03_03)
        recipe3.addStep(R.string.step_03_01)
        recipe3.addStep(R.string.step_03_02)
        recipe3.addStep(R.string.step_03_03)
        register(recipe3)

        val recipe4 = Recipe(
            "4",
            R.string.recipe_04, R.drawable.recipe_04,
            "https://www.youtube.com/embed/VDX-YXCARpM"
        )
        recipe4.addIngredient(R.string.ingredient_04_01)
        recipe4.addIngredient(R.string.ingredient_04_02)
        recipe4.addIngredient(R.string.ingredient_04_03)
        recipe4.addStep(R.string.step_04_01)
        recipe4.addStep(R.string.step_04_02)
        recipe4.addStep(R.string.step_04_03)
        register(recipe4)

        val recipe5 = Recipe(
            "5",
            R.string.recipe_05, R.drawable.recipe_05,
            "https://www.youtube.com/embed/OG4pJdxxmj4"
        )
        recipe5.addIngredient(R.string.ingredient_05_01)
        recipe5.addIngredient(R.string.ingredient_05_02)
        recipe5.addIngredient(R.string.ingredient_05_03)
        recipe5.addStep(R.string.step_05_01)
        recipe5.addStep(R.string.step_05_02)
        recipe5.addStep(R.string.step_05_03)
        register(recipe5)

        val recipe6 = Recipe(
            "6",
            R.string.recipe_06, R.drawable.recipe_06,
            "https://www.youtube.com/embed/168HrdzakaA"
        )
        recipe6.addIngredient(R.string.ingredient_06_01)
        recipe6.addIngredient(R.string.ingredient_06_02)
        recipe6.addIngredient(R.string.ingredient_06_03)
        recipe6.addStep(R.string.step_06_01)
        recipe6.addStep(R.string.step_06_02)
        recipe6.addStep(R.string.step_06_03)
        register(recipe6)

        val recipe7 = Recipe(
            "7",
            R.string.recipe_07, R.drawable.recipe_07,
            "https://www.youtube.com/embed/Sjx3J6du-gI"
        )
        recipe7.addIngredient(R.string.ingredient_07_01)
        recipe7.addIngredient(R.string.ingredient_07_02)
        recipe7.addIngredient(R.string.ingredient_07_03)
        recipe7.addStep(R.string.step_07_01)
        recipe7.addStep(R.string.step_07_02)
        recipe7.addStep(R.string.step_07_03)
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
     * @param context the Android context, used to resolve string resources
     * @param query   the search query
     * @return a list of recipes that match the query
     */
    suspend fun filterRecipesByIngredient(context: Context, query: String) : List<Recipe> = withContext(Dispatchers.IO) {
        val filteredRecipes: MutableList<Recipe> = ArrayList()

        // -Checking for matching recipes-
        for (recipe in recipeList) {
            if (recipe.matchesIngredientQuery(context, query)) {
                filteredRecipes.add(recipe)
            }
        }
        return@withContext filteredRecipes
    }

    suspend fun getById(recipeId: String): Recipe? = withContext(Dispatchers.IO) {
        return@withContext recipeMap[recipeId]
    }
}
