package com.mgsanlet.cheftube.data.datasource

import android.content.Context
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Singleton class that manages a collection of [Recipe] objects.
 * Provides methods to initialize, register, and filter recipes.
 * @author MarioG
 */
class RecipeDataStorage {
    private val recipeList = ArrayList<Recipe>()

    suspend fun addRecipesToList(): List<Recipe> = withContext(Dispatchers.IO) {
        register(
            Recipe(
                R.string.recipe_01, R.drawable.recipe_01,
                "https://www.youtube.com/embed/Lor3PbTSAho"
            )
        )
        recipeList[0].addIngredient(R.string.ingredient_01_01)
        recipeList[0].addIngredient(R.string.ingredient_01_02)
        recipeList[0].addIngredient(R.string.ingredient_01_03)
        recipeList[0].addIngredient(R.string.ingredient_01_04)
        recipeList[0].addStep(R.string.step_01_01)
        recipeList[0].addStep(R.string.step_01_02)
        recipeList[0].addStep(R.string.step_01_03)
        register(
            Recipe(
                R.string.recipe_02, R.drawable.recipe_02,
                "https://www.youtube.com/embed/NBjsqZZ2Tc4"
            )
        )
        recipeList[1].addIngredient(R.string.ingredient_02_01)
        recipeList[1].addIngredient(R.string.ingredient_02_02)
        recipeList[1].addIngredient(R.string.ingredient_02_03)
        recipeList[1].addIngredient(R.string.ingredient_02_04)
        recipeList[1].addStep(R.string.step_02_01)
        recipeList[1].addStep(R.string.step_02_02)
        recipeList[1].addStep(R.string.step_02_03)
        register(
            Recipe(
                R.string.recipe_03, R.drawable.recipe_03,
                "https://www.youtube.com/embed/E0n2ZhOw-MI"
            )
        )
        recipeList[2].addIngredient(R.string.ingredient_03_01)
        recipeList[2].addIngredient(R.string.ingredient_03_02)
        recipeList[2].addIngredient(R.string.ingredient_03_03)
        recipeList[2].addStep(R.string.step_03_01)
        recipeList[2].addStep(R.string.step_03_02)
        recipeList[2].addStep(R.string.step_03_03)
        register(
            Recipe(
                R.string.recipe_04, R.drawable.recipe_04,
                "https://www.youtube.com/embed/VDX-YXCARpM"
            )
        )
        recipeList[3].addIngredient(R.string.ingredient_04_01)
        recipeList[3].addIngredient(R.string.ingredient_04_02)
        recipeList[3].addIngredient(R.string.ingredient_04_03)
        recipeList[3].addStep(R.string.step_04_01)
        recipeList[3].addStep(R.string.step_04_02)
        recipeList[3].addStep(R.string.step_04_03)
        register(
            Recipe(
                R.string.recipe_05, R.drawable.recipe_05,
                "https://www.youtube.com/embed/OG4pJdxxmj4"
            )
        )
        recipeList[4].addIngredient(R.string.ingredient_05_01)
        recipeList[4].addIngredient(R.string.ingredient_05_02)
        recipeList[4].addIngredient(R.string.ingredient_05_03)
        recipeList[4].addStep(R.string.step_05_01)
        recipeList[4].addStep(R.string.step_05_02)
        recipeList[4].addStep(R.string.step_05_03)
        register(
            Recipe(
                R.string.recipe_06, R.drawable.recipe_06,
                "https://www.youtube.com/embed/168HrdzakaA"
            )
        )
        recipeList[5].addIngredient(R.string.ingredient_06_01)
        recipeList[5].addIngredient(R.string.ingredient_06_02)
        recipeList[5].addIngredient(R.string.ingredient_06_03)
        recipeList[5].addStep(R.string.step_06_01)
        recipeList[5].addStep(R.string.step_06_02)
        recipeList[5].addStep(R.string.step_06_03)
        register(
            Recipe(
                R.string.recipe_07, R.drawable.recipe_07,
                "https://www.youtube.com/embed/Sjx3J6du-gI"
            )
        )
        recipeList[6].addIngredient(R.string.ingredient_07_01)
        recipeList[6].addIngredient(R.string.ingredient_07_02)
        recipeList[6].addIngredient(R.string.ingredient_07_03)
        recipeList[6].addStep(R.string.step_07_01)
        recipeList[6].addStep(R.string.step_07_02)
        recipeList[6].addStep(R.string.step_07_03)
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
}
