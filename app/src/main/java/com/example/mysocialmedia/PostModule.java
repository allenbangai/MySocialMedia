package com.example.mysocialmedia;

public class PostModule {
    public String uid, time, date, description, fullName, postImage, profileImage;

    public PostModule(){

    }

    public PostModule(String uid, String time, String date, String description, String fullName, String postImage, String profileImage) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.description = description;
        this.fullName = fullName;
        this.postImage = postImage;
        this.profileImage = profileImage;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getProfileImage() {
        return profileImage;
    }
}
