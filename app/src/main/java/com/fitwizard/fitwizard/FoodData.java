package com.fitwizard.fitwizard;



// Formating and information to be pulled
//Used by FoodSearchActivity
//Used by FoodAdapter for getView()
public class FoodData {

    private final String name;
    private final int calories;
    private final float protein;
    private final float fats;
    private final float carbs;
    private final String servingSize;

    //Food info to get
    public FoodData(String name, int calories, int protein, int fats, int carbs, String servingSize) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.fats = fats;
        this.carbs = carbs;
        this.servingSize = servingSize;
    }

    public String getName() { return name; }
    public int getCalories() { return calories; }  // keep as an INT value
    public float getProtein_g() { return protein; }
    public float getFats_g() { return fats; }
    public float getCarbs_g() { return carbs; }
    public String getServingSize() { return servingSize; }
}

