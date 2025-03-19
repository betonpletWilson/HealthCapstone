package com.fitwizard.fitwizard;


// Formating and information to be pulled
//Used by FoodSearchActivity
//Used by FoodApapter for getView()
public class FoodData {

    private String name;
    private int calories;
    private int protein;
    private int fats;
    private int carbs;
    private String servingSize;

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
    public int getCalories() { return calories; }
    public int getProtein() { return protein; }
    public int getFats() { return fats; }
    public int getCarbs() { return carbs; }
    public String getServingSize() { return servingSize; }
}

