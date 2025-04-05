package com.mgsanlet.cheftube.data.repository;

import android.content.Context;

import com.mgsanlet.cheftube.R;
import com.mgsanlet.cheftube.data.model.Recipe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class that manages a collection of {@link Recipe} objects.
 * Provides methods to initialize, register, and filter recipes.
 * @author MarioG
 */
public class RecipeRepository implements Serializable {

    // -Singleton instance of the recipe list-
    private static List<Recipe> instance = null;

    /**
     * Retrieves the singleton instance of the recipe list.
     * Initializes the list and loads default data if not already created.
     *
     * @return the singleton list of recipes
     */
    public static List<Recipe> getInstance() {
        if (instance == null) {
            instance = new ArrayList<>();
            loadDefaultData();
        }
        return instance;
    }

    /**
     * Registers a new recipe to the singleton instance list.
     *
     * @param recipe the recipe to be added
     */
    public static void register(Recipe recipe) {
        instance.add(recipe);
    }

    /**
     * Filters recipes based on a search query.
     * The search checks ingredient names and is case-insensitive.
     *
     * @param context the Android context, used to resolve string resources
     * @param query   the search query
     * @return a list of recipes that match the query
     */
    public static List<Recipe> getFilteredRecipes(Context context, String query) {
        List<Recipe> filteredRecipes = new ArrayList<>();

        // -Checking for matching recipes-
        for (Recipe recipe :getInstance()) {
            if (recipe.matchesIngredientQuery(context, query)) {
                filteredRecipes.add(recipe);
            }
        }
        return filteredRecipes;
    }

    /**
     * Loads default recipe data into the instance list.
     * This method is used to populate the model with predefined recipes.
     */
    public static void loadDefaultData() {
        register(new Recipe(R.string.recipe_01, R.drawable.recipe_01,
                "https://www.youtube.com/embed/Lor3PbTSAho"
        ));
        instance.get(0).addIngredient(R.string.ingredient_01_01);
        instance.get(0).addIngredient(R.string.ingredient_01_02);
        instance.get(0).addIngredient(R.string.ingredient_01_03);
        instance.get(0).addIngredient(R.string.ingredient_01_04);
        instance.get(0).addStep(R.string.step_01_01);
        instance.get(0).addStep(R.string.step_01_02);
        instance.get(0).addStep(R.string.step_01_03);
        register(new Recipe(R.string.recipe_02, R.drawable.recipe_02,
                "https://www.youtube.com/embed/NBjsqZZ2Tc4"
        ));
        instance.get(1).addIngredient(R.string.ingredient_02_01);
        instance.get(1).addIngredient(R.string.ingredient_02_02);
        instance.get(1).addIngredient(R.string.ingredient_02_03);
        instance.get(1).addIngredient(R.string.ingredient_02_04);
        instance.get(1).addStep(R.string.step_02_01);
        instance.get(1).addStep(R.string.step_02_02);
        instance.get(1).addStep(R.string.step_02_03);
        register(new Recipe(R.string.recipe_03, R.drawable.recipe_03,
                "https://www.youtube.com/embed/E0n2ZhOw-MI"
        ));
        instance.get(2).addIngredient(R.string.ingredient_03_01);
        instance.get(2).addIngredient(R.string.ingredient_03_02);
        instance.get(2).addIngredient(R.string.ingredient_03_03);
        instance.get(2).addStep(R.string.step_03_01);
        instance.get(2).addStep(R.string.step_03_02);
        instance.get(2).addStep(R.string.step_03_03);
        register(new Recipe(R.string.recipe_04, R.drawable.recipe_04,
                "https://www.youtube.com/embed/VDX-YXCARpM"
        ));
        instance.get(3).addIngredient(R.string.ingredient_04_01);
        instance.get(3).addIngredient(R.string.ingredient_04_02);
        instance.get(3).addIngredient(R.string.ingredient_04_03);
        instance.get(3).addStep(R.string.step_04_01);
        instance.get(3).addStep(R.string.step_04_02);
        instance.get(3).addStep(R.string.step_04_03);
        register(new Recipe(R.string.recipe_05, R.drawable.recipe_05,
                "https://www.youtube.com/embed/OG4pJdxxmj4"
        ));
        instance.get(4).addIngredient(R.string.ingredient_05_01);
        instance.get(4).addIngredient(R.string.ingredient_05_02);
        instance.get(4).addIngredient(R.string.ingredient_05_03);
        instance.get(4).addStep(R.string.step_05_01);
        instance.get(4).addStep(R.string.step_05_02);
        instance.get(4).addStep(R.string.step_05_03);
        register(new Recipe(R.string.recipe_06, R.drawable.recipe_06,
                "https://www.youtube.com/embed/168HrdzakaA"
        ));
        instance.get(5).addIngredient(R.string.ingredient_06_01);
        instance.get(5).addIngredient(R.string.ingredient_06_02);
        instance.get(5).addIngredient(R.string.ingredient_06_03);
        instance.get(5).addStep(R.string.step_06_01);
        instance.get(5).addStep(R.string.step_06_02);
        instance.get(5).addStep(R.string.step_06_03);
        register(new Recipe(R.string.recipe_07, R.drawable.recipe_07,
                "https://www.youtube.com/embed/Sjx3J6du-gI"
        ));
        instance.get(6).addIngredient(R.string.ingredient_07_01);
        instance.get(6).addIngredient(R.string.ingredient_07_02);
        instance.get(6).addIngredient(R.string.ingredient_07_03);
        instance.get(6).addStep(R.string.step_07_01);
        instance.get(6).addStep(R.string.step_07_02);
        instance.get(6).addStep(R.string.step_07_03);
    }

}
