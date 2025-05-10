package Recipe_Logging;


// Formating and information to be pulled
//Used by FoodSearchActivity
//Used by FoodAdapter for getView()
import java.io.Serializable;

public class FoodData {
    private final String name;
    private final int calories;
    private final int protein;
    private final int fats;
    private final int carbs;
    private final String servingSize;

    // Constructor
    public FoodData(String name, int calories, int protein, int fats, int carbs, String servingSize) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.fats = fats;
        this.carbs = carbs;
        this.servingSize = servingSize;
    }

    // Getters
    public String getName() { return name; }
    public int getCalories() { return calories; }
    public int getProtein() { return protein; }
    public int getFats() { return fats; }
    public int getCarbs() { return carbs; }
    public String getServingSize() { return servingSize; }

    // Method to save to SharedPreferences - ⭐ Similar to Goal class
    @Override
    public String toString() {
        return name + ";" + calories + ";" + protein + ";" + fats + ";" + carbs + ";" + servingSize;
    }

    // Method to load from SharedPreferences - ⭐ Similar to Goal class
    public static FoodData fromString(String foodString) {
        String[] parts = foodString.split(";");
        if (parts.length == 6) {
            String name = parts[0];
            int calories = Integer.parseInt(parts[1]);
            int protein = Integer.parseInt(parts[2]);
            int fats = Integer.parseInt(parts[3]);
            int carbs = Integer.parseInt(parts[4]);
            String servingSize = parts[5];
            return new FoodData(name, calories, protein, fats, carbs, servingSize);
        } else {
            // If data is broken
            return new FoodData("Invalid", 0, 0, 0, 0, "");
        }
    }

    // For identification in lists (used when removing items)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        FoodData food = (FoodData) obj;
        return calories == food.calories &&
                protein == food.protein &&
                fats == food.fats &&
                carbs == food.carbs &&
                name.equals(food.name) &&
                servingSize.equals(food.servingSize);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + calories;
        result = 31 * result + protein;
        result = 31 * result + fats;
        result = 31 * result + carbs;
        result = 31 * result + servingSize.hashCode();
        return result;
    }
}