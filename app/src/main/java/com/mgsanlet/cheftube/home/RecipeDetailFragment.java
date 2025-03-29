package com.mgsanlet.cheftube.home;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.mgsanlet.cheftube.R;

import model.Recipe;

/**
 * A fragment that displays the details of a recipe, including its title, ingredients,
 * preparation steps, and an embedded video (if available). It also includes a countdown timer
 * for cooking or preparation time.
 */
public class RecipeDetailFragment extends Fragment {

    // -Declaring constant for argument key-
    private static final String ARG_RECIPE = "recipe";

    // -Declaring UI elements-
    private TextView timerTextView;
    private Button startPauseButton;

    // -Countdown timer variables-
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 0; // Set this to your desired countdown time in milliseconds
    private boolean timerRunning = false;

    // -MediaPlayer for alarm sound-
    private MediaPlayer mediaPlayer;

    /**
     * Factory method to create a new instance of this fragment with the specified recipe.
     *
     * @param recipe The recipe whose details will be shown.
     * @return A new instance of the RecipeDetailFragment.
     */
    public static RecipeDetailFragment newInstance(Recipe recipe) {
        RecipeDetailFragment fragment = new RecipeDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RECIPE, recipe); // -Passing recipe object as argument-
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_detail, container, false);
        Recipe recipe = null;

        // -Getting the recipe object passed as an argument to the fragment-
        if (getArguments() != null) {
            recipe = (Recipe) getArguments().getSerializable(ARG_RECIPE);
        }

        // -Initializing UI elements-
        TextView title = view.findViewById(R.id.recipeDetailTitle);
        WebView webView = view.findViewById(R.id.recipeDetailVideo);
        LinearLayout ingredientsContainer = view.findViewById(R.id.ingredientsLinearLayout);
        LinearLayout stepsContainer = view.findViewById(R.id.stepsLinearLayout);

        // -Setting title and video-
        if (recipe != null) {
            title.setText(getString(recipe.getTtlRId()));
            webView.getSettings().setJavaScriptEnabled(true);
            String videoUrl = recipe.getVideoUrl();
            webView.loadUrl(videoUrl);

            // -Dynamically adding ingredients to the ingredients container-
            for (Integer ingredientId : recipe.getIngrRIds()) {
                TextView ingredientTextView = new TextView(getContext());
                ingredientTextView.setText(ingredientId);
                if (getContext() != null) {
                    ingredientTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                }
                ingredientTextView.setTextSize(16);
                ingredientsContainer.addView(ingredientTextView);
            }

            // -Dynamically adding steps to the steps container-
            for (Integer stepId : recipe.getStepsRIds()) {
                TextView stepTextView = new TextView(getContext());
                stepTextView.setText(stepId);
                stepTextView.setPadding(0, 4, 0, 2);
                if (getContext() != null) {
                    stepTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                }
                stepTextView.setTextSize(12);
                stepsContainer.addView(stepTextView);
            }
        }

        // -Initializing timer UI elements-
        timerTextView = view.findViewById(R.id.timerTextView);
        startPauseButton = view.findViewById(R.id.startPauseButton);

        // -Setting up button listeners-
        startPauseButton.setOnClickListener(v -> startPauseTimer());
        // -Setting up the timer TextView click listener to show the dialog-
        timerTextView.setOnClickListener(v -> showSetTimerDialog());

        return view;
    }

    /**
     * Toggles the timer state between started and paused. If the timer is running, it will be paused.
     * If the timer is paused or stopped, it will be started.
     */
    private void startPauseTimer() {
        if (timerRunning) {
            pauseTimer();
        } else {
            startTimer();
        }
    }

    /**
     * Starts the countdown timer. Creates a new CountDownTimer instance and initializes it
     * with the remaining time. Updates the timer display every second and handles the timer completion.
     */
    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                startPauseButton.setText(R.string.start);
                playAlarmSound();
            }
        }.start();

        timerRunning = true;
        startPauseButton.setText(R.string.pause);
    }

    /**
     * Pauses the currently running timer. Cancels the countdown and updates the UI
     * to reflect the paused state.
     */
    private void pauseTimer() {
        countDownTimer.cancel();
        timerRunning = false;
        startPauseButton.setText(R.string.start);
    }

    /**
     * Updates the timer display with the current remaining time. Converts milliseconds
     * to minutes and seconds format (MM:SS) and displays it in the timer TextView.
     */
    private void updateTimer() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format("%02d:%02d", minutes, seconds);
        timerTextView.setText(timeLeftFormatted);
    }

    /**
     * Shows a dialog allowing the user to set the timer duration. The dialog contains
     * input fields for minutes and seconds. When the user confirms, the timer is updated
     * with the new duration and the display is refreshed.
     */
    private void showSetTimerDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_timer, null);
        dialogBuilder.setView(dialogView);

        EditText minutesInput = dialogView.findViewById(R.id.minutesInput);
        EditText secondsInput = dialogView.findViewById(R.id.secondsInput);

        dialogBuilder.setPositiveButton(R.string.set, (dialog, which) -> {
            String minutesString = minutesInput.getText().toString();
            String secondsString = secondsInput.getText().toString();

            int minutes = minutesString.isEmpty() ? 0 : Integer.parseInt(minutesString);
            int seconds = secondsString.isEmpty() ? 0 : Integer.parseInt(secondsString);

            // Convert to milliseconds and set the timer
            timeLeftInMillis = (minutes * 60L + seconds) * 1000;
            updateTimer(); // Update the displayed timer
        });

        dialogBuilder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        AlertDialog timerDialog = dialogBuilder.create();
        timerDialog.show();
    }

    /**
     * Plays the alarm sound when the timer completes. Uses MediaPlayer to play the sound
     * resource located in res/raw/alarm_clock.mp3. Automatically releases MediaPlayer resources
     * after the sound finishes playing.
     */
    private void playAlarmSound() {
        // Initialize MediaPlayer to play the alarm sound
        mediaPlayer = MediaPlayer.create(getContext(), R.raw.alarm_clock);
        mediaPlayer.start();

        // Release the MediaPlayer resources after playback is complete
        mediaPlayer.setOnCompletionListener(mp -> {
            mp.release();
            mediaPlayer = null;
        });
    }
}