package Mood;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitwizard.fitwizard.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

//"Your Journals" screen

public class MoodLogsActivity extends AppCompatActivity {
    private RecyclerView moodLogsRecyclerView;
    private List<MoodData> journalEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_logs);

        // Remove the top action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize UI
        initializeUI();

        // Back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        // Load all journal entries
        loadAllJournalEntries();
    }

    private void initializeUI() {
        moodLogsRecyclerView = findViewById(R.id.moodLogsRecyclerView);
        moodLogsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadAllJournalEntries() {
        SharedPreferences prefs = getSharedPreferences("MoodPrefs", MODE_PRIVATE);
        Set<String> allEntries = prefs.getStringSet("all_journal_entries", new HashSet<>());
        journalEntries = new ArrayList<>();

        // Load all tags first (needed to resolve tag references)
        List<MoodData> allTags = MoodJournalActivity.loadAllTagsFromPrefs(this);

        // Load each entry and add to list
        for (String dateKey : allEntries) {
            String entryString = prefs.getString(dateKey, null);
            if (entryString != null) {
                MoodData entry = MoodData.fromString(entryString);

                // Resolve tag references
                if (entry.getSelectedTags() != null) {
                    List<MoodData> resolvedTags = new ArrayList<>();
                    for (MoodData placeholderTag : entry.getSelectedTags()) {
                        MoodData fullTag = MoodData.findTagById(allTags, placeholderTag.getId());
                        if (fullTag != null) {
                            resolvedTags.add(fullTag);
                        }
                    }
                    entry.setSelectedTags(resolvedTags);
                }

                journalEntries.add(entry);
            }
        }

        // Sort entries by date (newest first)
        Collections.sort(journalEntries, (entry1, entry2) ->
                entry2.getDate().compareTo(entry1.getDate()));

        // Set up adapter
        MoodLogsAdapter adapter = new MoodLogsAdapter(this, journalEntries);
        moodLogsRecyclerView.setAdapter(adapter);
    }


}