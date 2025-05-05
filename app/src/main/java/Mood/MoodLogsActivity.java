package Mood;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitwizard.fitwizard.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MoodLogsActivity extends AppCompatActivity {
    private ImageView moodImageView;
    private TextView dateTextView;
    private TextView journalEntryTextView;
    private LinearLayout tagsContainer;


    private static final String PREFS_NAME = "MoodJournalPrefs";
    private static final String TAGS_KEY = "user_tags";
    private static final String ENTRIES_KEY = "mood_entries";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_logs);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Back button setup
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        moodImageView = findViewById(R.id.moodImageView);
        dateTextView = findViewById(R.id.dateTextView);
        journalEntryTextView = findViewById(R.id.journalEntryTextView);
        tagsContainer = findViewById(R.id.tagsContainer);

        // Load today's data
        MoodData todayMood = loadTodayMoodData();
        if (todayMood != null) {
            displayMoodData(todayMood);
        } else {
            Toast.makeText(this, "No mood data available for today.", Toast.LENGTH_SHORT).show();
        }

    }

    private MoodData loadTodayMoodData() {
        SharedPreferences prefs = getSharedPreferences("MoodPrefs", MODE_PRIVATE);
        String todayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String moodString = prefs.getString(todayKey, null);

        if (moodString != null) {
            return MoodData.fromString(moodString);
        } else {
            return null;
        }
    }

    private void displayMoodData(MoodData moodData) {
        // Mood image
        if (moodData.getMoodResourceId() != 0) {
            moodImageView.setImageResource(moodData.getMoodResourceId());
        }

        // Date
        if (moodData.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            dateTextView.setText(sdf.format(moodData.getDate()));
        }

        // Journal entry
        journalEntryTextView.setText(moodData.getContent() != null ? moodData.getContent() : "No entry.");

        // Tags
        tagsContainer.removeAllViews();
        List<MoodData> tags = moodData.getSelectedTags();
        if (tags != null && !tags.isEmpty()) {
            for (MoodData tag : tags) {
                TextView tagView = new TextView(this);
                tagView.setText(tag.getTagName());
                tagView.setPadding(16, 8, 16, 8);
                tagView.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
                tagView.setTextSize(14f);
                tagView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                tagsContainer.addView(tagView);
            }
        }
    }
}