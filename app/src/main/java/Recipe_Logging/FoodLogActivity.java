package Recipe_Logging;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitwizard.fitwizard.HomeActivity;
import com.fitwizard.fitwizard.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


//TODO: food_item_layout.xml update, remove hard coded values/ warnings
//TODO: Fix meal logging screen so that the user can scroll when there are multiple food inputs



public class FoodLogActivity extends AppCompatActivity {

    // SharedPreferences constants
    private static final String FOOD_LOG_PREFS = "food_log_preferences";
    private static final String KEY_DATE_FORMAT = "yyyy-MM-dd";

    // Map to store food items by meal type
    private Map<String, List<FoodData>> mealFoodMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_log);

        // Remove top bar with "fit_wizard" name
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize food map for today's meals
        mealFoodMap = new HashMap<>();
        mealFoodMap.put("breakfast", new ArrayList<>());
        mealFoodMap.put("lunch", new ArrayList<>());
        mealFoodMap.put("dinner", new ArrayList<>());

        // Load saved food items for today
        loadFoodItemsForToday();

        // Initialize UI elements
        setupMealSection("Breakfast");
        setupMealSection("Lunch");
        setupMealSection("Dinner");

        // Back button navigation
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // Upper left button < , return to home page
            saveFoodItems(); // Save before leaving

            Intent intent = new Intent(FoodLogActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Closes the current activity (Meal logging)
        });

        // Done button navigation
        Button doneButton = findViewById(R.id.btn_done);
        doneButton.setOnClickListener(v -> {
            Intent intent = new Intent(FoodLogActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Close the current screen, back to home activity
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveFoodItems(); // Save food items when leaving the activity
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

        // Load existing food items for this meal type
        displayFoodItemsForMealType(mealType.toLowerCase());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK && data != null) {
                // Get the food data from the intent
                String foodName = data.getStringExtra("FOOD_NAME");
                int calories = (int) data.getDoubleExtra("FOOD_CALORIES", 0);
                int protein = (int) data.getDoubleExtra("FOOD_PROTEIN", 0);
                int fat = (int) data.getDoubleExtra("FOOD_FAT", 0);
                int carbs = (int) data.getDoubleExtra("FOOD_CARBS", 0);
                String serving = data.getStringExtra("FOOD_SERVING");
                String mealType = data.getStringExtra("MEAL_TYPE");

                // Create and add a new FoodData object
                if (mealType != null) {
                    FoodData newFood = new FoodData(foodName, calories, protein, fat, carbs, serving);
                    addFoodItemToMeal(newFood, mealType);

                    // Add to our data structure
                    mealFoodMap.get(mealType).add(newFood);

                    // Save to preferences
                    saveFoodItems();

                    // Add to Homescreen
                    updateNutritionTotals();
                } else {
                    Toast.makeText(this, "Meal type is missing.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void addFoodItemToMeal(FoodData food, String mealType) {
        // Find the container for the specified meal type
        LinearLayout container = getContainerForMealType(mealType);

        if (container != null) {
            // Inflate the food item layout
            LayoutInflater inflater = LayoutInflater.from(this);
            View foodItemView = inflater.inflate(R.layout.food_item_layout, container, false);

            // Set the food data to the views
            TextView foodNameTextView = foodItemView.findViewById(R.id.foodNameTextView);
            TextView servingTextView = foodItemView.findViewById(R.id.servingTextView);
            TextView caloriesTextView = foodItemView.findViewById(R.id.caloriesTextView);
            TextView macrosTextView = foodItemView.findViewById(R.id.macrosTextView);

            foodNameTextView.setText(food.getName());
            servingTextView.setText(food.getServingSize());
            caloriesTextView.setText(String.format(Locale.ENGLISH, "%d kcal", food.getCalories()));
            macrosTextView.setText(String.format(Locale.ENGLISH, "P: %dg, F: %dg, C: %dg",
                    food.getProtein(), food.getFats(), food.getCarbs()));

            // Add delete functionality
            ImageButton deleteButton = foodItemView.findViewById(R.id.deleteButton);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    container.removeView(foodItemView);

                    // Remove from our data structure
                    mealFoodMap.get(mealType).remove(food);

                    // Save changes to preferences
                    saveFoodItems();

                    updateNutritionTotals();


                    // If no items left, show placeholder
                    if (mealFoodMap.get(mealType).isEmpty()) {
                        addPlaceholder(container);
                    }
                }
            });

            // Remove the placeholder text if it exists
            View placeholderView = container.findViewWithTag("placeholder");
            if (placeholderView != null) {
                container.removeView(placeholderView);
            }

            // Add the food item view to the container
            container.addView(foodItemView);
        }
    }

    private LinearLayout getContainerForMealType(String mealType) {
        switch (mealType.toLowerCase()) {
            case "breakfast":
                return findViewById(R.id.breakfastContainer);
            case "lunch":
                return findViewById(R.id.lunchContainer);
            case "dinner":
                return findViewById(R.id.dinnerContainer);
            default:
                return null;
        }
    }

    private void loadFoodItemsForToday() {
        SharedPreferences prefs = getSharedPreferences(FOOD_LOG_PREFS, Context.MODE_PRIVATE);
        String today = getCurrentDate();

        // Load each meal type separately
        for (String mealType : new String[]{"breakfast", "lunch", "dinner"}) {
            String key = today + "_" + mealType;
            Set<String> foodStringSet = prefs.getStringSet(key, new HashSet<>());

            if (foodStringSet != null && !foodStringSet.isEmpty()) {
                List<FoodData> foodList = new ArrayList<>();

                for (String foodString : foodStringSet) {
                    FoodData food = FoodData.fromString(foodString);
                    if (!food.getName().equals("Invalid")) {
                        foodList.add(food);
                    }
                }

                mealFoodMap.put(mealType, foodList);
            }
        }
    }

    private void saveFoodItems() {
        SharedPreferences prefs = getSharedPreferences(FOOD_LOG_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String today = getCurrentDate();

        // Save each meal type separately
        for (String mealType : mealFoodMap.keySet()) {
            String key = today + "_" + mealType;
            List<FoodData> foodList = mealFoodMap.get(mealType);
            Set<String> foodStringSet = new HashSet<>();

            if (foodList != null) {
                for (FoodData food : foodList) {
                    foodStringSet.add(food.toString());
                }
            }

            editor.putStringSet(key, foodStringSet);
        }

        editor.apply();
    }

    // ===== END OF UPDATED CODE =====

    private void displayFoodItemsForMealType(String mealType) {
        LinearLayout container = getContainerForMealType(mealType);
        if (container == null) return;

        // Clear existing views
        container.removeAllViews();

        List<FoodData> foodItems = mealFoodMap.get(mealType);

        if (foodItems == null || foodItems.isEmpty()) {
            // Add placeholder if no items
            addPlaceholder(container);
            return;
        }

        // Add all food items to UI
        for (FoodData food : foodItems) {
            // Create view for each food item
            LayoutInflater inflater = LayoutInflater.from(this);
            View foodItemView = inflater.inflate(R.layout.food_item_layout, container, false);

            // Set the food data to the views
            TextView foodNameTextView = foodItemView.findViewById(R.id.foodNameTextView);
            TextView servingTextView = foodItemView.findViewById(R.id.servingTextView);
            TextView caloriesTextView = foodItemView.findViewById(R.id.caloriesTextView);
            TextView macrosTextView = foodItemView.findViewById(R.id.macrosTextView);

            foodNameTextView.setText(food.getName());
            servingTextView.setText(food.getServingSize());
            caloriesTextView.setText(String.format(Locale.ENGLISH, "%d kcal", food.getCalories()));
            macrosTextView.setText(String.format(Locale.ENGLISH, "P: %dg, F: %dg, C: %dg",
                    food.getProtein(), food.getFats(), food.getCarbs()));

            // Add delete functionality
            ImageButton deleteButton = foodItemView.findViewById(R.id.deleteButton);
            final FoodData finalFood = food;
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    container.removeView(foodItemView);

                    // Remove from our data structure
                    mealFoodMap.get(mealType).remove(finalFood);

                    // Save changes to preferences
                    saveFoodItems();

                    // If no items left, show placeholder
                    if (mealFoodMap.get(mealType).isEmpty()) {
                        addPlaceholder(container);
                    }
                }
            });

            // Add the food item view to the container
            container.addView(foodItemView);
        }
    }

    private void addPlaceholder(LinearLayout container) {
        // Create a placeholder TextView
        TextView placeholder = new TextView(this);
        placeholder.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        placeholder.setText(R.string.no_foods_added);
        placeholder.setPadding(16, 16, 16, 16);
        placeholder.setTag("placeholder");

        container.addView(placeholder);
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(KEY_DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(new Date());
    }


    private void updateNutritionTotals() {
        int totalProtein = 0;
        int totalFats = 0;
        int totalCarbs = 0;
        int totalCalories = 0;

        for (List<FoodData> mealList : mealFoodMap.values()) {
            for (FoodData food : mealList) {
                totalProtein += food.getProtein();
                totalFats += food.getFats();
                totalCarbs += food.getCarbs();
                totalCalories += food.getCalories();
            }
        }

        SharedPreferences prefs = getSharedPreferences("health_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        editor.putInt("proteins_" + today, totalProtein);
        editor.putInt("fats_" + today, totalFats);
        editor.putInt("carbs_" + today, totalCarbs);
        editor.putInt("calories_" + today, totalCalories);
        editor.apply();
    }
}