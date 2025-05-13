package Mood;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitwizard.fitwizard.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//This is the screen with emojis in a straight line / How are you today? screen
//


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


        Button calendarButton = findViewById(R.id.open_calendar_button);
        calendarButton.setOnClickListener(v -> {
            // Format today's date key
            SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String todayKey = keyFormat.format(new Date());

            // Load from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("MoodPrefs", MODE_PRIVATE);
            String savedEntry = prefs.getString(todayKey, null);

            if (savedEntry != null) {
                Intent intent = new Intent(MoodActivity.this, MoodLogsActivity.class);
                intent.putExtra("MOOD_DATA_STRING", savedEntry);
                startActivity(intent);
            } else {
                Toast.makeText(MoodActivity.this, "No entry found for today.", Toast.LENGTH_SHORT).show();
            }
        });
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