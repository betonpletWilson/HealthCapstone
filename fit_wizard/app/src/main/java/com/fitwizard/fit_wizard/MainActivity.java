package com.fitwizard.fit_wizard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.fitwizard.fit_wizard.DashboardActivity;
import com.fitwizard.fit_wizard.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load dark mode setting
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Simulating a splash screen delay before redirecting
        new Handler().postDelayed(() -> {
            boolean isLoggedIn = preferences.getBoolean("is_logged_in", false);

            if (isLoggedIn) {
                // User is logged in, go to Dashboard
                startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            } else {
                // User is not logged in, go to Login screen
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
            finish(); // Close MainActivity
        }, 1500); // 1.5-second delay
    }
}
