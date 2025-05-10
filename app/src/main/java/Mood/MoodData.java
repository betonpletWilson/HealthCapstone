package Mood;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class MoodData {
    private String id;
    private String tagName;
    private String tagType;
    private String content;
    private Date date;
    private int moodResourceId;
    private List<MoodData> selectedTags;

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
        StringBuilder sb = new StringBuilder();

        // Add primary fields with semicolon separator (like Goal class)
        sb.append(id).append(";")
                .append(date != null ? date.getTime() : 0).append(";")
                .append(tagName != null ? tagName.replace(";", "\\;") : "").append(";")
                .append(tagType != null ? tagType : "").append(";")
                .append(content != null ? content.replace(";", "\\;") : "").append(";")
                .append(moodResourceId).append(";");

        // Add selected tag IDs with comma separator
        if (selectedTags != null && !selectedTags.isEmpty()) {
            for (int i = 0; i < selectedTags.size(); i++) {
                MoodData tag = selectedTags.get(i);
                if (tag != null && tag.getId() != null) {
                    sb.append(tag.getId());
                    if (i < selectedTags.size() - 1) {
                        sb.append(",");
                    }
                }
            }
        }

        return sb.toString();
    }

    // Needed for loading from SharedPreferences
    public static MoodData fromString(String moodString) {
        if (moodString == null || moodString.isEmpty()) {
            return null;
        }

        String[] parts = moodString.split(";");
        if (parts.length < 6) {
            // Invalid format, return default MoodData
            return new MoodData(UUID.randomUUID().toString(), "Invalid", "invalid");
        }

        String id = parts[0];
        long dateMillis = Long.parseLong(parts[1]);
        Date date = new Date(dateMillis);
        String tagName = parts[2].replace("\\;", ";");
        String tagType = parts[3];
        String content = parts[4].replace("\\;", ";");
        int moodResourceId = Integer.parseInt(parts[5]);

        // Create the base MoodData object
        MoodData moodData = new MoodData();
        moodData.setId(id);
        moodData.setDate(date);
        moodData.setTagName(tagName);
        moodData.setTagType(tagType);
        moodData.setContent(content);
        moodData.setMoodResourceId(moodResourceId);

        // Process selected tags if present
        List<MoodData> selectedTags = new ArrayList<>();
        if (parts.length > 6 && !parts[6].isEmpty()) {
            String[] tagIds = parts[6].split(",");
            for (String tagId : tagIds) {
                if (!tagId.isEmpty()) {
                    MoodData tag = new MoodData();
                    tag.setId(tagId);
                    selectedTags.add(tag);
                }
            }
        }
        moodData.setSelectedTags(selectedTags);

        return moodData;
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