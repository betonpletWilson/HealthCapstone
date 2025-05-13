package Mood;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


public class MoodData {
    private String id;
    private String tagName;
    private String tagType;
    private String content;
    private Date date;
    private int moodResourceId;
    private List<MoodData> selectedTags;
    // Date format for serialization

    // Default constructor
    public MoodData() {
        selectedTags = new ArrayList<>();
    }

    // Constructor for tags
    public MoodData(String id, String tagName, String tagType) {
        this.id = id;
        this.tagName = tagName;
        this.tagType = tagType;
        this.selectedTags = new ArrayList<>();
    }

    // Complete constructor for journal entries
    public MoodData(String id, String tagName, String tagType, String content, Date date,
                    int moodResourceId, List<MoodData> selectedTags) {
        this.id = id;
        this.tagName = tagName;
        this.tagType = tagType;
        this.content = content;
        this.date = date;
        this.moodResourceId = moodResourceId;
        this.selectedTags = selectedTags != null ? selectedTags : new ArrayList<>();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getMoodResourceId() {
        return moodResourceId;
    }

    public void setMoodResourceId(int moodResourceId) {
        this.moodResourceId = moodResourceId;
    }

    public List<MoodData> getSelectedTags() {
        return selectedTags;
    }

    public void setSelectedTags(List<MoodData> selectedTags) {
        this.selectedTags = selectedTags;
    }

    public static List<String> getSelectedTagIdsFromString(String moodString) {
        List<String> tagIds = new ArrayList<>();
        if (moodString == null || moodString.trim().isEmpty()) return tagIds;

        String[] parts = moodString.split("\\|");
        if (parts.length < 7) return tagIds;

        // Tag IDs are expected at index 6 (last field)
        String tagIdString = parts[6];
        if (!tagIdString.isEmpty()) {
            String[] idArray = tagIdString.split(",");
            tagIds.addAll(Arrays.asList(idArray));
        }

        return tagIds;
    }


    @Override
    public String toString() {
        // Format: id;tagName;tagType;content;dateInMillis;moodResourceId;tag1,tag2,tag3
        StringBuilder sb = new StringBuilder();
        sb.append(id != null ? id : "").append(";");
        sb.append(tagName != null ? tagName : "").append(";");
        sb.append(tagType != null ? tagType : "").append(";");
        sb.append(content != null ? content : "").append(";");
        sb.append(date != null ? date.getTime() : 0).append(";");
        sb.append(moodResourceId).append(";");

        // Append selectedTags IDs as comma-separated values
        if (selectedTags != null && !selectedTags.isEmpty()) {
            for (int i = 0; i < selectedTags.size(); i++) {
                sb.append(selectedTags.get(i).getId());
                if (i < selectedTags.size() - 1) {
                    sb.append(",");
                }
            }
        }

        return sb.toString();
    }

    //  Needed for loading from SharedPreferences
    public static MoodData fromString(String moodDataString) {
        String[] parts = moodDataString.split(";");
        if (parts.length >= 6) { // At least 6 parts (may not have tags)
            MoodData moodData = new MoodData();

            moodData.setId(parts[0]);
            moodData.setTagName(parts[1]);
            moodData.setTagType(parts[2]);
            moodData.setContent(parts[3]);

            // Parse date
            try {
                long dateMillis = Long.parseLong(parts[4]);
                if (dateMillis > 0) {
                    moodData.setDate(new Date(dateMillis));
                }
            } catch (NumberFormatException e) {
                moodData.setDate(new Date()); // Default to current date if parsing fails
            }

            // Parse moodResourceId
            try {
                moodData.setMoodResourceId(Integer.parseInt(parts[5]));
            } catch (NumberFormatException e) {
                moodData.setMoodResourceId(-1); // Default value if parsing fails
            }

            // Parse selectedTags if available (part index 6)
            if (parts.length > 6 && !parts[6].isEmpty()) {
                String[] tagIds = parts[6].split(",");
                List<MoodData> placeholderTags = new ArrayList<>();

                for (String tagId : tagIds) {
                    // Create placeholder tags with IDs only - to be replaced later with full tags
                    MoodData tag = new MoodData();
                    tag.setId(tagId);
                    placeholderTags.add(tag);
                }

                moodData.setSelectedTags(placeholderTags);
            }

            return moodData;
        } else {
            // If data is broken
            return new MoodData();
        }
    }

    public static MoodData findTagById(List<MoodData> allTags, String tagId) {
        for (MoodData tag : allTags) {
            if (tag.getId().equals(tagId)) {
                return tag;
            }
        }
        return null;
    }

    // Helper method to get a list of tag IDs
    public List<String> getSelectedTagIds() {
        List<String> tagIds = new ArrayList<>();
        if (selectedTags != null) {
            for (MoodData tag : selectedTags) {
                if (tag != null && tag.getId() != null) {
                    tagIds.add(tag.getId());
                }
            }
        }
        return tagIds;
    }
}