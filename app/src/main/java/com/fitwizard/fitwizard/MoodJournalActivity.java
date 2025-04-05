package com.fitwizard.fitwizard;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
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
        loadMoodData(); //loads all mood tags information

        //back button
        ImageButton backButton = findViewById(R.id.journalBackButton);
        backButton.setOnClickListener(v -> onBackPressed());

        // Set up add tag button
        ImageButton addTagButton = findViewById(R.id.addTagButton);
        addTagButton.setOnClickListener(v -> showAddTagDialog());

        Intent intent = getIntent();
        if (intent != null) {
            // Get resource ID of the selected mood
            int moodResourceId = intent.getIntExtra("SELECTED_MOOD_RESOURCE_ID", -1);

            //This is the upper left emoji that popups next to the back button
            if (moodResourceId != -1) {
                ImageView moodImageView = findViewById(R.id.selectedMoodImageView);

                if (moodImageView != null) {
                    moodImageView.setImageResource(moodResourceId);
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

        // Add click listener
        chip.setOnClickListener(v -> {
            chip.setChecked(!chip.isChecked());
            String tagId = (String) chip.getTag();

            if (chip.isChecked()) {
                selectedTagIds.add(tagId);
            } else {
                selectedTagIds.remove(tagId);
            }
        });

        return chip;
    }


    //TODO: change the function to add tags to the backend
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

        // save this new tag to your backend/database here
        saveMoodDataToBackend(newMoodData);
    }


    //TODO: Push data to the backend when the tag is saved
    /**
     * Example method to save mood data to backend
     * MoodData: tag id's, name's, type's
     */
    private void saveMoodDataToBackend(MoodData moodData) {

    }

    //TODO: change functionality to save the journal entry
    //TODO: change journalContent and remove finish() to functions that will save the journal data
    private void saveJournalEntry() {
        EditText journalEditText = findViewById(R.id.journalEntryEditText);
        String journalContent = journalEditText.getText().toString().trim();

        // Get all selected tags data (not just the strings)
        List<MoodData> selectedTags = new ArrayList<>();
        for (String tagId : selectedTagIds) {
            for (MoodData moodData : moodDataList) {
                if (moodData.getId().equals(tagId)) {
                    selectedTags.add(moodData);
                    break;
                }
            }
        }


        // For now, just show a confirmation message
        Toast.makeText(this, "Journal entry saved with " + selectedTags.size() + " tags", Toast.LENGTH_SHORT).show();
        finish();
    }





    //TODO: change color loading when working with backend
    //Loads the tag with their corresponding color
    private void initializeTagColors() {
        tagColorMap = new HashMap<>();
        tagColorMap.put("emotion", ContextCompat.getColor(this, R.color.green));
        tagColorMap.put("sleep", ContextCompat.getColor(this, R.color.light_pink));
        tagColorMap.put("hobbies", ContextCompat.getColor(this, R.color.light_blue));
        tagColorMap.put("social", ContextCompat.getColor(this, R.color.dark_pink));
    }


    //TODO: Populate tags from backend
    /**
     * Populate the UI with tags from moodDataList
     * Reset and  repopulates the page currently
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


    //TODO: remove this function contents to load the moodDataList from backend
    /**
     * Load mood data from backend data source
     * BELOW IS THE EXAMPLE DATA SOURCE / CURRENT TAG ITEMS
     */
    private void loadMoodData() {
        // Initialize the list
        moodDataList = new ArrayList<>();

        // Add sample data - in a real app, you would get this from your database
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