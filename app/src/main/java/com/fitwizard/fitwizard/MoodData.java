package com.fitwizard.fitwizard;

public class MoodData {
    private String id;
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
}

