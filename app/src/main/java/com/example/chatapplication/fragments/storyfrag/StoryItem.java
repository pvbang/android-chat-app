package com.example.chatapplication.fragments.storyfrag;

public class StoryItem {
    private String date, description, time, uid, username;
    private byte[] profileImage;

    public StoryItem() {

    }

    public StoryItem(String date, String description, String time, String uid, String username, byte[] profileImage) {
        this.date = date;
        this.description = description;
        this.time = time;
        this.uid = uid;
        this.username = username;
        this.profileImage = profileImage;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
    }
}
