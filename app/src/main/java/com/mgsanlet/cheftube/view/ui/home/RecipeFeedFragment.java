package com.mgsanlet.cheftube.view.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mgsanlet.cheftube.R;
import com.mgsanlet.cheftube.view.adapter.RecipeFeedAdapter;

import java.util.List;

import com.mgsanlet.cheftube.data.model.Recipe;
import com.mgsanlet.cheftube.data.repository.RecipeRepository;

/**
 * A fragment that displays a list of recipes. Each recipe is shown with its title and an image.
 * When a recipe is clicked, the fragment navigates to a detailed view of the selected recipe,
 * displaying additional information such as ingredients, preparation steps, and video content.
 * If there are no recipes to display, a message indicating that no results were found is shown.
 *
 * @author MarioG
 */
public class RecipeFeedFragment extends Fragment {
    // -Declaring data members-
    private List<Recipe> recipeList;
    // -Declaring UI elements-
    private RecyclerView recipesRecycler;
    private TextView noResultsTextView;
    private ImageButton searchButton;
    // -Declaring string resources-
    private String resultsStr;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);
        // -Initializing UI elements-
        recipesRecycler = view.findViewById(R.id.recipeFeedRecyclerView);
        noResultsTextView = view.findViewById(R.id.noResultsTextView);
        recipesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        searchButton = view.findViewById(R.id.search_button);
        // -Initializing string resources-
        resultsStr = getString(R.string.results);

        // -Setting up the search button listener-
        searchButton.setOnClickListener(v -> setUpSearchBtn());

        // -Checking if recipeList is null and retrieving default recipes from RecipeRepository-
        verifyRecipeList();

        RecipeFeedAdapter adapter = new RecipeFeedAdapter(recipeList, getParentFragmentManager());
        recipesRecycler.setAdapter(adapter);
        noResultsTextView.setVisibility(View.GONE); // -Hiding no results message-
        recipesRecycler.setVisibility(View.VISIBLE); // -Showing RecyclerView-

        return view;
    }

    /**
     * Verifies the availability of the recipe list, falling back to RecipeRepository if necessary.
     */
    private void verifyRecipeList() {
        if (this.recipeList == null) {
            this.recipeList = RecipeRepository.getInstance();
        }
    }

    /**
     * Displays a message indicating no results were found.
     */
    private void displayNoResults() {
        noResultsTextView.setVisibility(View.VISIBLE); // -Showing the no results message-
        recipesRecycler.setVisibility(View.GONE); // -Hiding the RecyclerView-
    }

    private void setUpSearchBtn() {
        AlertDialog.Builder searchDialogBuilder = new AlertDialog.Builder(getContext());

        // -Inflating a custom layout for the search dialog-
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_search, null);
        searchDialogBuilder.setView(dialogView);

        // -Getting a reference to the UI elements in the custom layout-
        EditText input = dialogView.findViewById(R.id.editTextSearch);
        Button okBtn = dialogView.findViewById(R.id.okBtn);

        AlertDialog searchDialog = searchDialogBuilder.create();
        searchDialog.show();

        okBtn.setOnClickListener(v -> {
            String query = input.getText().toString().trim();
            // -Filtering the recipes based on the input query-
            List<Recipe> filteredRecipes = RecipeRepository.getFilteredRecipes(getContext(), query);

            Toast.makeText(getContext(), resultsStr + filteredRecipes.size(),
                    Toast.LENGTH_SHORT
            ).show();
            if (filteredRecipes.isEmpty()) {
                displayNoResults(); // Show no results message
            } else {

                RecipeFeedAdapter adapter = new RecipeFeedAdapter(filteredRecipes, getParentFragmentManager());
                recipesRecycler.setAdapter(adapter);
                noResultsTextView.setVisibility(View.GONE); // Hide no results message
                recipesRecycler.setVisibility(View.VISIBLE); // Show RecyclerView
            }

            searchDialog.dismiss(); // Dismiss the dialog after search
        });
    }
}