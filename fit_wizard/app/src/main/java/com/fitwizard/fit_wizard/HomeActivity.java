package com.fitwizard.fit_wizard;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fitwizard.fit_wizard.WaterLevelView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private ProgressBar proteinsProgress, fatsProgress, carbsProgress, caloriesProgress;
    private WaterLevelView waterLevelView;
    private TextView waterAmount;
    private float currentWater = 1.9f;
    private final float waterGoal = 2.5f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //removal of top bar with "fit_wizard" name on top of screen
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize views
        proteinsProgress = findViewById(R.id.proteins_progress);
        fatsProgress = findViewById(R.id.fats_progress);
        carbsProgress = findViewById(R.id.carbs_progress);
        caloriesProgress = findViewById(R.id.calories_progress);
        waterLevelView = findViewById(R.id.water_level);
        waterAmount = findViewById(R.id.water_amount);

        // Set progress values
        proteinsProgress.setProgress(67); // 150/225 ≈ 67%
        fatsProgress.setProgress(25);     // 30/118 ≈ 25%
        carbsProgress.setProgress(94);    // 319/340 ≈ 94%
        caloriesProgress.setProgress(72); // 2456/3400 ≈ 72%

        // Set water level
        updateWaterDisplay();

        // Set date text
        TextView dateText = findViewById(R.id.date_text);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        dateText.setText(sdf.format(new Date()));

        // Set up water control buttons
        FloatingActionButton addWater = findViewById(R.id.water_add);
        FloatingActionButton subtractWater = findViewById(R.id.water_subtract);

        addWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWater(0.1f);
            }
        });

        subtractWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWater(-0.1f);
            }
        });

        // Set up bottom navigation
        View homeNav = findViewById(R.id.nav_home);
        View reportsNav = findViewById(R.id.nav_reports);
        FloatingActionButton addFab = findViewById(R.id.fab_add);

        homeNav.setOnClickListener(v -> {
            // Already on home screen
        });

        reportsNav.setOnClickListener(v -> {
            // Navigate to reports screen
            // Intent intent = new Intent(HomeActivity.this, ReportsActivity.class);
            // startActivity(intent);
        });

        addFab.setOnClickListener(v -> {
            // Open add food/water dialog
            // showAddItemDialog();
        });
    }

    private void addWater(float amount) {
        currentWater += amount;
        // Ensure water doesn't go below 0
        currentWater = Math.max(0, currentWater);
        updateWaterDisplay();

        // Update last time text
        TextView waterTime = findViewById(R.id.water_time);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        waterTime.setText("Last time " + sdf.format(new Date()));
    }

    private void updateWaterDisplay() {
        // Update water amount text
        waterAmount.setText(String.format(Locale.getDefault(), "%.1f / %.1fL", currentWater, waterGoal));

        // Update water level view
        float percentage = Math.min(currentWater / waterGoal, 1.0f);
        waterLevelView.setWaterLevel(percentage);
    }
}