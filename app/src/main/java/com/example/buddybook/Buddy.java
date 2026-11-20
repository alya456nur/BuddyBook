package com.example.buddybook;

public class Buddy {

    private String buddyId;
    private String userId;
    private String buddyName;
    private String buddyPic;
    private String buddyGender;
    private String buddyEmail;
    private String buddyPhoneNum;
    private String buddyAddress1;
    private String buddyAddress2;
    private String buddyAddress3;
    private String buddyState;
    private String dateAdded;

    public Buddy() {
    }

    public Buddy(
            String buddyId,
            String userId,
            String buddyName,
            String buddyPic,
            String buddyGender,
            String buddyEmail,
            String buddyPhoneNum,
            String buddyAddress1,
            String buddyAddress2,
            String buddyAddress3,
            String buddyState,
            String dateAdded
    ) {
        this.buddyId = buddyId;
        this.userId = userId;
        this.buddyName = buddyName;
        this.buddyPic = buddyPic;
        this.buddyGender = buddyGender;
        this.buddyEmail = buddyEmail;
        this.buddyPhoneNum = buddyPhoneNum;
        this.buddyAddress1 = buddyAddress1;
        this.buddyAddress2 = buddyAddress2;
        this.buddyAddress3 = buddyAddress3;
        this.buddyState = buddyState;
        this.dateAdded = dateAdded;
    }

    public String getBuddyId() {
        return buddyId;
    }

    public void setBuddyId(String buddyId) {
        this.buddyId = buddyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBuddyName() {
        return buddyName;
    }

    public void setBuddyName(String buddyName) {
        this.buddyName = buddyName;
    }

    public String getBuddyPic() {
        return buddyPic;
    }

    public void setBuddyPic(String buddyPic) {
        this.buddyPic = buddyPic;
    }

    public String getBuddyGender() {
        return buddyGender;
    }

    public void setBuddyGender(String buddyGender) {
        this.buddyGender = buddyGender;
    }

    public String getBuddyEmail() {
        return buddyEmail;
    }

    public void setBuddyEmail(String buddyEmail) {
        this.buddyEmail = buddyEmail;
    }

    public String getBuddyPhoneNum() {
        return buddyPhoneNum;
    }

    public void setBuddyPhoneNum(String buddyPhoneNum) {
        this.buddyPhoneNum = buddyPhoneNum;
    }

    public String getBuddyAddress1() {
        return buddyAddress1;
    }

    public void setBuddyAddress1(String buddyAddress1) {
        this.buddyAddress1 = buddyAddress1;
    }

    public String getBuddyAddress2() {
        return buddyAddress2;
    }

    public void setBuddyAddress2(String buddyAddress2) {
        this.buddyAddress2 = buddyAddress2;
    }

    public String getBuddyAddress3() {
        return buddyAddress3;
    }

    public void setBuddyAddress3(String buddyAddress3) {
        this.buddyAddress3 = buddyAddress3;
    }

    public String getBuddyState() {
        return buddyState;
    }

    public void setBuddyState(String buddyState) {
        this.buddyState = buddyState;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }
}