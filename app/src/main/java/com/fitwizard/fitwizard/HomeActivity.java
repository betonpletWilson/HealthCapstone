package com.fitwizard.fitwizard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


//      TO-DO/ THINGS THAT NEED TO BE FIXED
// - Structure of the popout nav drawer at the bottom, the "Add Meal" button is in the wrong location
// - Popout nav drawer fix 2: when the user selects the screen outside of the drawer, the drawer should minimize again
// - Add water tracking that logs the users water intake to their background
// - Fix water tracker shape (the blue pill shaped object)
// - Update 'Nutrients Indicator' section to get the real values



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


        ImageView profileImage = findViewById(R.id.profile_image);
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Initialize views
        proteinsProgress = findViewById(R.id.proteins_progress);
        fatsProgress = findViewById(R.id.fats_progress);
        carbsProgress = findViewById(R.id.carbs_progress);
        caloriesProgress = findViewById(R.id.calories_progress);
        waterLevelView = findViewById(R.id.water_level);
        waterAmount = findViewById(R.id.water_amount);

        //Nutrients Indicator
        // Set progress values
        proteinsProgress.setProgress(67); // change to ( goalProtein() - getCurrProtein() ) / 100
        fatsProgress.setProgress(25);
        carbsProgress.setProgress(94);
        caloriesProgress.setProgress(72);

        // Set water level
        updateWaterDisplay();

        // Set date text
        TextView dateText = findViewById(R.id.date_text);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        dateText.setText(sdf.format(new Date()));

        // Set up water control buttons
        FloatingActionButton addWater = findViewById(R.id.water_add);     // Plus button
        FloatingActionButton subtractWater = findViewById(R.id.water_subtract); //Minus button

        // Pressing the plus button
        addWater.setOnClickListener(v -> addWater(0.1f));

        // Pressing the minus button
        subtractWater.setOnClickListener(v -> addWater(-0.1f));


        // Popup nav drawer
        FloatingActionButton addFab = findViewById(R.id.fab_add);
        LinearLayout addMenu = findViewById(R.id.add_menu);
        Button addMealButton = findViewById(R.id.btn_add_meal);
        Button logMoodButton = findViewById(R.id.btn_log_mood);

        // Modify the layout parameters for the add_menu
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) addMenu.getLayoutParams();
        layoutParams.bottomToTop = R.id.fab_add; // Position above the fab button
        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        addMenu.setLayoutParams(layoutParams);

        addFab.setOnClickListener(v -> {
            if (addMenu.getVisibility() == View.GONE) {
                addMenu.setVisibility(View.VISIBLE);
                addMenu.setAlpha(0f);
                addMenu.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start();
            } else {
                addMenu.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> {
                            addMenu.setVisibility(View.GONE);
                        })
                        .start();
            }
        });

        addMealButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FoodLogActivity.class);
            startActivity(intent);
            addMenu.setVisibility(View.GONE); // hide the menu
        });

        logMoodButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MoodActivity.class);
            startActivity(intent);
            addMenu.setVisibility(View.GONE); // hide the menu
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