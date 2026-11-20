package com.example.buddybook;

public class User {
    private String userId;
    private String userName;
    private String userPic;
    private String userPhoneNum;
    private String userEmail;
    private String userPass;
    private String userSecQuest;
    private String userSecAns;

    // Empty Constructor
    public User() {}

    // Full Constructor
    public User(String userId, String userName, String userPic, String userPhoneNum,
                String userEmail, String userPass, String userSecQuest, String userSecAns) {
        this.userId = userId;
        this.userName = userName;
        this.userPic = userPic;
        this.userPhoneNum = userPhoneNum;
        this.userEmail = userEmail;
        this.userPass = userPass;
        this.userSecQuest = userSecQuest;
        this.userSecAns = userSecAns;
    }

    // Getter & Setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPic() { return userPic; }
    public void setUserPic(String userPic) { this.userPic = userPic; }

    public String getUserPhoneNum() { return userPhoneNum; }
    public void setUserPhoneNum(String userPhoneNum) { this.userPhoneNum = userPhoneNum; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserPass() { return userPass; }
    public void setUserPass(String userPass) { this.userPass = userPass; }

    public String getUserSecQuest() { return userSecQuest; }
    public void setUserSecQuest(String userSecQuest) { this.userSecQuest = userSecQuest; }

    public String getUserSecAns() { return userSecAns; }
    public void setUserSecAns(String userSecAns) { this.userSecAns = userSecAns; }
}