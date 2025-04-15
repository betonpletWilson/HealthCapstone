package com.fitwizard.fitwizard;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;



//TODO: All functions related to sample data that need to be removed are at the bottom
//TODO: Remove/Change functions with backend connections, VIEW TODOS BELOW / ABOVE FUNCTIONS THAT REQUIRE CHANGES


public class MoodJournalActivity extends AppCompatActivity {

    private ChipGroup emotionTagsChipGroup;
    private ChipGroup sleepTagsChipGroup;
    private ChipGroup hobbiesTagsChipGroup;
    private ChipGroup socialTagsChipGroup;

    // Map to store tag colors by category
    private Map<String, Integer> tagColorMap; // Const tag colors

    // List to store all mood data
    private List<MoodData> moodDataList;

    // Set to track selected tag IDs
    private Set<String> selectedTagIds;

    // Selected mood resource ID
    private int selectedMoodResourceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_journal);

        // Remove the top action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize selected tags set
        selectedTagIds = new HashSet<>();
        initializeUI();
        initializeTagColors();
        loadMoodData(); // loads all mood tags information

        // back button
        ImageButton backButton = findViewById(R.id.journalBackButton);
        backButton.setOnClickListener(v -> onBackPressed());

        // Set up add tag button
        ImageButton addTagButton = findViewById(R.id.addTagButton);
        addTagButton.setOnClickListener(v -> showAddTagDialog());

        Intent intent = getIntent();
        if (intent != null) {
            // Get resource ID of the selected mood
            selectedMoodResourceId = intent.getIntExtra("SELECTED_MOOD_RESOURCE_ID", -1);

            // This is the upper left emoji that popups next to the back button
            if (selectedMoodResourceId != -1) {
                ImageView moodImageView = findViewById(R.id.selectedMoodImageView);

                if (moodImageView != null) {
                    moodImageView.setImageResource(selectedMoodResourceId);
                }
            }
        }
    }

    private void initializeUI() {
        emotionTagsChipGroup = findViewById(R.id.emotionTagsChipGroup);
        sleepTagsChipGroup = findViewById(R.id.sleepTagsChipGroup);
        hobbiesTagsChipGroup = findViewById(R.id.hobbiesTagsChipGroup);
        socialTagsChipGroup = findViewById(R.id.socialTagsChipGroup);

        // Set up save button
        findViewById(R.id.saveJournalButton).setOnClickListener(v -> saveJournalEntry()); // call saveJournalEntry function
    }

    /*
        This is the popup to add and create a new mood tag
        Calls addNewTag which should create a new tag and make it visible for the user
     */
    private void showAddTagDialog() {
        // Inflate dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_tag, null);
        EditText tagNameEditText = dialogView.findViewById(R.id.tagNameEditText);

        // Create the category options
        final String[] categories = {"Emotion", "Sleep", "Hobbies", "Social"};
        final int[] selectedCategory = {0}; // Default to first category

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New Tag")
                .setView(dialogView)
                .setSingleChoiceItems(categories, 0, (dialogInterface, which) -> {
                    selectedCategory[0] = which;
                })
                .setPositiveButton("Add", (dialogInterface, i) -> {
                    String tagName = tagNameEditText.getText().toString().trim();
                    if (!tagName.isEmpty()) {
                        addNewTag(tagName, categories[selectedCategory[0]].toLowerCase());
                    } else {
                        Toast.makeText(MoodJournalActivity.this, "Tag name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
    }

    /**
     * Create a chip from MoodData
     * Stores the ID in the tag
     */
    private Chip createChip(MoodData moodData) {
        Chip chip = new Chip(this);
        chip.setText(moodData.getTagName());
        chip.setCheckable(true);
        chip.setTextColor(getResources().getColor(android.R.color.white));
        chip.setTag(moodData.getId()); // Store the ID in the tag for easy retrieval

        // Set chip color based on tag type
        Integer colorResId = tagColorMap.get(moodData.getTagType());
        if (colorResId != null) {
            chip.setChipBackgroundColor(ColorStateList.valueOf(colorResId));
        }

        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String tagId = (String) chip.getTag();

            if (isChecked) {
                selectedTagIds.add(tagId);
            } else {
                selectedTagIds.remove(tagId);
            }

            // Show the selected tags status
            showSelectedTagsToast();
        });

        // Check if this tag is already selected (for UI restoration)
        if (selectedTagIds.contains(moodData.getId())) {
            chip.setChecked(true);
        }

        return chip;
    }

    /**
     * Shows a toast message with all currently selected tags
     */
    private void showSelectedTagsToast() {
        // Get names of all selected tags
        List<String> selectedTagNames = new ArrayList<>();
        for (String tagId : selectedTagIds) {
            for (MoodData moodData : moodDataList) {
                if (moodData.getId().equals(tagId)) {
                    selectedTagNames.add(moodData.getTagName());
                    break;
                }
            }
        }

        StringBuilder message = new StringBuilder();

        if (selectedTagNames.isEmpty()) {
            message.append("No tags selected");
        } else if (selectedTagNames.size() == 1) {
            message.append("Selected tag: '").append(selectedTagNames.get(0)).append("'");
        } else if (selectedTagNames.size() == 2) {
            message.append("Selected tags: '").append(selectedTagNames.get(0))
                    .append("' and '").append(selectedTagNames.get(1)).append("'");
        } else {
            message.append("Selected tags: ");
            for (int i = 0; i < selectedTagNames.size(); i++) {
                message.append("'").append(selectedTagNames.get(i)).append("'");
                if (i < selectedTagNames.size() - 2) {
                    message.append(", ");
                } else if (i == selectedTagNames.size() - 2) {
                    message.append(", and ");
                }
            }
        }

        // Show the toast
        Toast.makeText(this, message.toString(), Toast.LENGTH_SHORT).show();
    }

    private void addNewTag(String tagName, String category) {
        // Generate a unique ID
        String newId = UUID.randomUUID().toString();

        // Create new MoodData
        MoodData newMoodData = new MoodData(newId, tagName, category);

        // Add to our list
        moodDataList.add(newMoodData);

        // Create and add chip to appropriate group
        Chip chip = createChip(newMoodData);

        switch (category) {
            case "emotion":
                emotionTagsChipGroup.addView(chip);
                break;
            case "sleep":
                sleepTagsChipGroup.addView(chip);
                break;
            case "hobbies":
                hobbiesTagsChipGroup.addView(chip);
                break;
            case "social":
                socialTagsChipGroup.addView(chip);
                break;
        }

        Toast.makeText(this, "Added " + tagName + " to " + category, Toast.LENGTH_SHORT).show();

        // save this new tag to backend/database here
        saveMoodDataToBackend(newMoodData);
    }

    /**
     * Save the new tag to backend database
     */
    private void saveMoodDataToBackend(MoodData moodData) {
        // TODO: Implement database logic here to save a new tag

    }

    /**
     * Save journal entry to backend database
     */
    private void saveJournalEntryToDatabase(MoodData journalEntry) {
        // TODO: Implement database logic here to save journal entry

    }

    private int darkenColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a, r, g, b);
    }

    /**
     * Save the journal entry and selected tags to the database
     */
    private void saveJournalEntry() {
        EditText journalEditText = findViewById(R.id.journalEntryEditText);
        String journalContent = journalEditText.getText().toString().trim();

        // Check if journal content is empty
        if (journalContent.isEmpty()) {
            Toast.makeText(this, "Please write something in your journal", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create journal entry model
        MoodData journalEntry = new MoodData();
        journalEntry.setId(UUID.randomUUID().toString());
        journalEntry.setContent(journalContent);
        journalEntry.setDate(new Date());
        journalEntry.setMoodResourceId(selectedMoodResourceId);

        // Get all selected tags data
        List<MoodData> selectedTags = new ArrayList<>();
        for (String tagId : selectedTagIds) {
            for (MoodData moodData : moodDataList) {
                if (moodData.getId().equals(tagId)) {
                    selectedTags.add(moodData);
                    break;
                }
            }
        }

        // Set the selected tags to the journal entry
        journalEntry.setSelectedTags(selectedTags);

        // Save journal entry to database
        saveJournalEntryToDatabase(journalEntry);

        // Show success message with the number of selected tags
        Toast.makeText(this, "Journal entry saved with " + selectedTags.size() + " tags", Toast.LENGTH_SHORT).show();

        // Navigate to HomeActivity after saving
        Intent intent = new Intent(MoodJournalActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Optional: remove it from the stack
    }

    /**
     * Initialize tag colors for different categories
     */
    private void initializeTagColors() {
        tagColorMap = new HashMap<>();
        tagColorMap.put("emotion", ContextCompat.getColor(this, R.color.green));
        tagColorMap.put("sleep", ContextCompat.getColor(this, R.color.light_pink));
        tagColorMap.put("hobbies", ContextCompat.getColor(this, R.color.light_blue));
        tagColorMap.put("social", ContextCompat.getColor(this, R.color.dark_pink));
    }

    /**
     * Populate the UI with tags from moodDataList
     * Reset and repopulates the page currently
     */
    private void populateTagsUI() {
        // Clear existing chips
        emotionTagsChipGroup.removeAllViews();
        sleepTagsChipGroup.removeAllViews();
        hobbiesTagsChipGroup.removeAllViews();
        socialTagsChipGroup.removeAllViews();

        // Add chips based on tag type
        for (MoodData moodData : moodDataList) {
            Chip chip = createChip(moodData);

            switch (moodData.getTagType()) {
                case "emotion":
                    emotionTagsChipGroup.addView(chip);
                    break;
                case "sleep":
                    sleepTagsChipGroup.addView(chip);
                    break;
                case "hobbies":
                    hobbiesTagsChipGroup.addView(chip);
                    break;
                case "social":
                    socialTagsChipGroup.addView(chip);
                    break;
            }
        }
    }

    /**
     * Load mood data from backend data source
     * Currently contains sample data
     */
    private void loadMoodData() {
        // TODO: Replace with database call to load tags

        // Initialize the list
        moodDataList = new ArrayList<>();

        // Emotion tags
        moodDataList.add(new MoodData("e1", "happy", "emotion"));
        moodDataList.add(new MoodData("e2", "sad", "emotion"));
        moodDataList.add(new MoodData("e3", "Relaxed", "emotion"));
        moodDataList.add(new MoodData("e4", "Sick", "emotion"));
        moodDataList.add(new MoodData("e5", "Yuck", "emotion"));

        // Sleep tags
        moodDataList.add(new MoodData("s1", "Good Sleep", "sleep"));
        moodDataList.add(new MoodData("s2", "Bad Sleep", "sleep"));
        moodDataList.add(new MoodData("s3", "Overslept", "sleep"));
        moodDataList.add(new MoodData("s4", "Ok Sleep", "sleep"));

        // Hobbies tags
        moodDataList.add(new MoodData("h1", "Read", "hobbies"));
        moodDataList.add(new MoodData("h2", "Shop", "hobbies"));
        moodDataList.add(new MoodData("h3", "Work", "hobbies"));
        moodDataList.add(new MoodData("h4", "Relax", "hobbies"));
        moodDataList.add(new MoodData("h5", "Exercise", "hobbies"));

        // Social tags
        moodDataList.add(new MoodData("so1", "Family", "social"));
        moodDataList.add(new MoodData("so2", "Friends", "social"));
        moodDataList.add(new MoodData("so3", "Party", "social"));

        // Populate the UI with the loaded tags
        populateTagsUI();
    }
}