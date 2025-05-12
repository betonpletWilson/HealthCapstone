package com.fitwizard.fitwizard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import Goals.GoalsActivity;
import Medication.MedicationActivity;
import Exercise_Logging.ExerciseHistoryActivity;
import Mood.MoodLogsActivity;
import Recipe_Logging.FoodLogActivity;
import Reminders.NotificationsActivity;
import Mood.MoodActivity;

public class HomeActivity extends AppCompatActivity {

    private WaterLevelView waterLevelView;
    private TextView waterAmount, waterTime, dateText, usernameText;
    private float currentWater = 1.9f;
    final float waterGoal = 2.5f;

    private ProgressBar proteinsProgress, fatsProgress, carbsProgress, caloriesProgress;
    private FloatingActionButton addWaterBtn, subtractWaterBtn, addFab;
    private LinearLayout addMenu;

    private final String channelId = "i.apps.notifications";

    private Button addMealButton, logMoodButton, newNotifButton, medicationsButton, logExerciseButton;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d("NotificationPermission", "Granted");
            } else {
                // Permission denied
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        /*
        // Ask for notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }

         */



        if (getSupportActionBar() != null) getSupportActionBar().hide();


        initializeViews();
        setupProfileImage();
        setupNutrientIndicators();
        setupWaterControls();
        setupPopupNavMenu();
        loadUserData();
    }

    private void initializeViews() {
        // UI components
        waterAmount = findViewById(R.id.water_amount);
        waterTime = findViewById(R.id.water_time);
        waterLevelView = findViewById(R.id.water_level);
        usernameText = findViewById(R.id.username_text);

        // Progress Bars
        proteinsProgress = findViewById(R.id.proteins_progress);
        fatsProgress = findViewById(R.id.fats_progress);
        carbsProgress = findViewById(R.id.carbs_progress);
        caloriesProgress = findViewById(R.id.calories_progress);

        // Water buttons
        addWaterBtn = findViewById(R.id.water_add);
        subtractWaterBtn = findViewById(R.id.water_subtract);



        // Popup menu components
        addFab = findViewById(R.id.fab_add);
        addMenu = findViewById(R.id.add_menu);
        logExerciseButton = findViewById(R.id.btn_log_exercise);
        addMealButton = findViewById(R.id.btn_add_meal);
        logMoodButton = findViewById(R.id.btn_log_mood);
        newNotifButton = findViewById(R.id.btn_new_notif);
        medicationsButton = findViewById(R.id.btn_medications);

        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Click listener for setting custom water amount
        waterAmount.setOnClickListener(v -> showWaterInputDialog());

        CardView moodCard = findViewById(R.id.mood_card);
        moodCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MoodLogsActivity.class);
            startActivity(intent);
        });



        Button goalsBtn = findViewById(R.id.btn_goals);
        goalsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, GoalsActivity.class);
            intent.putExtra("reset_goal", true); // optional if you want it force-reset every time
            startActivity(intent);

        });

    }

    private void setupProfileImage() {
        ImageView profileImage = findViewById(R.id.profile_image);
        profileImage.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
    }

    private void setupNutrientIndicators() {
        proteinsProgress.setProgress(67);  // TODO: Replace with dynamic data
        fatsProgress.setProgress(25);
        carbsProgress.setProgress(94);
        caloriesProgress.setProgress(72);
    }

    private void setupWaterControls() {
        addWaterBtn.setOnClickListener(v -> modifyWaterAmount(0.1f));
        subtractWaterBtn.setOnClickListener(v -> modifyWaterAmount(-0.1f));

        // Initial display update
        updateWaterDisplay();
    }

    private void modifyWaterAmount(float amount) {
        currentWater = Math.max(0, currentWater + amount);
        updateWaterDisplay();
        updateWaterTime();
    }

    private void updateWaterDisplay() {
        waterAmount.setText(String.format(Locale.getDefault(), "%.1f / %.1fL", currentWater, waterGoal));
        waterLevelView.setWaterLevel(Math.min(currentWater / waterGoal, 1.0f));
    }

    private void updateWaterTime() {
        String formattedTime = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
        waterTime.setText(getString(R.string.last_time_text, formattedTime));
    }

    private void showWaterInputDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter water amount (L)");
        input.setText(String.valueOf(currentWater));

        new AlertDialog.Builder(this)
                .setTitle("Set Water Amount")
                .setView(input)
                .setPositiveButton("Set", (dialog, which) -> {
                    try {
                        float newAmount = Float.parseFloat(input.getText().toString());
                        if (newAmount >= 0) {
                            currentWater = newAmount;
                            updateWaterDisplay();
                            updateWaterTime();
                        } else {
                            showToast("Please enter a positive value");
                        }
                    } catch (NumberFormatException e) {
                        showToast("Please enter a valid number");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }



    private void setupPopupNavMenu() {
        // Move the popup menu outside the bottom nav card
        addFab.setOnClickListener(v -> togglePopupMenu());

        addMealButton.setOnClickListener(v -> openActivity(FoodLogActivity.class));

        // Modified mood button logic
        logMoodButton.setOnClickListener(v -> {
            checkAndNavigateToMoodActivity();
        });

        newNotifButton.setOnClickListener(v -> openActivity(NotificationsActivity.class));
        medicationsButton.setOnClickListener(v -> openActivity(MedicationActivity.class));
        logExerciseButton.setOnClickListener(v -> openActivity(ExerciseHistoryActivity.class));
    }

    private void checkAndNavigateToMoodActivity() {
        // Format today's date key
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayKey = keyFormat.format(new Date());

        // Load from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MoodPrefs", MODE_PRIVATE);
        String savedEntry = prefs.getString(todayKey, null);

        if (savedEntry != null) {
            // Entry exists for today, go to MoodEntryDetailActivity
            try {
                // Parse the saved entry to get the full date string
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault());
                Date currentDate = new Date(); // Use the current date
                String fullDateString = dateFormat.format(currentDate);

                // Create intent for detail activity
                Intent intent = new Intent(HomeActivity.this, MoodLogsActivity.class);
                intent.putExtra("ENTRY_DATE", fullDateString);
                startActivity(intent);
            } catch (Exception e) {
                // If any error occurs, default to the MoodActivity
                Intent intent = new Intent(HomeActivity.this, MoodActivity.class);
                startActivity(intent);
            }
        } else {
            // No entry exists for today, go to the mood selection activity
            Intent intent = new Intent(HomeActivity.this, MoodActivity.class);
            startActivity(intent);
        }
    }



    private void togglePopupMenu() {
        if (addMenu.getVisibility() == View.GONE) {
            addMenu.setVisibility(View.VISIBLE);
            addMenu.setAlpha(0f);
            addMenu.setTranslationY(100f);
            addMenu.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(200)
                    .start();
        } else {
            addMenu.animate()
                    .alpha(0f)
                    .translationY(100f)
                    .setDuration(200)
                    .withEndAction(() -> addMenu.setVisibility(View.GONE))
                    .start();
        }
    }

    private void openActivity(Class<?> activityClass) {
        startActivity(new Intent(HomeActivity.this, activityClass));
        addMenu.setVisibility(View.GONE);
    }



    private void showToast(String message) {
        Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    // 👇 Load user's name from shared preferences
    private void loadUserData() {
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userName = preferences.getString("user_name", "GetName");
        usernameText.setText(userName);
    }
}
