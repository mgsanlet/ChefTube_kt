package com.mgsanlet.cheftube.data.model;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a recipe, containing details such as title, image, ingredients,
 * preparation steps, and a video URL.
 * This class is designed to be serializable for ease of storage and retrieval.
 * @author MarioG
 */
public class Recipe implements Serializable {
    private final int ttlRId;
    private final int imgRId;
    private final List<Integer> ingrRIds;
    private final List<Integer> stepsRIds;
    private final String videoUrl;

    public Recipe(int title, int imgResId, String videoUrl) {
        this.ttlRId = title;
        this.imgRId = imgResId;
        this.videoUrl = videoUrl;
        this.ingrRIds = new ArrayList<>();
        this.stepsRIds = new ArrayList<>();
    }

    public int getTtlRId() {
        return ttlRId;
    }

    public int getImgRId() {
        return imgRId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public List<Integer> getIngrRIds() {
        return ingrRIds;
    }

    public List<Integer> getStepsRIds() {
        return stepsRIds;
    }

    public void addIngredient(int ingrRId) {
        this.ingrRIds.add(ingrRId);
    }

    public void addStep(int stepRId) {
        this.stepsRIds.add(stepRId);
    }

    /**
     * Determines if the recipe matches a query string.
     * The search checks ingredient names for matches and is case-insensitive.
     *
     * @param context the Android context, used to resolve string resources
     * @param query   the search query
     * @return true if the query matches any ingredient name, false otherwise
     */
    public boolean matchesQuery(Context context, String query) {
        // -Converting the query to lowercase for case-insensitive comparison-
        String lowerCaseQuery = query.toLowerCase();

        // -Checking if any ingredient name matches the query-
        for (Integer ingredientId : ingrRIds) {
            String ingredientName = context.getResources().getString(ingredientId);
            if (ingredientName.toLowerCase().contains(lowerCaseQuery)) {
                return true;
            }
        }
        return false;
    }
}
