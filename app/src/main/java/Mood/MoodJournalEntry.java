package Mood;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MoodJournalEntry {

    private static final String JOURNAL_PREFS_NAME = "JournalEntryPrefs";
    private static final String ENTRIES_KEY = "journal_entries";
    private static final String TAGS_PREFS_NAME = "MoodJournalPrefs";
    private static final String TAGS_KEY = "user_tags";

    private Context context;

    public MoodJournalEntry(Context context) {
        this.context = context;
    }

    /**
     * Save a journal entry to SharedPreferences
     */


    /**
     * Get all journal entries from SharedPreferences
     */
    public List<MoodData> getAllJournalEntries() {
        List<MoodData> entries = new ArrayList<>();

        // Get all tag data first
        List<MoodData> allTags = getAllTags();

        // Load entries from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(JOURNAL_PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> entryStrings = prefs.getStringSet(ENTRIES_KEY, new HashSet<>());

        for (String entryString : entryStrings) {
            MoodData entry = MoodData.fromString(entryString);
            if (entry != null) {
                // Populate the selected tags list with full tag objects
                List<String> tagIds = MoodData.getSelectedTagIdsFromString(entryString);
                List<MoodData> selectedTags = new ArrayList<>();

                for (String tagId : tagIds) {
                    for (MoodData tag : allTags) {
                        if (tag.getId().equals(tagId)) {
                            selectedTags.add(tag);
                            break;
                        }
                    }
                }

                entry.setSelectedTags(selectedTags);
                entries.add(entry);
            }
        }

        return entries;
    }

    /**
     * Get a specific journal entry by its ID
     */
    public MoodData getJournalEntryById(String entryId) {
        List<MoodData> allEntries = getAllJournalEntries();

        for (MoodData entry : allEntries) {
            if (entry.getId().equals(entryId)) {
                return entry;
            }
        }

        return null;
    }

    /**
     * Delete a journal entry by its ID
     */
    public boolean deleteJournalEntry(String entryId) {
        SharedPreferences prefs = context.getSharedPreferences(JOURNAL_PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> entries = prefs.getStringSet(ENTRIES_KEY, new HashSet<>());

        // Create a mutable copy
        Set<String> updatedEntries = new HashSet<>();
        boolean found = false;

        // Copy all entries except the one to delete
        for (String entryString : entries) {
            MoodData entry = MoodData.fromString(entryString);
            if (entry != null && !entry.getId().equals(entryId)) {
                updatedEntries.add(entryString);
            } else if (entry != null) {
                found = true;
            }
        }

        // Only save if we actually found and removed the entry
        if (found) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putStringSet(ENTRIES_KEY, updatedEntries);
            editor.apply();
            return true;
        }

        return false;
    }

    /**
     * Get all tags from SharedPreferences
     */
    private List<MoodData> getAllTags() {
        List<MoodData> tags = new ArrayList<>();

        SharedPreferences prefs = context.getSharedPreferences(TAGS_PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> tagStrings = prefs.getStringSet(TAGS_KEY, new HashSet<>());

        for (String tagString : tagStrings) {
            MoodData tag = MoodData.fromString(tagString);
            if (tag != null) {
                tags.add(tag);
            }
        }

        return tags;
    }

    /**
     * Get journal entries for a specific date range
     */
    public List<MoodData> getEntriesForDateRange(Date startDate, Date endDate) {
        List<MoodData> allEntries = getAllJournalEntries();
        List<MoodData> filteredEntries = new ArrayList<>();

        for (MoodData entry : allEntries) {
            Date entryDate = entry.getDate();
            if (entryDate != null && !entryDate.before(startDate) && !entryDate.after(endDate)) {
                filteredEntries.add(entry);
            }
        }

        return filteredEntries;
    }

    /**
     * Get journal entries with a specific tag
     */
    public List<MoodData> getEntriesWithTag(String tagId) {
        List<MoodData> allEntries = getAllJournalEntries();
        List<MoodData> filteredEntries = new ArrayList<>();

        for (MoodData entry : allEntries) {
            List<MoodData> selectedTags = entry.getSelectedTags();
            if (selectedTags != null) {
                for (MoodData tag : selectedTags) {
                    if (tag.getId().equals(tagId)) {
                        filteredEntries.add(entry);
                        break;
                    }
                }
            }
        }

        return filteredEntries;
    }
}
