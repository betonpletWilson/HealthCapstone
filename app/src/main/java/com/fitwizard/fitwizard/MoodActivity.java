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
            // Get the resource name for the selected mood
            String mood = getResources().getResourceEntryName(buttonId);

            // Get the drawable resource ID from the ImageButton
            // This emoji will be carried to the next screen
            int drawableResourceId = 0;
            if (buttonId == R.id.face_amazing) {
                drawableResourceId = R.drawable.face_amazing;
            } else if (buttonId == R.id.face_good) {
                drawableResourceId = R.drawable.face_good;
            } else if (buttonId == R.id.face_meh) {
                drawableResourceId = R.drawable.face_meh;
            } else if (buttonId == R.id.face_sad) {
                drawableResourceId = R.drawable.face_sad;
            } else if (buttonId == R.id.face_awful) {
                drawableResourceId = R.drawable.face_awful;
            }

            // Create an intent to navigate to MoodJournalActivity
            Intent intent = new Intent(MoodActivity.this, MoodJournalActivity.class);

            // Pass the drawable resource ID to the next activity
            intent.putExtra("SELECTED_MOOD_RESOURCE_ID", drawableResourceId);



            // Start the activity
            startActivity(intent);
        });
    }
}