package com.fitwizard.fitwizard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class HomeActivity extends AppCompatActivity {

    private WaterLevelView waterLevelView;
    private TextView waterAmount, waterTime, dateText, usernameText;
    private float currentWater = 1.9f;
    final float waterGoal = 2.5f;

    private ProgressBar proteinsProgress, fatsProgress, carbsProgress, caloriesProgress;
    private FloatingActionButton addWaterBtn, subtractWaterBtn, addFab;
    private LinearLayout addMenu;
    private Button addMealButton, logMoodButton;

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
        usernameText = findViewById(R.id.username_text); // 👈 NEW

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
        addMealButton = findViewById(R.id.btn_add_meal);
        logMoodButton = findViewById(R.id.btn_log_mood);

        // Set Date Text
        dateText.setText(new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date()));

        // Click listener for setting custom water amount
        waterAmount.setOnClickListener(v -> showWaterInputDialog());
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
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) addMenu.getLayoutParams();
        layoutParams.bottomToTop = R.id.fab_add;
        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        addMenu.setLayoutParams(layoutParams);

        addFab.setOnClickListener(v -> togglePopupMenu());
        addMealButton.setOnClickListener(v -> openActivity(FoodLogActivity.class));
        logMoodButton.setOnClickListener(v -> openActivity(MoodActivity.class));
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
        if (addMenu.getVisibility() == View.GONE) {
            addMenu.setVisibility(View.VISIBLE);
            addMenu.setAlpha(0f);
            addMenu.animate().alpha(1f).setDuration(200).start();
        } else {
            addMenu.animate().alpha(0f).setDuration(200).withEndAction(() -> addMenu.setVisibility(View.GONE)).start();
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
