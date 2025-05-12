package com.fitwizard.fitwizard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import Exercise_Logging.ExerciseHistoryActivity;
import Goals.GoalsActivity;
import Medication.MedicationActivity;
import Mood.MoodActivity;
import Recipe_Logging.FoodLogActivity;
import Reminders.NotificationsActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView proteinsValue, fatsValue, carbsValue, caloriesValue;
    private WaterLevelView waterLevelView;
    private TextView waterAmount, waterTime, dateText, usernameText;
    private float currentWater = 1.9f;
    final float waterGoal = 2.5f;

    private ProgressBar proteinsProgress, fatsProgress, carbsProgress, caloriesProgress;
    private FloatingActionButton addWaterBtn, subtractWaterBtn, addFab;
    private LinearLayout addMenu;
    private Button addMealButton, logMoodButton;

    private static final String PREFS_NAME = "health_data";
    private static final String KEY_WATER = "water_";
    private static final String KEY_TIME = "water_time_";
    private static final String KEY_PROTEINS = "proteins_";
    private static final String KEY_FATS = "fats_";
    private static final String KEY_CARBS = "carbs_";
    private static final String KEY_CALORIES = "calories_";
    private static final String KEY_LAST_UPDATE_DATE = "last_update_date";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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
        dateText = findViewById(R.id.date_text);
        usernameText = findViewById(R.id.username_text);

        // Progress Bars
        proteinsProgress = findViewById(R.id.proteins_progress);
        fatsProgress = findViewById(R.id.fats_progress);
        carbsProgress = findViewById(R.id.carbs_progress);
        caloriesProgress = findViewById(R.id.calories_progress);

        proteinsValue = findViewById(R.id.proteins_value);
        fatsValue = findViewById(R.id.fats_value);
        carbsValue = findViewById(R.id.carbs_value);
        caloriesValue = findViewById(R.id.calories_value);


        // Water buttons
        addWaterBtn = findViewById(R.id.water_add);
        subtractWaterBtn = findViewById(R.id.water_subtract);

        // Popup menu
        addFab = findViewById(R.id.fab_add);
        addMenu = findViewById(R.id.add_menu);
        addMealButton = findViewById(R.id.btn_add_meal);
        logMoodButton = findViewById(R.id.btn_log_mood);

        // ✅ Set current date
        dateText.setText(new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date()));

        // Water input dialog
        waterAmount.setOnClickListener(v -> showWaterInputDialog());

        // Goals button
        Button goalsBtn = findViewById(R.id.btn_goals);
        goalsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, GoalsActivity.class);
            intent.putExtra("reset_goal", true);
            startActivity(intent);
        });

        // Settings button
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }


    private String getTodayKey(String baseKey) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return baseKey + today;
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
        updateWaterDisplay();
    }

    private void setupPopupNavMenu() {
        // Attach the popup menu just above the FAB
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) addMenu.getLayoutParams();
        layoutParams.bottomToTop = R.id.fab_add;
        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        addMenu.setLayoutParams(layoutParams);

        addFab.setOnClickListener(v -> togglePopupMenu());

        // Button references
        Button addMealButton = findViewById(R.id.btn_add_meal);
        Button logMoodButton = findViewById(R.id.btn_log_mood);
        Button goalsButton = findViewById(R.id.btn_goals);
        Button medicationsButton = findViewById(R.id.btn_medications);
        Button logExerciseButton = findViewById(R.id.btn_log_exercise);
        Button newNotifButton = findViewById(R.id.btn_new_notif);

        // Set listeners
        addMealButton.setOnClickListener(v -> openActivity(FoodLogActivity.class));
        logMoodButton.setOnClickListener(v -> openActivity(MoodActivity.class));
        goalsButton.setOnClickListener(v -> openActivity(GoalsActivity.class)); // Replace with your actual class
        medicationsButton.setOnClickListener(v -> openActivity(MedicationActivity.class));
        logExerciseButton.setOnClickListener(v -> openActivity(ExerciseHistoryActivity.class)); // Or ExerciseLogActivity.class
        newNotifButton.setOnClickListener(v -> openActivity(Reminders.NotificationsActivity.class));
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

    private void togglePopupMenu() {
        View profileCard = findViewById(R.id.profile_card);

        if (addMenu.getVisibility() == View.GONE) {
            addMenu.setVisibility(View.VISIBLE);
            addMenu.setAlpha(0f);
            addMenu.animate().alpha(1f).setDuration(200).start();

            // Hide profile while menu is up
            profileCard.setVisibility(View.GONE);
        } else {
            addMenu.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                addMenu.setVisibility(View.GONE);
                profileCard.setVisibility(View.VISIBLE); // Restore profile when menu closes
            }).start();
        }
    }


    private void openActivity(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
        addMenu.setVisibility(View.GONE); // Optional: hide menu after opening
    }


    private void showToast(String message) {
        Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    // 👇 Load user's name from shared preferences
    private void loadUserData() {
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userName = preferences.getString("user_name", "GetName");
        usernameText.setText(userName);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastUpdate = prefs.getString(KEY_LAST_UPDATE_DATE, "");

        // ✅ Reset data at midnight before loading values
        if (!today.equals(lastUpdate)) {
            editor.putString(KEY_LAST_UPDATE_DATE, today);
            editor.putFloat(getTodayKey(KEY_WATER), 0f);
            editor.putString(getTodayKey(KEY_TIME), "");
            editor.putInt(getTodayKey(KEY_PROTEINS), 0);
            editor.putInt(getTodayKey(KEY_FATS), 0);
            editor.putInt(getTodayKey(KEY_CARBS), 0);
            editor.putInt(getTodayKey(KEY_CALORIES), 0);
            editor.apply();
        }

        // ✅ Load water
        currentWater = prefs.getFloat(getTodayKey(KEY_WATER), 0f);
        waterAmount.setText(String.format(Locale.getDefault(), "%.1f / %.1fL", currentWater, waterGoal));
        waterLevelView.setWaterLevel(Math.min(currentWater / waterGoal, 1.0f));

        String timeText = prefs.getString(getTodayKey(KEY_TIME), "");
        if (!timeText.isEmpty()) {
            waterTime.setText(timeText);
        } else {
            waterTime.setText(""); // Clear text if no time saved
        }

        // ✅ Load nutrient indicators (start at 0 if not set)
        proteinsProgress.setProgress(prefs.getInt(getTodayKey(KEY_PROTEINS), 0));
        fatsProgress.setProgress(prefs.getInt(getTodayKey(KEY_FATS), 0));
        carbsProgress.setProgress(prefs.getInt(getTodayKey(KEY_CARBS), 0));
        caloriesProgress.setProgress(prefs.getInt(getTodayKey(KEY_CALORIES), 0));

        int protein = prefs.getInt(getTodayKey(KEY_PROTEINS), 0);
        int fat = prefs.getInt(getTodayKey(KEY_FATS), 0);
        int carbs = prefs.getInt(getTodayKey(KEY_CARBS), 0);
        int calories = prefs.getInt(getTodayKey(KEY_CALORIES), 0);

        proteinsProgress.setProgress(protein);
        fatsProgress.setProgress(fat);
        carbsProgress.setProgress(carbs);
        caloriesProgress.setProgress(calories);

// Update the text values (Assume goals: 225g protein, 118g fat, 340g carbs, 3400 cal)
        proteinsValue.setText(protein + " / 225");
        fatsValue.setText(fat + " / 118");
        carbsValue.setText(carbs + " / 340");
        caloriesValue.setText(calories + " / 3400");

    }



    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putFloat(getTodayKey(KEY_WATER), currentWater);
        editor.putString(getTodayKey(KEY_TIME), waterTime.getText().toString());

        editor.putInt(getTodayKey(KEY_PROTEINS), proteinsProgress.getProgress());
        editor.putInt(getTodayKey(KEY_FATS), fatsProgress.getProgress());
        editor.putInt(getTodayKey(KEY_CARBS), carbsProgress.getProgress());
        editor.putInt(getTodayKey(KEY_CALORIES), caloriesProgress.getProgress());

        editor.apply();
    }

}
