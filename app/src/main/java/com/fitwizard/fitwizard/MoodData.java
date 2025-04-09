package com.fitwizard.fitwizard;

import java.util.List;
import java.util.Date;

public class MoodData {
    private String id;
    private String content;
    private Date date;
    private int moodResourceId;
    private List<MoodData> selectedTags;
    private String tagName;
    private String tagType;

    public MoodData() {
        // Empty constructor
    }
    public MoodData(String id, String tagName, String tagType) {
        this.id = id;
        this.tagName = tagName;
        this.tagType = tagType;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getTagType() { return tagType;}

    public String getTagName() { return tagName; }

    public String getContent() { return content; }

    public void setId(String id) { this.id = id; }

    public void setContent(String content) { this.content = content; }

    public void setTagName(String tagName) { this.tagName = tagName; }


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
}