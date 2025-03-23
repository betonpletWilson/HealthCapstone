package com.fitwizard.fitwizard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

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
            if (resultCode == RESULT_OK && data != null) {
                // Get the food data from the intent
                String foodName = data.getStringExtra("FOOD_NAME");
                double calories = data.getDoubleExtra("FOOD_CALORIES", 0);
                double protein = data.getDoubleExtra("FOOD_PROTEIN", 0);
                double fat = data.getDoubleExtra("FOOD_FAT", 0);
                double carbs = data.getDoubleExtra("FOOD_CARBS", 0);
                String serving = data.getStringExtra("FOOD_SERVING");
                String mealType = data.getStringExtra("MEAL_TYPE");

                // Add the food item to the appropriate meal container
                if (mealType != null) {
                    addFoodItemToMeal(foodName, calories, protein, fat, carbs, serving, mealType);
                } else {
                    Toast.makeText(this, "Meal type is missing.", Toast.LENGTH_SHORT).show();
                    // Optionally return or log the issue
                }
            }
        }
    }

    private void addFoodItemToMeal(String foodName, double calories, double protein,
                                   double fat, double carbs, String serving, String mealType) {
        // Find the container for the specified meal type
        LinearLayout container = null;
        switch (mealType.toLowerCase()) {
            case "breakfast":
                container = findViewById(R.id.breakfastContainer);
                break;
            case "lunch":
                container = findViewById(R.id.lunchContainer);
                break;
            case "dinner":
                container = findViewById(R.id.dinnerContainer);
                break;
        }

        if (container != null) {
            // Inflate the food item layout
            LayoutInflater inflater = LayoutInflater.from(this);
            View foodItemView = inflater.inflate(R.layout.food_item_layout, container, false);

            // Set the food data to the views
            TextView foodNameTextView = foodItemView.findViewById(R.id.foodNameTextView);
            TextView servingTextView = foodItemView.findViewById(R.id.servingTextView);
            TextView caloriesTextView = foodItemView.findViewById(R.id.caloriesTextView);
            TextView macrosTextView = foodItemView.findViewById(R.id.macrosTextView);

            foodNameTextView.setText(foodName);
            servingTextView.setText(serving);
            caloriesTextView.setText(String.format(Locale.ENGLISH, "%.0f kcal", calories));
            macrosTextView.setText(String.format(Locale.ENGLISH, "P: %.1fg, F: %.1fg, C: %.1fg", protein, fat, carbs));

            // Add delete functionality
            ImageButton deleteButton = foodItemView.findViewById(R.id.deleteButton);
            LinearLayout finalContainer = container;
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finalContainer.removeView(foodItemView);
                    // TODO: Remove from database if you're storing the data
                }
            });

            // Add the food item view to the container
            container.addView(foodItemView);

            // Remove the placeholder text if it exists
            View placeholderView = container.findViewWithTag("placeholder");
            if (placeholderView != null) {
                container.removeView(placeholderView);
            }
        }
    }





}