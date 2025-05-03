package com.fitwizard.fitwizard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class FoodSearchActivity extends AppCompatActivity {

    private String mealType;
    private EditText searchEditText;
    private ListView foodListView;

    private List<FoodData> foodList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_search);

        //removal of top bar with "fit_wizard" name on top of screen
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Get meal type from intent
        mealType = getIntent().getStringExtra("MEAL_TYPE");

        // Initialize UI components
        searchEditText = findViewById(R.id.searchEditText);
        foodListView = findViewById(R.id.foodListView);
        TextView titleTextView = findViewById(R.id.titleTextView);

        // Set the title based on meal type
        titleTextView.setText("Add " + mealType.substring(0, 1).toUpperCase() + mealType.substring(1));

        //Deal with Food Data from FoodData and FoodAdapter
        foodList = getSampleFoodList(); // Load mock backend data
        foodListView.setAdapter(new FoodAdapter(this, foodList));

        // Set up item click listener for the food list
        foodListView.setOnItemClickListener((parent, view, position, id) -> {
            FoodData selectedFood = (FoodData) parent.getItemAtPosition(position);
            showServingsDialog(selectedFood);
        });

        // Back button navigation
        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());

        // Search button functionality
        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> searchFoods(searchEditText.getText().toString()));
    }

    // Show serving size dialog when a food is selected
    private void showServingsDialog(final FoodData food) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_food_search, null);
        builder.setView(dialogView);

        // Set up dialog views
        TextView foodNameTextView = dialogView.findViewById(R.id.foodNameTextView);
        TextView mealTypeTextView = dialogView.findViewById(R.id.mealTypeTextView);
        final EditText servingsEditText = dialogView.findViewById(R.id.servingsEditText);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button addButton = dialogView.findViewById(R.id.addButton);

        // Set food name and meal type
        foodNameTextView.setText(food.getName());
        mealTypeTextView.setText("Adding to: " + formatMealType(mealType));

        // Create and show the dialog
        final AlertDialog dialog = builder.create();
        dialog.show();

        // Set up button click listeners
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the number of servings
                String servingsText = servingsEditText.getText().toString();
                if (servingsText.isEmpty()) {
                    Toast.makeText(FoodSearchActivity.this, "Please enter the number of servings", Toast.LENGTH_SHORT).show();
                    return;
                }

                double servings;
                try {
                    servings = Double.parseDouble(servingsText);
                } catch (NumberFormatException e) {
                    Toast.makeText(FoodSearchActivity.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save the food with the specified servings
                saveFoodToMeal(food, mealType, servings);
                dialog.dismiss();

                // Optional: Show confirmation and return to previous screen
                Toast.makeText(FoodSearchActivity.this, food.getName() + " added to " + formatMealType(mealType), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // Helper method to format meal type for display
    private String formatMealType(String mealType) {
        if (mealType == null || mealType.isEmpty()) {
            return "Unknown";
        }
        return mealType.substring(0, 1).toUpperCase() + mealType.substring(1);
    }

    // Sample food list for testing
    private List<FoodData> getSampleFoodList() {
        List<FoodData> sampleFoods = new ArrayList<>();
        sampleFoods.add(new FoodData("Hard Boiled Egg", 78, 6, 5, 1, "1 large egg"));
        sampleFoods.add(new FoodData("Green Apple", 95, 0, 0, 25, "1 medium apple"));
        sampleFoods.add(new FoodData("Chicken Breast", 165, 31, 4, 0, "100g"));
        sampleFoods.add(new FoodData("Apple Juice", 114, 0, 0, 28, "1 cup (240ml)"));
        return sampleFoods;
    }

    // Search functionality
    private void searchFoods(String query) {
        List<FoodData> filteredList = new ArrayList<>();    // Change to get information from API, set a limit to only grab about 10 items
        for (FoodData food : foodList) {
            if (food.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(food);
            }
        }
        foodListView.setAdapter(new FoodAdapter(this, filteredList));
    }



    // Save the food item to the meal with specified servings
    private void saveFoodToMeal(FoodData food, String mealType, double servings) {

        // Calculate total nutrition based on servings
        double totalCalories = food.getCalories() * servings;
        double totalProtein = food.getProtein() * servings;
        double totalFat = food.getFats() * servings;
        double totalCarbs = food.getCarbs() * servings;

        // Create an intent to return the data to FoodLogActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("FOOD_NAME", food.getName());
        resultIntent.putExtra("FOOD_CALORIES", totalCalories);
        resultIntent.putExtra("FOOD_PROTEIN", totalProtein);
        resultIntent.putExtra("FOOD_FAT", totalFat);
        resultIntent.putExtra("FOOD_CARBS", totalCarbs);
        resultIntent.putExtra("FOOD_SERVING", food.getServingSize() + " x " + servings);
        resultIntent.putExtra("MEAL_TYPE", mealType);

        // Set the result and finish the activity
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        }
}