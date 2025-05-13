package Recipe_Logging;


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
    public FoodData(String name, int calories, float protein, float fats, float carbs, String servingSize) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.fats = fats;
        this.carbs = carbs;
        this.servingSize = servingSize;
    }

    // Getters
    public String getName() { return name; }
    public int getCalories() { return calories; }  // keep as an INT value
    public float getProtein() { return protein; }  //change to _g
    public float getFats() { return fats; }    //Change to _g
    public float getCarbs() { return carbs; }   //Change to _g
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
        result = 31 * result + Float.hashCode(calories);
        result = 31 * result + Float.hashCode(protein);
        result = 31 * result + Float.hashCode(fats);
        result = 31 * result + Float.hashCode(carbs);
        result = 31 * result + servingSize.hashCode();
        return result;
    }
}