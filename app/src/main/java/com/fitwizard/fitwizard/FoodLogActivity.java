package com.fitwizard.fitwizard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FoodLogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_log);

        //removal of top bar with "fit_wizard" name on top of screen
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize UI elements
        setupMealSection("Breakfast");
        setupMealSection("Lunch");
        setupMealSection("Dinner");

        // Back button navigation
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // upper left button < , return to home page
            Intent intent = new Intent(FoodLogActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // closes the current activity (Meal logging)
        });
    }

    private void setupMealSection(final String mealType) {
        // Find the appropriate button based on meal type
        ImageButton addButton = null;

        switch (mealType.toLowerCase()) {
            case "breakfast":
                addButton = findViewById(R.id.breakfastAddButton);
                break;
            case "lunch":
                addButton = findViewById(R.id.lunchAddButton);
                break;
            case "dinner":
                addButton = findViewById(R.id.dinnerAddButton);
                break;
        }

        // Set click listener for the add button
        if (addButton != null) {
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Launch FoodSearchActivity with meal type information
                    Intent intent = new Intent(FoodLogActivity.this, FoodSearchActivity.class);
                    intent.putExtra("MEAL_TYPE", mealType.toLowerCase());
                    startActivityForResult(intent, 1);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // Handle the returned data from FoodSearchActivity
                // Refresh the meal sections to display newly added items

            }
        }
    }
}












