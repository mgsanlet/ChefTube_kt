package com.mgsanlet.cheftube.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.mgsanlet.cheftube.utils.FragmentNavigator;
import com.mgsanlet.cheftube.R;
import com.mgsanlet.cheftube.view.ui.home.RecipeDetailFragment;
import com.mgsanlet.cheftube.view.ui.home.RecipeFeedFragment;

import java.util.List;

import com.mgsanlet.cheftube.data.model.Recipe;
/**
 * Adapter for displaying a list of recipes in a RecyclerView.
 * This adapter binds recipe data to the views in the RecyclerView.
 * @author MarioG
 */
public class RecipeFeedAdapter extends RecyclerView.Adapter<RecipeFeedAdapter.RecipeViewHolder> {
    private final List<Recipe> recipeList;
    private final FragmentManager fragmentManager;

    public RecipeFeedAdapter(List<Recipe> recipeList, FragmentManager fragmentManager) {
        this.recipeList = recipeList;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        holder.bind(recipeList.get(position));
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    /**
     * Handles navigation to the detailed view of the selected recipe.
     *
     * @param recipe The recipe whose details are to be displayed.
     */
    private void navToRecipeDetail(Recipe recipe) {
        // -Getting the current visible fragment-
        RecipeFeedFragment currentFragment = (RecipeFeedFragment) fragmentManager
                .findFragmentById(R.id.mainFrContainer);

        if (currentFragment != null && currentFragment.isVisible()) {
            RecipeDetailFragment detailFragment = RecipeDetailFragment.newInstance(recipe);

            FragmentNavigator.loadFragmentInstance(
                    null, currentFragment, detailFragment, R.id.mainFrContainer
            );
        }
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;

        public RecipeViewHolder(@NonNull View recipeView) {
            super(recipeView);
            title = recipeView.findViewById(R.id.recipeTitle);
            image = recipeView.findViewById(R.id.recipeImage);
        }

        public void bind(Recipe recipe) {
            title.setText(recipe.getTtlRId());
            // -Loading recipe images with rounded corners using Glide-
            Glide.with(itemView.getContext())
                    .load(recipe.getImgRId())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(40)))
                    .into(image);

            image.setOnClickListener(v -> navToRecipeDetail(recipe));
        }
    }
}
