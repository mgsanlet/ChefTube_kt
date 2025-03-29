package com.mgsanlet.cheftube.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mgsanlet.cheftube.FragmentNavigator;
import com.mgsanlet.cheftube.R;
import com.mgsanlet.cheftube.auth.AuthActivity;

import java.util.Locale;

import model.User;
/**
 * MainActivity serves as the primary activity for the application's home screen.
 * It manages the top MaterialToolbar, BottomNavigationView, and fragments
 * for various sections of the app.
 * @author MarioG
 */
public class MainActivity extends AppCompatActivity {
    // -Declaring UI elements-
    MaterialToolbar topToolbar;
    BottomNavigationView bottomNavView;
    // -Declaring data members-
    User mloggedUser;
    // -Declaring intent-
    Intent authActIntent;
    // -Declaring string resources-
    String noEmailAppStr;
    // -Declaring shared preferences data-
    private static final String PREFS_NAME = "AppPrefs";
    private static final String LANGUAGE_KEY = "language";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // -Initializing UI elements-
        topToolbar = findViewById(R.id.materialToolbar);
        bottomNavView = findViewById(R.id.bottomNavigationView);

        // -Initializing string resources-
        noEmailAppStr = getString(R.string.no_email_app);

        // -Saving user data arriving from authentication activity-
        mloggedUser = (User) getIntent().getSerializableExtra("user");

        // -Setting up material toolbar-
        setSupportActionBar(topToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
        // -Setting up bottom navigation view-
        setUpBottomNav();
    }

    private void saveLanguage(String languageCode) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LANGUAGE_KEY, languageCode);
        editor.apply();
    }

    /* ************************************************************
     *                  TOP MATERIAL BAR MENU                     *
     ************************************************************ */
    // Top menu managing
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_contact_us) {
            onContactUs();
        }
        if (id == R.id.action_language) {
            onLanguage();
        }
        if (id == R.id.action_logout) {
            onLogout();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Opens an email app to allow the user to contact support.
     * Uses an Intent with `ACTION_SENDTO` and the "mailto" URI scheme.
     * If no email app is available, shows a Toast message.
     */
    private void onContactUs() {
        String emailAddress = "support@cheftube.com";

        // -Creating an Intent to send an email-
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", emailAddress, null));
        /* -Checking if there is an app in package manager (system)
            to handle the action SENDTO/mailto- */
        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(emailIntent);
        } else {
            Toast.makeText(getApplicationContext(), noEmailAppStr, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays a dialog for language selection and updates the app's locale.
     */
    private void onLanguage() {
        String[] languages = getResources().getStringArray(R.array.languages);
        // -Language codes ISO 639-1 format-
        final String[] languageCodes = {"en", "es", "it"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getString(R.string.dialog_title));
        builder.setItems(languages, (dialog, which) -> {
            // -This code is executed when a language is selected-
            String selectedLanguage = languageCodes[which]; // -'which' is the item index-
            setLocale(selectedLanguage, true); // -Setting the language of the app-
        });
        builder.show();
    }

    /**
     * Updates the app's locale to the specified language code (ISO 639-1) and optionally restarts
     * the activity to apply changes. Saves the language preference in SharedPreferences.
     *
     * @param languageCode    The language code to set (e.g., "en" for English, "es" for Spanish).
     * @param restartActivity If true, the activity will be restarted to apply the new locale.
     */
    private void setLocale(String languageCode, boolean restartActivity) {
        Resources res = getResources();
        Configuration config = res.getConfiguration();
        config.setLocale(new Locale(languageCode));
        res.updateConfiguration(config, res.getDisplayMetrics());

        saveLanguage(languageCode); // -Saving the chosen language to SharedPreferences-

        if (restartActivity) {
            recreate(); // -Restarting activity only if requested-
        }
    }

    /**
     * Logs out the user and redirects to the authentication activity.
     */
    private void onLogout() {
        authActIntent = new Intent(MainActivity.this, AuthActivity.class);
        startActivity(authActIntent);
    }

    /* **********************************************************
     *                  BOTTOM NAVIGATION VIEW                  *
     ********************************************************** */

    /**
     * Configures the listener for the BottomNavigationView to handle navigation between fragments.
     */
    private void setUpBottomNav() {
        bottomNavView.setSelectedItemId(R.id.home_item); //Default
        // -Removing insets and padding because they cause issues with the  bottom navigation view-
        bottomNavView.setOnApplyWindowInsetsListener(null);
        bottomNavView.setPadding(0,0,0,0);

        // -Setting up the listener-
        bottomNavView.setOnItemSelectedListener(item -> {
           if (item.getItemId() == R.id.profile_item) {
               Fragment profileFragment = ProfileFragment.newInstance(mloggedUser);
               FragmentNavigator.loadFragmentInstance(
                       this, null, profileFragment, R.id.mainFrContainer
               );
               return true;
           }  else if (item.getItemId() == R.id.home_item) {
               FragmentNavigator.loadFragment(
                       this, null, new RecipeListFragment(), R.id.mainFrContainer
               );
               return true;
           }  else if (item.getItemId() == R.id.health_item){
               FragmentNavigator.loadFragment(
                       this, null, new HealthyFragment(), R.id.mainFrContainer
               );
               return true;
           } else {
               return false;
           }
       });
    }
}

