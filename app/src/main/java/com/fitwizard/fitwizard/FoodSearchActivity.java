package com.fitwizard.fitwizard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fitwizard.fitwizard.network.ApiService;

import java.util.ArrayList;
import java.util.List;

public class FoodSearchActivity extends AppCompatActivity {

    private String mealType;
    private EditText searchEditText;
    private ListView foodListView;
    private List<FoodData> foodList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_search);

        // Remove top bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Meal type from Intent
        mealType = getIntent().getStringExtra("MEAL_TYPE");

        // Initialize UI
        searchEditText = findViewById(R.id.searchEditText);
        foodListView   = findViewById(R.id.foodListView);
        TextView title  = findViewById(R.id.titleTextView);
        title.setText("Add " + formatMealType(mealType));

        // Load mock data
        foodList.addAll(getSampleFoodList());
        foodListView.setAdapter(new FoodAdapter(this, foodList));

        // Test DB connection: fetch Greek Yogurt and add to list
        final FoodAdapter adapter = (FoodAdapter) foodListView.getAdapter();
        ApiService.DBRequest(
                "food","name","Greek Yogurt - Plain Nonfat",FoodData.class,
                new ApiService.ApiCallback<FoodData>() {
                    @Override
                    public void onSuccess(FoodData d) {
                        runOnUiThread(() -> {
                            foodList.add(d);
                            adapter.notifyDataSetChanged();
                        });
                    }
                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e("FoodSearchActivity", "DBRequest failed: " + errorMessage);
                    }
                }
        );

        // Item click listener
        foodListView.setOnItemClickListener((parent, view, pos, id) -> {
            FoodData selectedFood = (FoodData) parent.getItemAtPosition(pos);
            showServingsDialog(selectedFood);
        });

        // Back button
        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());

        // Search button
        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v ->
                searchFoods(searchEditText.getText().toString())
        );
    }

    // Show serving size dialog
    private void showServingsDialog(final FoodData food) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_food_search, null);
        builder.setView(dialogView);

        TextView foodNameTxt = dialogView.findViewById(R.id.foodNameTextView);
        TextView mealTypeTxt = dialogView.findViewById(R.id.mealTypeTextView);
        EditText servingsEt = dialogView.findViewById(R.id.servingsEditText);
        Button cancelBtn = dialogView.findViewById(R.id.cancelButton);
        Button addBtn    = dialogView.findViewById(R.id.addButton);

        foodNameTxt.setText(food.getName());
        mealTypeTxt.setText("Adding to: " + formatMealType(mealType));

        AlertDialog dialog = builder.create();
        dialog.show();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        addBtn.setOnClickListener(v -> {
            String servingsText = servingsEt.getText().toString();
            if (servingsText.isEmpty()) {
                Toast.makeText(this,
                        "Please enter the number of servings",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            double servings;
            try {
                servings = Double.parseDouble(servingsText);
            } catch (NumberFormatException e) {
                Toast.makeText(this,
                        "Please enter a valid number",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            saveFoodToMeal(food, mealType, servings);
            dialog.dismiss();
            Toast.makeText(this,
                    food.getName() + " added to " + formatMealType(mealType),
                    Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    // Format meal type string
    private String formatMealType(String mealType) {
        if (mealType == null || mealType.isEmpty()) return "Unknown";
        return mealType.substring(0,1).toUpperCase() + mealType.substring(1);
    }

    // Static mock data
    private List<FoodData> getSampleFoodList() {
        List<FoodData> sample = new ArrayList<>();
        sample.add(new FoodData("Hard Boiled Egg", 78, 6.3f, 5.3f, 0.6f, "1 large egg"));
        sample.add(new FoodData("Green Apple", 95, 0.5f, 0.3f, 25.1f, "1 medium apple"));
        sample.add(new FoodData("Chicken Breast", 165, 31.0f, 3.6f, 0.0f, "100g"));
        sample.add(new FoodData("Apple Juice", 114, 0.1f, 0.2f, 28.0f, "1 cup (240ml)"));
        return sample;
    }

    // Save to meal and return
    private void saveFoodToMeal(FoodData food, String mealType, double servings) {
        double totalCal    = food.getCalories() * servings;
        double totalProt   = food.getProtein_g() * servings;
        double totalFat    = food.getFats_g()   * servings;
        double totalCarb   = food.getCarbs_g()  * servings;

        Intent resultIntent = new Intent();
        resultIntent.putExtra("FOOD_NAME", food.getName());
        resultIntent.putExtra("FOOD_CALORIES", totalCal);
        resultIntent.putExtra("FOOD_PROTEIN", totalProt);
        resultIntent.putExtra("FOOD_FAT", totalFat);
        resultIntent.putExtra("FOOD_CARBS", totalCarb);
        resultIntent.putExtra("FOOD_SERVING", food.getServingSize() + " x " + servings);
        resultIntent.putExtra("MEAL_TYPE", mealType);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    // Search filter
    private void searchFoods(String query) {
        List<FoodData> filtered = new ArrayList<>();
        for (FoodData f : foodList) {
            if (f.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(f);
            }
        }
        foodListView.setAdapter(new FoodAdapter(this, filtered));
    }
}
