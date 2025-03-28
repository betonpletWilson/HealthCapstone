package com.fitwizard.fitwizard;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MoodActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood);

        // Remove the top action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Set current date and time
        TextView dateTimeTextView = findViewById(R.id.dateTimeTextView);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault());
        dateTimeTextView.setText(sdf.format(new Date()));

        // Back button setup
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        // Mood button setups
        setupMoodButton(R.id.face_amazing);
        setupMoodButton(R.id.face_good);
        setupMoodButton(R.id.face_meh);
        setupMoodButton(R.id.face_sad);
        setupMoodButton(R.id.face_awful);
    }

    private void setupMoodButton(int buttonId) {
        ImageButton button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            // Here you would typically log the mood and navigate to the next screen
            // For now, we'll just print the mood
            String mood = getResources().getResourceEntryName(buttonId);
            System.out.println("Selected mood: " + mood);
        });
    }
}