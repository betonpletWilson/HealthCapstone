package com.fitwizard.fitwizard;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import Goals.GoalsActivity;
import Medication.MedicationActivity;
import Exercise_Logging.ExerciseHistoryActivity;
import Mood.MoodLogsActivity;
import Recipe_Logging.FoodLogActivity;
import Reminders.NotificationsActivity;
import Mood.MoodActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView proteinsValue, fatsValue, carbsValue, caloriesValue;
    private WaterLevelView waterLevelView;
    private TextView waterAmount, waterTime, dateText, usernameText;
    private float currentWater = 1.9f;
    final float waterGoal = 2.5f;

    private ProgressBar proteinsProgress, fatsProgress, carbsProgress, caloriesProgress;
    private FloatingActionButton addWaterBtn, subtractWaterBtn, addFab;
    private LinearLayout addMenu;

    private final String channelId = "i.apps.notifications";

    private Button addMealButton, logMoodButton, newNotifButton, medicationsButton, logExerciseButton;

    private static final String PREFS_NAME = "health_data";
    private static final String KEY_WATER = "water_";
    private static final String KEY_TIME = "water_time_";
    private static final String KEY_PROTEINS = "proteins_";
    private static final String KEY_FATS = "fats_";
    private static final String KEY_CARBS = "carbs_";
    private static final String KEY_CALORIES = "calories_";
    private static final String KEY_LAST_UPDATE_DATE = "last_update_date";



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d("NotificationPermission", "Granted");
            } else {
                // Permission denied
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putFloat(getTodayKey(KEY_WATER), currentWater);
        editor.putString(getTodayKey(KEY_TIME), waterTime.getText().toString());

        editor.putInt(getTodayKey(KEY_PROTEINS), proteinsProgress.getProgress());
        editor.putInt(getTodayKey(KEY_FATS), fatsProgress.getProgress());
        editor.putInt(getTodayKey(KEY_CARBS), carbsProgress.getProgress());
        editor.putInt(getTodayKey(KEY_CALORIES), caloriesProgress.getProgress());

        editor.apply();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        checkExactAlarmPermission();

        /*
        // Ask for notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }

         */



        if (getSupportActionBar() != null) getSupportActionBar().hide();


        initializeViews();
        setupProfileImage();
        setupNutrientIndicators();
        setupWaterControls();
        setupPopupNavMenu();
        loadUserData();
    }

    private static final int EXACT_ALARM_PERMISSION_REQUEST_CODE = 1002;

    /**
     * Checks and requests permission to schedule exact alarms (for Android 12+)
     */
    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            if (!alarmManager.canScheduleExactAlarms()) {
                // Show an explanation to the user
                new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("To ensure your reminders work correctly, please enable the 'Alarms & Reminders' permission for this app.")
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            // Open settings screen for exact alarm permission
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, EXACT_ALARM_PERMISSION_REQUEST_CODE);
                        })
                        .setNegativeButton("Not Now", (dialog, which) -> {
                            Toast.makeText(this,
                                    "Reminders may not work exactly on time without this permission",
                                    Toast.LENGTH_LONG).show();
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }

    private void initializeViews() {
        // UI components
        waterAmount = findViewById(R.id.water_amount);
        waterTime = findViewById(R.id.water_time);
        waterLevelView = findViewById(R.id.water_level);
        usernameText = findViewById(R.id.username_text);

        // Progress Bars
        proteinsProgress = findViewById(R.id.proteins_progress);
        fatsProgress = findViewById(R.id.fats_progress);
        carbsProgress = findViewById(R.id.carbs_progress);
        caloriesProgress = findViewById(R.id.calories_progress);

        proteinsValue = findViewById(R.id.proteins_value);
        fatsValue = findViewById(R.id.fats_value);
        carbsValue = findViewById(R.id.carbs_value);
        caloriesValue = findViewById(R.id.calories_value);


        // Water buttons
        addWaterBtn = findViewById(R.id.water_add);
        subtractWaterBtn = findViewById(R.id.water_subtract);

        // Popup menu components
        addFab = findViewById(R.id.fab_add);
        addMenu = findViewById(R.id.add_menu);
        logExerciseButton = findViewById(R.id.btn_log_exercise);
        addMealButton = findViewById(R.id.btn_add_meal);
        logMoodButton = findViewById(R.id.btn_log_mood);
        newNotifButton = findViewById(R.id.btn_new_notif);
        medicationsButton = findViewById(R.id.btn_medications);

        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Click listener for setting custom water amount
        waterAmount.setOnClickListener(v -> showWaterInputDialog());

        CardView moodCard = findViewById(R.id.mood_card);
        moodCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MoodLogsActivity.class);
            startActivity(intent);
        });



        Button goalsBtn = findViewById(R.id.btn_goals);
        goalsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, GoalsActivity.class);
            intent.putExtra("reset_goal", true); // optional if you want it force-reset every time
            startActivity(intent);

        });

    }

    private String getTodayKey(String baseKey) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return baseKey + today;
    }

    private void setupProfileImage() {
        ImageView profileImage = findViewById(R.id.profile_image);
        profileImage.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
    }

    private void setupNutrientIndicators() {
        proteinsProgress.setProgress(67);  // TODO: Replace with dynamic data
        fatsProgress.setProgress(25);
        carbsProgress.setProgress(94);
        caloriesProgress.setProgress(72);
    }

    private void setupWaterControls() {
        addWaterBtn.setOnClickListener(v -> modifyWaterAmount(0.1f));
        subtractWaterBtn.setOnClickListener(v -> modifyWaterAmount(-0.1f));

        // Initial display update
        updateWaterDisplay();
    }

    private void modifyWaterAmount(float amount) {
        currentWater = Math.max(0, currentWater + amount);
        updateWaterDisplay();
        updateWaterTime();
    }

    private void updateWaterDisplay() {
        waterAmount.setText(String.format(Locale.getDefault(), "%.1f / %.1fL", currentWater, waterGoal));
        waterLevelView.setWaterLevel(Math.min(currentWater / waterGoal, 1.0f));
    }

    private void updateWaterTime() {
        String formattedTime = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
        waterTime.setText(getString(R.string.last_time_text, formattedTime));
    }

    private void showWaterInputDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter water amount (L)");
        input.setText(String.valueOf(currentWater));

        new AlertDialog.Builder(this)
                .setTitle("Set Water Amount")
                .setView(input)
                .setPositiveButton("Set", (dialog, which) -> {
                    try {
                        float newAmount = Float.parseFloat(input.getText().toString());
                        if (newAmount >= 0) {
                            currentWater = newAmount;
                            updateWaterDisplay();
                            updateWaterTime();
                        } else {
                            showToast("Please enter a positive value");
                        }
                    } catch (NumberFormatException e) {
                        showToast("Please enter a valid number");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }



    private void setupPopupNavMenu() {
        // Move the popup menu outside the bottom nav card
        addFab.setOnClickListener(v -> togglePopupMenu());

        addMealButton.setOnClickListener(v -> openActivity(FoodLogActivity.class));

        // Modified mood button logic
        logMoodButton.setOnClickListener(v -> {
            checkAndNavigateToMoodActivity();
        });

        newNotifButton.setOnClickListener(v -> openActivity(NotificationsActivity.class));
        medicationsButton.setOnClickListener(v -> openActivity(MedicationActivity.class));
        logExerciseButton.setOnClickListener(v -> openActivity(ExerciseHistoryActivity.class));
    }

    private void checkAndNavigateToMoodActivity() {
        // Format today's date key
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayKey = keyFormat.format(new Date());

        // Load from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MoodPrefs", MODE_PRIVATE);
        String savedEntry = prefs.getString(todayKey, null);

        if (savedEntry != null) {
            // Entry exists for today, go to MoodEntryDetailActivity
            try {
                // Parse the saved entry to get the full date string
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault());
                Date currentDate = new Date(); // Use the current date
                String fullDateString = dateFormat.format(currentDate);

                // Create intent for detail activity
                Intent intent = new Intent(HomeActivity.this, MoodLogsActivity.class);
                intent.putExtra("ENTRY_DATE", fullDateString);
                startActivity(intent);
            } catch (Exception e) {
                // If any error occurs, default to the MoodActivity
                Intent intent = new Intent(HomeActivity.this, MoodActivity.class);
                startActivity(intent);
            }
        } else {
            // No entry exists for today, go to the mood selection activity
            Intent intent = new Intent(HomeActivity.this, MoodActivity.class);
            startActivity(intent);
        }
    }



    private void togglePopupMenu() {
        if (addMenu.getVisibility() == View.GONE) {
            addMenu.setVisibility(View.VISIBLE);
            addMenu.setAlpha(0f);
            addMenu.setTranslationY(100f);
            addMenu.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(200)
                    .start();
        } else {
            addMenu.animate()
                    .alpha(0f)
                    .translationY(100f)
                    .setDuration(200)
                    .withEndAction(() -> addMenu.setVisibility(View.GONE))
                    .start();
        }
    }

    private void openActivity(Class<?> activityClass) {
        startActivity(new Intent(HomeActivity.this, activityClass));
        addMenu.setVisibility(View.GONE);
    }



    private void showToast(String message) {
        Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    // 👇 Load user's name from shared preferences
    private void loadUserData() {
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userName = preferences.getString("user_name", "GetName");
        usernameText.setText(userName);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastUpdate = prefs.getString(KEY_LAST_UPDATE_DATE, "");

        // ✅ Reset data at midnight before loading values
        if (!today.equals(lastUpdate)) {
            editor.putString(KEY_LAST_UPDATE_DATE, today);
            editor.putFloat(getTodayKey(KEY_WATER), 0f);
            editor.putString(getTodayKey(KEY_TIME), "");
            editor.putInt(getTodayKey(KEY_PROTEINS), 0);
            editor.putInt(getTodayKey(KEY_FATS), 0);
            editor.putInt(getTodayKey(KEY_CARBS), 0);
            editor.putInt(getTodayKey(KEY_CALORIES), 0);
            editor.apply();
        }

        // ✅ Load water
        currentWater = prefs.getFloat(getTodayKey(KEY_WATER), 0f);
        waterAmount.setText(String.format(Locale.getDefault(), "%.1f / %.1fL", currentWater, waterGoal));
        waterLevelView.setWaterLevel(Math.min(currentWater / waterGoal, 1.0f));

        String timeText = prefs.getString(getTodayKey(KEY_TIME), "");
        if (!timeText.isEmpty()) {
            waterTime.setText(timeText);
        } else {
            waterTime.setText(""); // Clear text if no time saved
        }

        // ✅ Load nutrient indicators (start at 0 if not set)
        proteinsProgress.setProgress(prefs.getInt(getTodayKey(KEY_PROTEINS), 0));
        fatsProgress.setProgress(prefs.getInt(getTodayKey(KEY_FATS), 0));
        carbsProgress.setProgress(prefs.getInt(getTodayKey(KEY_CARBS), 0));
        caloriesProgress.setProgress(prefs.getInt(getTodayKey(KEY_CALORIES), 0));

        int protein = prefs.getInt(getTodayKey(KEY_PROTEINS), 0);
        int fat = prefs.getInt(getTodayKey(KEY_FATS), 0);
        int carbs = prefs.getInt(getTodayKey(KEY_CARBS), 0);
        int calories = prefs.getInt(getTodayKey(KEY_CALORIES), 0);

        proteinsProgress.setProgress(protein);
        fatsProgress.setProgress(fat);
        carbsProgress.setProgress(carbs);
        caloriesProgress.setProgress(calories);

// Update the text values (Assume goals: 225g protein, 118g fat, 340g carbs, 3400 cal)
        proteinsValue.setText(protein + " / 225");
        fatsValue.setText(fat + " / 118");
        carbsValue.setText(carbs + " / 340");
        caloriesValue.setText(calories + " / 3400");

    }
}
