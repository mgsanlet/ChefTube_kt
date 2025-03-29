package com.mgsanlet.cheftube.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mgsanlet.cheftube.R;

import java.util.Locale;

/**
 * SplashActivity is the introductory screen that appears when the application starts.
 * It displays a splash screen with an animated logo and title. The animations are applied
 * to the logo and title images using the `Animation` class, and after a delay, the activity
 * transitions to the next screen, `AuthActivity`.
 * @author MarioG
 */
public class SplashActivity extends AppCompatActivity {
    // -Declaring UI elements-
    ImageView logoImg;
    ImageView titleImg;
    // -Declaring animations-
    Animation animationLogo;
    Animation animationTitle;
    // -Declaring shared preferences data-
    private static final String PREFS_NAME = "AppPrefs";
    private static final String LANGUAGE_KEY = "language";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the saved language and apply the locale
        applySavedLocale();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // -Binding attributes to XML views-
        logoImg = findViewById(R.id.splash_logo);
        titleImg = findViewById(R.id.splash_title);
        // -Loading animations-
        animationLogo = AnimationUtils.loadAnimation(this, R.anim.splash_logo_anim);
        animationTitle = AnimationUtils.loadAnimation(this, R.anim.splash_title_anim);
        // -Starting animations-
        logoImg.startAnimation(animationLogo);
        titleImg.startAnimation(animationTitle);
        // -Delaying the start of the next activity-
        new Handler().postDelayed( () -> {
            startActivity( new Intent(SplashActivity.this, AuthActivity.class) );
            finish();
        }, 2100);
    }

    /**
     * Applies the saved language from SharedPreferences to the current configuration.
     */
    private void applySavedLocale() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String languageCode = preferences.getString(LANGUAGE_KEY, Locale.getDefault().getLanguage());
        Resources res = getResources();
        Configuration config = res.getConfiguration();
        Locale locale = new Locale(languageCode);
        config.setLocale(locale);
        res.updateConfiguration(config, res.getDisplayMetrics());
    }
}