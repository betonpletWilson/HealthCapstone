package com.fitwizard.fitwizard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    private TextView aboutText, legalText;
    private Button logoutButton;
    private Switch darkModeSwitch;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load dark mode preference before setting the view
        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        aboutText = findViewById(R.id.aboutText);
        legalText = findViewById(R.id.legalText);
        logoutButton = findViewById(R.id.logoutButton);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        ImageButton backButton = findViewById(R.id.backButton);


        aboutText.setText("FitWizard helps you track wellness goals, medications, mood, and more.");
        legalText.setText("HIPAA Notice: Your health data is stored locally and not shared externally. Please consult your healthcare provider for any medical guidance.");

        darkModeSwitch.setChecked(isDarkMode);
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            recreate(); // Restart activity for theme change
        });

        logoutButton.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("current_user");
            editor.putBoolean("is_logged_in", false);
            editor.apply();

            Intent intent = new Intent(SettingsActivity.this, Login_Register.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Finish SettingsActivity
        });



        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

    }
}
