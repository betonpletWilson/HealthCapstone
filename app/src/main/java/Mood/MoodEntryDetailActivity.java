package Mood;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.fitwizard.fitwizard.R;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MoodEntryDetailActivity extends AppCompatActivity {

    private ImageView moodImageView;
    private TextView dateTextView;
    private TextView contentTextView;
    private FlexboxLayout tagsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_entry_detail);

        // Remove the top action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize UI components
        moodImageView = findViewById(R.id.detailMoodImageView);
        dateTextView = findViewById(R.id.detailDateTextView);
        contentTextView = findViewById(R.id.detailContentTextView);
        tagsContainer = findViewById(R.id.detailTagsContainer);

        // Back button
        ImageButton backButton = findViewById(R.id.BackButton);
        backButton.setOnClickListener(v -> onBackPressed());

        // Get date from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("ENTRY_DATE")) {
            String dateString = intent.getStringExtra("ENTRY_DATE");
            dateTextView.setText(dateString);

            // Parse date to load entry
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault());
                Date entryDate = dateFormat.parse(dateString);
                if (entryDate != null) {
                    loadEntry(entryDate);
                }
            } catch (ParseException e) {
                Toast.makeText(this, "Error loading entry", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "No entry date provided", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadEntry(Date entryDate) {
        // Load entry from SharedPreferences
        MoodData entry = MoodJournalActivity.loadJournalEntryFromPrefs(this, entryDate);

        if (entry != null) {
            // Set mood image
            if (entry.getMoodResourceId() != -1) {
                moodImageView.setImageResource(entry.getMoodResourceId());
            }

            // Set content
            contentTextView.setText(entry.getContent());

            // Add tag chips
            addTagChips(entry.getSelectedTags());
        } else {
            Toast.makeText(this, "Entry not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void addTagChips(List<MoodData> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }

        tagsContainer.removeAllViews();
        Map<String, Integer> tagColorMap = createTagColorMap();

        for (MoodData tag : tags) {
            Chip chip = new Chip(this);
            chip.setText(tag.getTagName());
            chip.setCheckable(false);
            chip.setClickable(false);
            chip.setTextColor(getResources().getColor(android.R.color.white));

            // Set color based on tag type
            Integer colorResId = tagColorMap.get(tag.getTagType());
            if (colorResId != null) {
                chip.setChipBackgroundColor(ColorStateList.valueOf(colorResId));
            }

            // Add to container
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 8, 8, 8);
            tagsContainer.addView(chip, params);
        }
    }

    private Map<String, Integer> createTagColorMap() {
        Map<String, Integer> tagColorMap = new HashMap<>();
        tagColorMap.put("emotion", ContextCompat.getColor(this, R.color.green));
        tagColorMap.put("sleep", ContextCompat.getColor(this, R.color.light_pink));
        tagColorMap.put("hobbies", ContextCompat.getColor(this, R.color.light_blue));
        tagColorMap.put("social", ContextCompat.getColor(this, R.color.dark_pink));
        return tagColorMap;
    }
}
