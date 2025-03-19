package com.fitwizard.fitwizard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;


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

        // Back button navigation
        findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Search button functionality
        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFoods(searchEditText.getText().toString());
            }
        });


    }

    //remove later
    //this is example of presentation of food information to user
    private List<FoodData> getSampleFoodList() {
        List<FoodData> sampleFoods = new ArrayList<>();
        sampleFoods.add(new FoodData("Hard Boiled Egg", 78, 6, 5, 1, "1 large egg"));
        sampleFoods.add(new FoodData("Green Apple", 95, 0, 0, 25, "1 medium apple"));
        sampleFoods.add(new FoodData("Chicken Breast", 165, 31, 4, 0, "100g"));
        sampleFoods.add(new FoodData("Apple Juice", 114, 0, 0, 28, "1 cup (240ml)"));
        return sampleFoods;
    }



    //user searches food name
    //presses 'Search' to be shown list
    private void searchFoods(String query) {
        List<FoodData> filteredList = new ArrayList<>();    // Change to get information from API, set a limit to only grab about 10 items
        for (FoodData food : foodList) {
            if (food.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(food);
            }
        }
        foodListView.setAdapter(new FoodAdapter(this, filteredList));
    }

    private void saveFoodToMeal(Object food, String mealType) {

    }
}