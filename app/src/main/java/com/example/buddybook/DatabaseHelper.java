package com.example.buddybook;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private final Context appContext;

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    // Database Details
    private static final String DATABASE_NAME = "BuddyBook.db";
    private static final int DATABASE_VERSION = 5;

    private static final String TABLE_USER = "USER";
    private static final String TABLE_BUDDY = "BUDDY";
    private static final String BUDDY_ID = "BUDDY_ID";
    private static final String BUDDY_USER_ID = "USER_ID";
    private static final String BUDDY_NAME = "BUDDY_NAME";
    private static final String BUDDY_GENDER = "BUDDY_GENDER";
    private static final String BUDDY_EMAIL = "BUDDY_EMAIL";
    private static final String BUDDY_PHONENUM = "BUDDY_PHONENUM";
    private static final String BUDDY_ADDRESS1 = "BUDDY_ADDRESS1";
    private static final String BUDDY_ADDRESS2 = "BUDDY_ADDRESS2";
    private static final String BUDDY_ADDRESS3 = "BUDDY_ADDRESS3";
    private static final String BUDDY_STATE = "BUDDY_STATE";
    private static final String BUDDY_PIC = "BUDDY_PIC";
    private static final String DATE_ADDED = "DATE_ADDED";

    // Create USER Table
    private static final String CREATE_USER_TABLE =
            "CREATE TABLE USER (" +
                    "USER_ID TEXT PRIMARY KEY," +
                    "USER_NAME TEXT NOT NULL," +
                    "USER_PIC TEXT," +
                    "USER_PHONENUM TEXT," +
                    "USER_EMAIL TEXT UNIQUE," +
                    "USER_PASS TEXT NOT NULL," +
                    "USER_SEC_QUEST TEXT," +
                    "USER_SEC_ANS TEXT" +
                    ");";

    // Create USER_GROUP Table
    private static final String CREATE_GROUP_TABLE =
            "CREATE TABLE USER_GROUP (" +
                    "GROUP_ID TEXT PRIMARY KEY," +
                    "USER_ID TEXT NOT NULL," +
                    "GROUP_NAME TEXT NOT NULL," +
                    "DATE_CREATED TEXT," +
                    "FOREIGN KEY(USER_ID) REFERENCES USER(USER_ID)" +
                    ");";

    // Create BUDDY Table
    private static final String CREATE_BUDDY_TABLE =
            "CREATE TABLE BUDDY (" +
                    "BUDDY_ID TEXT PRIMARY KEY," +
                    "USER_ID TEXT NOT NULL," +
                    "BUDDY_NAME TEXT NOT NULL," +
                    "BUDDY_PIC TEXT," +
                    "BUDDY_GENDER TEXT," +
                    "BUDDY_EMAIL TEXT," +
                    "BUDDY_PHONENUM TEXT," +
                    "BUDDY_ADDRESS1 TEXT," +
                    "BUDDY_ADDRESS2 TEXT," +
                    "BUDDY_ADDRESS3 TEXT," +
                    "BUDDY_STATE TEXT," +
                    "DATE_ADDED TEXT," +
                    "FOREIGN KEY(USER_ID) REFERENCES USER(USER_ID)" +
                    ");";

    // Create GROUP_BUDDY Table
    private static final String CREATE_GROUP_BUDDY_TABLE =
            "CREATE TABLE GROUP_BUDDY (" +
                    "GROUP_ID TEXT NOT NULL," +
                    "BUDDY_ID TEXT NOT NULL," +
                    "PRIMARY KEY (GROUP_ID, BUDDY_ID)," +
                    "FOREIGN KEY(GROUP_ID) REFERENCES USER_GROUP(GROUP_ID)," +
                    "FOREIGN KEY(BUDDY_ID) REFERENCES BUDDY(BUDDY_ID)" +
                    ");";

    public DatabaseHelper(Context context) {
        super(
                context.getApplicationContext(),
                DATABASE_NAME,
                null,
                DATABASE_VERSION
        );

        appContext = context.getApplicationContext();
    }


    // ==========================
    // REGISTER USER
    // ==========================
    public boolean registerUser(String name, String email, String phone, String password,
                                String securityQuestion, String securityAnswer, String imagePath) {

        if (name == null || email == null || phone == null || password == null
                || securityQuestion == null || securityAnswer == null) {
            return false;
        }

        String cleanName = name.trim();
        String cleanEmail = email.trim().toLowerCase(Locale.ROOT);
        String cleanPhone = phone.trim();
        String cleanQuestion = securityQuestion.trim();
        String cleanAnswer = securityAnswer.trim();

        if (cleanName.isEmpty() || cleanEmail.isEmpty() || cleanPhone.isEmpty()
                || password.isEmpty() || cleanQuestion.isEmpty() || cleanAnswer.isEmpty()) {
            return false;
        }

        if (isEmailExists(cleanEmail)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String newUserId = generateNextUserId();
        String hashedPassword = PasswordUtils.hashPassword(password);

        values.put("USER_ID", newUserId);
        values.put("USER_NAME", cleanName);
        values.put("USER_EMAIL", cleanEmail);
        values.put("USER_PHONENUM", cleanPhone);
        values.put("USER_PASS", hashedPassword);
        values.put("USER_SEC_QUEST", cleanQuestion);
        values.put("USER_SEC_ANS", cleanAnswer);
        values.put("USER_PIC", imagePath);

        try {
            long result = db.insertOrThrow(TABLE_USER, null, values);
            return result != -1;
        } catch (SQLiteConstraintException e) {
            Log.e("DB_REGISTER", "Registration failed because the email or ID already exists.", e);
            return false;
        } catch (Exception e) {
            Log.e("DB_REGISTER", "Unexpected registration error.", e);
            return false;
        }
    }

    // ==========================
    // CHECK EMAIL EXISTS
    // ==========================
    public boolean isEmailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String cleanEmail = email.trim().toLowerCase(Locale.ROOT);

        String query = "SELECT USER_ID FROM USER " +
                "WHERE LOWER(USER_EMAIL) = ? LIMIT 1";

        try (Cursor cursor = db.rawQuery(query, new String[]{cleanEmail})) {
            return cursor.moveToFirst();
        }
    }

    // ==========================
    // GENERATE USER ID
    // ==========================
    public String generateNextUserId() {
        SQLiteDatabase db = this.getReadableDatabase();
        String nextId = "U001"; // Default

        String query = "SELECT USER_ID FROM USER WHERE USER_ID LIKE 'U%' ORDER BY USER_ID DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String lastId = cursor.getString(0);
                try {
                    int numericPart = Integer.parseInt(lastId.substring(1));
                    numericPart++;
                    nextId = String.format("U%03d", numericPart);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }
        return nextId;
    }

    // ==========================
    // LOG IN USER
    // ==========================
    public User loginUser(String email, String password) {
        if (email == null || password == null
                || email.trim().isEmpty() || password.isEmpty()) {
            return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String cleanEmail = email.trim().toLowerCase(Locale.ROOT);

        String query = "SELECT * FROM USER WHERE LOWER(USER_EMAIL) = ? LIMIT 1";

        try (Cursor cursor = db.rawQuery(query, new String[]{cleanEmail})) {
            if (!cursor.moveToFirst()) {
                return null;
            }

            String storedPassword = cursor.getString(
                    cursor.getColumnIndexOrThrow("USER_PASS")
            );

            if (!PasswordUtils.verifyPassword(password, storedPassword)) {
                return null;
            }

            return new User(
                    cursor.getString(cursor.getColumnIndexOrThrow("USER_ID")),
                    cursor.getString(cursor.getColumnIndexOrThrow("USER_NAME")),
                    cursor.getString(cursor.getColumnIndexOrThrow("USER_PIC")),
                    cursor.getString(cursor.getColumnIndexOrThrow("USER_PHONENUM")),
                    cursor.getString(cursor.getColumnIndexOrThrow("USER_EMAIL")),
                    storedPassword,
                    cursor.getString(cursor.getColumnIndexOrThrow("USER_SEC_QUEST")),
                    cursor.getString(cursor.getColumnIndexOrThrow("USER_SEC_ANS"))
            );
        } catch (Exception e) {
            Log.e("DB_LOGIN", "Login query failed.", e);
            return null;
        }
    }

    // ==========================
    // CREATE BUDDY
    // ==========================
    public String createBuddy(String userId, String name, String gender, String email, String phone,
                              String address1, String address2, String address3,
                              String state, String photoUri) {

        if (userId == null || userId.trim().isEmpty()
                || name == null || name.trim().isEmpty()) {
            return null;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        String buddyId = generateNextBuddyId();

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new java.util.Date());

        ContentValues values = new ContentValues();
        values.put(BUDDY_ID, buddyId);
        values.put(BUDDY_USER_ID, userId.trim());
        values.put(BUDDY_NAME, name.trim());
        values.put(BUDDY_GENDER, gender);
        values.put(BUDDY_EMAIL, email);
        values.put(BUDDY_PHONENUM, phone);
        values.put(BUDDY_ADDRESS1, address1);
        values.put(BUDDY_ADDRESS2, address2);
        values.put(BUDDY_ADDRESS3, address3);
        values.put(BUDDY_STATE, state);
        values.put(BUDDY_PIC, photoUri);
        values.put(DATE_ADDED, currentDate);

        try {
            long result = db.insertOrThrow(TABLE_BUDDY, null, values);

            if (result == -1) {
                Log.e("DB_CREATE_BUDDY", "Failed to insert buddy: " + name);
                return null;
            }

            Log.d("DB_CREATE_BUDDY", "Created buddy with ID: " + buddyId);
            return buddyId;

        } catch (SQLiteConstraintException e) {
            Log.e("DB_CREATE_BUDDY",
                    "Invalid USER_ID or duplicate BUDDY_ID while creating buddy.", e);
            return null;
        } catch (Exception e) {
            Log.e("DB_CREATE_BUDDY", "Unexpected error while creating buddy.", e);
            return null;
        }
    }

    // ==========================
    // FETCH ALL GROUPS LIST
    // ==========================
    @SuppressLint("Range")
    public List<UserGroup> getAllGroups() {
        List<UserGroup> groupList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT GROUP_ID, GROUP_NAME FROM USER_GROUP", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex("GROUP_ID"));
                    String name = cursor.getString(cursor.getColumnIndex("GROUP_NAME"));
                    groupList.add(new UserGroup(id, name));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DB_ERROR", "Error fetching dynamic groups list", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return groupList;
    }

    // ==========================
    // FETCH GROUPS FOR ONE USER
    // ==========================
    public List<UserGroup> getGroupsByUserId(String userId) {
        List<UserGroup> groups = new ArrayList<>();

        if (userId == null || userId.trim().isEmpty()) {
            return groups;
        }

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT GROUP_ID, GROUP_NAME FROM USER_GROUP " +
                        "WHERE USER_ID = ? ORDER BY GROUP_NAME COLLATE NOCASE ASC",
                new String[]{userId.trim()}
        )) {
            while (cursor.moveToNext()) {
                groups.add(new UserGroup(
                        cursor.getString(cursor.getColumnIndexOrThrow("GROUP_ID")),
                        cursor.getString(cursor.getColumnIndexOrThrow("GROUP_NAME"))
                ));
            }
        } catch (Exception e) {
            Log.e("DB_GROUPS", "Unable to load groups for user.", e);
        }

        return groups;
    }

    // ==========================================
    // CHECK WHETHER BUDDY IS ALREADY IN A GROUP
    // ==========================================
    public boolean isBuddyAlreadyInGroup(String groupId, String buddyId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM GROUP_BUDDY " +
                        "WHERE GROUP_ID=? AND BUDDY_ID=? LIMIT 1",
                new String[]{
                        groupId,
                        buddyId
                });

        boolean exists = cursor.moveToFirst();

        cursor.close();

        return exists;
    }

    // ==========================================
    // GET GROUP NAME
    // ==========================================
    public String getGroupName(String groupId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT GROUP_NAME FROM USER_GROUP WHERE GROUP_ID=?",
                new String[]{groupId}
        );

        String name = "";

        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }

        cursor.close();

        return name;
    }


    // ==========================================
    // ADD BUDDY TO GROUP
    // ==========================================
    public boolean addToGroup(String groupId, String buddyId) {

        if (isBuddyAlreadyInGroup(groupId, buddyId)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("GROUP_ID", groupId);
        values.put("BUDDY_ID", buddyId);

        long result = db.insert(
                "GROUP_BUDDY",
                null,
                values
        );

        return result != -1;
    }

    // ==========================
    // GENERATE BUDDY ID
    // ==========================
    public String generateNextBuddyId() {
        SQLiteDatabase db = this.getReadableDatabase();
        String nextId = "B001"; //Default

        String query = "SELECT " + BUDDY_ID + " FROM " + TABLE_BUDDY +
                " WHERE " + BUDDY_ID + " LIKE 'B%'" +
                " ORDER BY " + BUDDY_ID + " DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String lastId = cursor.getString(0);
                try {
                    int numericPart = Integer.parseInt(lastId.substring(1));
                    numericPart++;

                    nextId = String.format("B%03d", numericPart);
                } catch (NumberFormatException e) {
                    Log.e("DB_ERR", "Error parsing numeric part from ID: " + lastId);
                }
            }
            cursor.close();
        }
        return nextId;
    }

    // ==========================
    // FETCH ALL BUDDIES
    // ==========================
    @SuppressLint("Range")
    public List<Buddy> getBuddiesByUserId(String userId) {
        List<Buddy> buddyList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM BUDDY " +
                "WHERE USER_ID = ? " +
                "ORDER BY BUDDY_NAME ASC";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{userId});

        if (cursor.moveToFirst()) {
            do {
                Buddy buddy = new Buddy();

                buddy.setBuddyId(cursor.getString(cursor.getColumnIndex("BUDDY_ID")));
                buddy.setUserId(cursor.getString(cursor.getColumnIndex("USER_ID")));
                buddy.setBuddyName(cursor.getString(cursor.getColumnIndex("BUDDY_NAME")));
                buddy.setBuddyPic(cursor.getString(cursor.getColumnIndex("BUDDY_PIC")));
                buddy.setBuddyGender(cursor.getString(cursor.getColumnIndex("BUDDY_GENDER")));
                buddy.setBuddyEmail(cursor.getString(cursor.getColumnIndex("BUDDY_EMAIL")));
                buddy.setBuddyPhoneNum(cursor.getString(cursor.getColumnIndex("BUDDY_PHONENUM")));
                buddy.setBuddyAddress1(cursor.getString(cursor.getColumnIndex("BUDDY_ADDRESS1")));
                buddy.setBuddyAddress2(cursor.getString(cursor.getColumnIndex("BUDDY_ADDRESS2")));
                buddy.setBuddyAddress3(cursor.getString(cursor.getColumnIndex("BUDDY_ADDRESS3")));
                buddy.setBuddyState(cursor.getString(cursor.getColumnIndex("BUDDY_STATE")));
                buddy.setDateAdded(cursor.getString(cursor.getColumnIndex("DATE_ADDED")));

                buddyList.add(buddy);
            } while (cursor.moveToNext());
        } else {
            Log.d("BUDDY_FETCH_TEST", "No buddies found for this user ID in database.");
        }

        cursor.close();
        return buddyList;
    }

    // ==========================
    // FETCH ONE BUDDY
    // ==========================
    public Buddy getBuddyById(String buddyId) {
        return getBuddyById(buddyId, null);
    }

    public Buddy getBuddyById(String buddyId, String userId) {
        if (buddyId == null || buddyId.trim().isEmpty()) {
            return null;
        }

        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT * FROM BUDDY WHERE BUDDY_ID = ?";
        List<String> args = new ArrayList<>();
        args.add(buddyId.trim());

        if (userId != null && !userId.trim().isEmpty()) {
            sql += " AND USER_ID = ?";
            args.add(userId.trim());
        }

        sql += " LIMIT 1";

        try (Cursor cursor = db.rawQuery(sql, args.toArray(new String[0]))) {
            if (!cursor.moveToFirst()) {
                return null;
            }

            Buddy buddy = new Buddy();
            buddy.setBuddyId(cursor.getString(cursor.getColumnIndexOrThrow("BUDDY_ID")));
            buddy.setUserId(cursor.getString(cursor.getColumnIndexOrThrow("USER_ID")));
            buddy.setBuddyName(cursor.getString(cursor.getColumnIndexOrThrow("BUDDY_NAME")));
            buddy.setBuddyPic(cursor.getString(cursor.getColumnIndexOrThrow("BUDDY_PIC")));
            buddy.setBuddyGender(cursor.getString(cursor.getColumnIndexOrThrow("BUDDY_GENDER")));
            buddy.setBuddyEmail(cursor.getString(cursor.getColumnIndexOrThrow("BUDDY_EMAIL")));
            buddy.setBuddyPhoneNum(cursor.getString(cursor.getColumnIndexOrThrow("BUDDY_PHONENUM")));
            buddy.setBuddyAddress1(cursor.getString(cursor.getColumnIndexOrThrow("BUDDY_ADDRESS1")));
            buddy.setBuddyAddress2(cursor.getString(cursor.getColumnIndexOrThrow("BUDDY_ADDRESS2")));
            buddy.setBuddyAddress3(cursor.getString(cursor.getColumnIndexOrThrow("BUDDY_ADDRESS3")));
            buddy.setBuddyState(cursor.getString(cursor.getColumnIndexOrThrow("BUDDY_STATE")));
            buddy.setDateAdded(cursor.getString(cursor.getColumnIndexOrThrow("DATE_ADDED")));
            return buddy;

        } catch (Exception e) {
            Log.e("DB_READ_BUDDY", "Unable to read buddy.", e);
            return null;
        }
    }

    // ==========================
    // UPDATE BUDDY
    // ==========================
    public boolean updateBuddy(
            String buddyId,
            String userId,
            String name,
            String gender,
            String email,
            String phone,
            String addr1,
            String addr2,
            String addr3,
            String state,
            String photoUri
    ) {
        if (buddyId == null || buddyId.trim().isEmpty()
                || userId == null || userId.trim().isEmpty()
                || name == null || name.trim().isEmpty()) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(BUDDY_NAME, name.trim());
        values.put(BUDDY_GENDER, cleanNullable(gender));
        values.put(BUDDY_EMAIL, cleanNullable(email).toLowerCase(Locale.ROOT));
        values.put(BUDDY_PHONENUM, cleanNullable(phone));
        values.put(BUDDY_ADDRESS1, cleanNullable(addr1));
        values.put(BUDDY_ADDRESS2, cleanNullable(addr2));
        values.put(BUDDY_ADDRESS3, cleanNullable(addr3));
        values.put(BUDDY_STATE, cleanNullable(state));
        values.put(BUDDY_PIC, cleanNullable(photoUri));

        try {
            int rows = getWritableDatabase().update(
                    TABLE_BUDDY,
                    values,
                    BUDDY_ID + " = ? AND " + BUDDY_USER_ID + " = ?",
                    new String[]{buddyId.trim(), userId.trim()}
            );
            return rows > 0;
        } catch (Exception e) {
            Log.e("DB_UPDATE_BUDDY", "Unable to update buddy.", e);
            return false;
        }
    }

    // Backward-compatible overload. Prefer the user-scoped method above.
    public boolean updateBuddy(
            String buddyId,
            String name,
            String gender,
            String email,
            String phone,
            String addr1,
            String addr2,
            String addr3,
            String state,
            String photoUri
    ) {
        Buddy buddy = getBuddyById(buddyId);
        return buddy != null && updateBuddy(
                buddyId,
                buddy.getUserId(),
                name,
                gender,
                email,
                phone,
                addr1,
                addr2,
                addr3,
                state,
                photoUri
        );
    }

    // ==========================
    // EDIT USER UPDATE
    // ==========================
    public boolean updateUser(String userId, String name, String email, String phone, String photoUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("USER_NAME", name);
        values.put("USER_EMAIL", email);
        values.put("USER_PHONENUM", phone);
        values.put("USER_PIC", photoUri);

        int rowsAffected = db.update(TABLE_USER, values, "USER_ID = ?", new String[]{userId});
        return rowsAffected > 0;
    }

    // ==========================
    // DELETE BUDDY
    // ==========================
    public boolean deleteBuddy(String buddyId, String userId) {
        if (buddyId == null || buddyId.trim().isEmpty()
                || userId == null || userId.trim().isEmpty()) {
            return false;
        }

        Buddy buddy = getBuddyById(buddyId, userId);
        if (buddy == null) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(
                    "GROUP_BUDDY",
                    "BUDDY_ID = ?",
                    new String[]{buddyId.trim()}
            );

            int rows = db.delete(
                    TABLE_BUDDY,
                    BUDDY_ID + " = ? AND " + BUDDY_USER_ID + " = ?",
                    new String[]{buddyId.trim(), userId.trim()}
            );

            if (rows <= 0) {
                return false;
            }

            db.setTransactionSuccessful();

            ImageStorageUtils.deleteStoredImage(
                    appContext,
                    buddy.getBuddyPic()
            );

            return true;

        } catch (Exception e) {
            Log.e("DB_DELETE_BUDDY", "Unable to delete buddy.", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // Backward-compatible overload.
    public boolean deleteBuddy(String buddyId) {
        Buddy buddy = getBuddyById(buddyId);
        return buddy != null && deleteBuddy(buddyId, buddy.getUserId());
    }

    // ==========================================
    // REPLACE ALL GROUP LINKS FOR A BUDDY
    // ==========================================
    public boolean replaceBuddyGroups(
            String buddyId,
            String userId,
            List<String> selectedGroupIds
    ) {
        Buddy buddy = getBuddyById(buddyId, userId);
        if (buddy == null) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(
                    "GROUP_BUDDY",
                    "BUDDY_ID = ?",
                    new String[]{buddyId}
            );

            if (selectedGroupIds != null) {
                for (String groupId : selectedGroupIds) {
                    if (groupId == null || groupId.trim().isEmpty()) {
                        continue;
                    }

                    try (Cursor cursor = db.rawQuery(
                            "SELECT 1 FROM USER_GROUP " +
                                    "WHERE GROUP_ID = ? AND USER_ID = ? LIMIT 1",
                            new String[]{groupId.trim(), userId.trim()}
                    )) {
                        if (!cursor.moveToFirst()) {
                            continue;
                        }
                    }

                    ContentValues values = new ContentValues();
                    values.put("GROUP_ID", groupId.trim());
                    values.put("BUDDY_ID", buddyId.trim());

                    long result = db.insertWithOnConflict(
                            "GROUP_BUDDY",
                            null,
                            values,
                            SQLiteDatabase.CONFLICT_IGNORE
                    );

                    if (result == -1) {
                        throw new IllegalStateException("Unable to insert group relationship.");
                    }
                }
            }

            db.setTransactionSuccessful();
            return true;

        } catch (Exception e) {
            Log.e("DB_REPLACE_GROUPS", "Unable to replace buddy groups.", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    private String cleanNullable(String value) {
        return value == null ? "" : value.trim();
    }

    // ==========================
    // VERIFY USER
    // ==========================
    public boolean verifySecurityAnswer(String email, String question, String answer) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean isValid = false;

        try {
            String query = "SELECT * FROM USER WHERE USER_EMAIL = ? AND USER_SEC_QUEST = ? AND USER_SEC_ANS = ?";
            cursor = db.rawQuery(query, new String[]{email, question, answer});

            if (cursor != null && cursor.getCount() > 0) {
                isValid = true;
                Log.d("DB_CHECK", "SUCCESS: Rekod pengguna dijumpai. Jumlah baris: " + cursor.getCount());
            }
        } catch (Exception e) {
            Log.e("DB_ERROR", "Error verifying identity", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return isValid;
    }

    // ==========================
    // GET USER
    // ==========================
    public User getUserProfile(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        String query = "SELECT * FROM USER WHERE USER_ID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{userId});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                user = new User();

                user.setUserId(cursor.getString(cursor.getColumnIndexOrThrow("USER_ID")));
                user.setUserName(cursor.getString(cursor.getColumnIndexOrThrow("USER_NAME")));
                user.setUserPic(cursor.getString(cursor.getColumnIndexOrThrow("USER_PIC")));
                user.setUserPhoneNum(cursor.getString(cursor.getColumnIndexOrThrow("USER_PHONENUM")));
                user.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow("USER_EMAIL")));
                user.setUserPass(cursor.getString(cursor.getColumnIndexOrThrow("USER_PASS")));
                user.setUserSecQuest(cursor.getString(cursor.getColumnIndexOrThrow("USER_SEC_QUEST")));
                user.setUserSecAns(cursor.getString(cursor.getColumnIndexOrThrow("USER_SEC_ANS")));
            }
            cursor.close();
        }
        return user;
    }

    // ==========================================
    // COUNT TOTAL BUDDIES
    // ==========================================
    public int getTotalBuddies(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return 0;
        }

        String query =
                "SELECT COUNT(*) FROM " + TABLE_BUDDY +
                        " WHERE " + BUDDY_USER_ID + " = ?";

        try (Cursor cursor = getReadableDatabase().rawQuery(
                query,
                new String[]{userId.trim()}
        )) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;

        } catch (Exception e) {
            Log.e("DB_TOTAL_BUDDIES", "Unable to count buddies.", e);
            return 0;
        }
    }

    // ==========================================
    // COUNT TOTAL STATES COVERED
    // ==========================================
    public int getTotalStatesCovered(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return 0;
        }

        String query =
                "SELECT COUNT(DISTINCT " + BUDDY_STATE + ") " +
                        "FROM " + TABLE_BUDDY + " " +
                        "WHERE " + BUDDY_USER_ID + " = ? " +
                        "AND " + BUDDY_STATE + " IS NOT NULL " +
                        "AND TRIM(" + BUDDY_STATE + ") != ''";

        try (Cursor cursor = getReadableDatabase().rawQuery(
                query,
                new String[]{userId.trim()}
        )) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;

        } catch (Exception e) {
            Log.e("DB_TOTAL_STATES", "Unable to count buddy states.", e);
            return 0;
        }
    }

    // ==========================================
    // GET RECENT BUDDIES
    // ==========================================
    public List<Buddy> getRecentBuddies(String userId) {
        List<Buddy> buddies = new ArrayList<>();

        if (userId == null || userId.trim().isEmpty()) {
            return buddies;
        }

        String query =
                "SELECT * FROM " + TABLE_BUDDY + " " +
                        "WHERE " + BUDDY_USER_ID + " = ? " +
                        "ORDER BY " + DATE_ADDED + " DESC, " +
                        BUDDY_ID + " DESC " +
                        "LIMIT 5";

        try (Cursor cursor = getReadableDatabase().rawQuery(
                query,
                new String[]{userId.trim()}
        )) {
            while (cursor.moveToNext()) {
                Buddy buddy = new Buddy();

                buddy.setBuddyId(
                        cursor.getString(cursor.getColumnIndexOrThrow(BUDDY_ID))
                );

                buddy.setUserId(
                        cursor.getString(cursor.getColumnIndexOrThrow(BUDDY_USER_ID))
                );

                buddy.setBuddyName(
                        cursor.getString(cursor.getColumnIndexOrThrow(BUDDY_NAME))
                );

                buddy.setBuddyPic(
                        cursor.getString(cursor.getColumnIndexOrThrow(BUDDY_PIC))
                );

                buddy.setBuddyGender(
                        cursor.getString(cursor.getColumnIndexOrThrow(BUDDY_GENDER))
                );

                buddy.setBuddyEmail(
                        cursor.getString(cursor.getColumnIndexOrThrow(BUDDY_EMAIL))
                );

                buddy.setBuddyPhoneNum(
                        cursor.getString(cursor.getColumnIndexOrThrow(BUDDY_PHONENUM))
                );

                buddy.setBuddyAddress1(
                        cursor.getString(cursor.getColumnIndexOrThrow(BUDDY_ADDRESS1))
                );

                buddy.setBuddyAddress2(
                        cursor.getString(cursor.getColumnIndexOrThrow(BUDDY_ADDRESS2))
                );

                buddy.setBuddyAddress3(
                        cursor.getString(cursor.getColumnIndexOrThrow(BUDDY_ADDRESS3))
                );

                buddy.setBuddyState(
                        cursor.getString(cursor.getColumnIndexOrThrow(BUDDY_STATE))
                );

                buddy.setDateAdded(
                        cursor.getString(cursor.getColumnIndexOrThrow(DATE_ADDED))
                );

                buddies.add(buddy);
            }

        } catch (Exception e) {
            Log.e("DB_RECENT_BUDDIES", "Unable to load recent buddies.", e);
        }

        return buddies;
    }

    // ==========================================
    // COUNT NEW BUDDIES THIS MONTH
    // ==========================================
    public int getNewBuddies(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return 0;
        }

        String query =
                "SELECT COUNT(*) FROM " + TABLE_BUDDY + " " +
                        "WHERE " + BUDDY_USER_ID + " = ? " +
                        "AND date(" + DATE_ADDED + ") >= date('now', 'start of month')";

        try (Cursor cursor = getReadableDatabase().rawQuery(
                query,
                new String[]{userId.trim()}
        )) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;

        } catch (Exception e) {
            Log.e("DB_NEW_BUDDIES", "Unable to count new buddies.", e);
            return 0;
        }
    }

    // ==========================================
    // GET ALL GROUPS
    // ==========================================
    public List<Group> getMyGroups(String currentUserId) {
        List<Group> groupList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT ug.*, COUNT(gb.BUDDY_ID) as MEMBER_COUNT " +
                "FROM USER_GROUP ug " +
                "LEFT JOIN GROUP_BUDDY gb ON ug.GROUP_ID = gb.GROUP_ID " +
                "WHERE ug.USER_ID = ? " +
                "GROUP BY ug.GROUP_ID " +
                "ORDER BY ug.DATE_CREATED DESC";

        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, new String[]{currentUserId});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Group group = new Group();

                    group.setId(cursor.getString(cursor.getColumnIndexOrThrow("GROUP_ID")));
                    group.setUserId(cursor.getString(cursor.getColumnIndexOrThrow("USER_ID")));
                    group.setGroupName(cursor.getString(cursor.getColumnIndexOrThrow("GROUP_NAME")));
                    group.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("DATE_CREATED")));
                    group.setMemberCount(cursor.getInt(cursor.getColumnIndexOrThrow("MEMBER_COUNT")));

                    groupList.add(group);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return groupList;
    }

    // ==========================================
    // CREATE GROUP
    // ==========================================
    public boolean createGroup(String userId, String groupName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String newGroupId = generateNextGroupId(db);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String currentDate = sdf.format(new java.util.Date());

        values.put("GROUP_ID", newGroupId);
        values.put("USER_ID", userId);
        values.put("GROUP_NAME", groupName);
        values.put("DATE_CREATED", currentDate);

        long result = db.insert("USER_GROUP", null, values);

        return result != -1;
    }

    // ==========================================
    // GENERATE GROUP ID
    // ==========================================
    private String generateNextGroupId(SQLiteDatabase db) {
        String query = "SELECT GROUP_ID FROM USER_GROUP ORDER BY GROUP_ID DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);

        String nextId = "G001";

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String lastId = cursor.getString(0);
                try {
                    int numericPart = Integer.parseInt(lastId.substring(1));
                    numericPart++;
                    nextId = String.format("G%03d", numericPart);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }

        return nextId;
    }

    // ==========================================
    // GET BUDDIES BY GROUP
    // ==========================================
    @SuppressLint("Range")
    public List<Buddy> getBuddiesByGroupId(String groupId) {
        List<Buddy> buddyList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT b.* FROM BUDDY b " +
                "INNER JOIN GROUP_BUDDY gb ON b.BUDDY_ID = gb.BUDDY_ID " +
                "WHERE gb.GROUP_ID = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{groupId});

        if (cursor.moveToFirst()) {
            do {
                Buddy buddy = new Buddy();

                buddy.setBuddyId(cursor.getString(cursor.getColumnIndex("BUDDY_ID")));
                buddy.setUserId(cursor.getString(cursor.getColumnIndex("USER_ID")));
                buddy.setBuddyName(cursor.getString(cursor.getColumnIndex("BUDDY_NAME")));
                buddy.setBuddyPic(cursor.getString(cursor.getColumnIndex("BUDDY_PIC")));
                buddy.setBuddyGender(cursor.getString(cursor.getColumnIndex("BUDDY_GENDER")));
                buddy.setBuddyEmail(cursor.getString(cursor.getColumnIndex("BUDDY_EMAIL")));
                buddy.setBuddyPhoneNum(cursor.getString(cursor.getColumnIndex("BUDDY_PHONENUM")));
                buddy.setBuddyAddress1(cursor.getString(cursor.getColumnIndex("BUDDY_ADDRESS1")));
                buddy.setBuddyAddress2(cursor.getString(cursor.getColumnIndex("BUDDY_ADDRESS2")));
                buddy.setBuddyAddress3(cursor.getString(cursor.getColumnIndex("BUDDY_ADDRESS3")));
                buddy.setBuddyState(cursor.getString(cursor.getColumnIndex("BUDDY_STATE")));
                buddy.setDateAdded(cursor.getString(cursor.getColumnIndex("DATE_ADDED")));

                buddyList.add(buddy);
            } while (cursor.moveToNext());
        } else {
            Log.d("BUDDY_FETCH_TEST", "No buddies found for group: " + groupId);
        }

        cursor.close();
        return buddyList;
    }

    // ==========================================
    // REMOVE BUDDY FROM GROUP
    // ==========================================
    public boolean deleteBuddyFromGroup(String groupId, String buddyId) {

        if (groupId == null || buddyId == null
                || groupId.trim().isEmpty()
                || buddyId.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        try {

            int rowsDeleted = db.delete(
                    "GROUP_BUDDY",
                    "GROUP_ID = ? AND BUDDY_ID = ?",
                    new String[]{
                            groupId,
                            buddyId
                    }
            );

            if (rowsDeleted > 0) {

                Log.d(
                        "DB_REMOVE_MEMBER",
                        "Buddy " + buddyId +
                                " removed from group " + groupId
                );

                return true;
            }

            return false;

        } catch (Exception e) {

            Log.e(
                    "DB_REMOVE_MEMBER_ERROR",
                    "Failed removing buddy from group.",
                    e
            );

            return false;
        }
    }

    // ==========================================
    // DELETE GROUP AND ALL MEMBER LINKS
    // ==========================================
    public boolean deleteGroup(String groupId) {

        if (groupId == null || groupId.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();

        try {

            // Delete all GROUP_BUDDY records related to this group
            db.delete(
                    "GROUP_BUDDY",
                    "GROUP_ID = ?",
                    new String[]{groupId}
            );

            // Delete the group itself
            int deletedGroupRows = db.delete(
                    "USER_GROUP",
                    "GROUP_ID = ?",
                    new String[]{groupId}
            );

            if (deletedGroupRows <= 0) {
                return false;
            }

            db.setTransactionSuccessful();

            Log.d(
                    "DB_DELETE_GROUP",
                    "Group " + groupId +
                            " and all related GROUP_BUDDY records deleted"
            );

            return true;

        } catch (Exception e) {

            Log.e(
                    "DB_DELETE_GROUP_ERROR",
                    "Failed to delete group",
                    e
            );

            return false;

        } finally {

            db.endTransaction();
        }
    }

    // ==========================================
    // UPDATE GROUP DETAILS
    // ==========================================
    public boolean updateGroupName(String groupId, String newGroupName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("GROUP_NAME", newGroupName);

        int rowsAffected = db.update("USER_GROUP", values, "GROUP_ID = ?", new String[]{groupId});
        return rowsAffected > 0;
    }

    // ==========================================
    // CHANGE PASSWORD BY ID
    // ==========================================
    public boolean changePassword(String userId, String newPassword) {
        if (userId == null || newPassword == null
                || userId.trim().isEmpty() || newPassword.isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("USER_PASS", PasswordUtils.hashPassword(newPassword));

        int result = db.update(
                TABLE_USER,
                values,
                "USER_ID = ?",
                new String[]{userId}
        );

        return result > 0;
    }

    // ==========================
    // UPDATE PASSWORD BY EMAIL
    // ==========================
    public boolean updatePassword(String email, String newPassword) {
        if (email == null || newPassword == null
                || email.trim().isEmpty() || newPassword.isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("USER_PASS", PasswordUtils.hashPassword(newPassword));

        int rows = db.update(
                TABLE_USER,
                values,
                "LOWER(USER_EMAIL) = ?",
                new String[]{email.trim().toLowerCase(Locale.ROOT)}
        );

        return rows > 0;
    }

    // ==========================
    // GET GENDER COUNT BY USER ID
    // ==========================
    public int getGenderCountForUser(
            String userId,
            String gender
    ) {
        if (userId == null || userId.trim().isEmpty()
                || gender == null || gender.trim().isEmpty()) {
            return 0;
        }

        String query =
                "SELECT COUNT(*) FROM " + TABLE_BUDDY + " " +
                        "WHERE " + BUDDY_USER_ID + " = ? " +
                        "AND LOWER(" + BUDDY_GENDER + ") = LOWER(?)";

        try (Cursor cursor = getReadableDatabase().rawQuery(
                query,
                new String[]{userId.trim(), gender.trim()}
        )) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;

        } catch (Exception e) {
            Log.e("DB_GENDER_COUNT", "Unable to count buddy genders.", e);
            return 0;
        }
    }

    // ==========================
    // COUNT STATES STATISTICS
    // ==========================
    public List<StateStat> getStateCountsForUser(String userId) {
        List<StateStat> stats = new ArrayList<>();

        if (userId == null || userId.trim().isEmpty()) {
            return stats;
        }

        String query =
                "SELECT " + BUDDY_STATE + ", COUNT(*) AS TOTAL " +
                        "FROM " + TABLE_BUDDY + " " +
                        "WHERE " + BUDDY_USER_ID + " = ? " +
                        "AND " + BUDDY_STATE + " IS NOT NULL " +
                        "AND TRIM(" + BUDDY_STATE + ") != '' " +
                        "GROUP BY " + BUDDY_STATE + " " +
                        "ORDER BY TOTAL DESC, " + BUDDY_STATE + " COLLATE NOCASE ASC " +
                        "LIMIT 5";

        try (Cursor cursor = getReadableDatabase().rawQuery(
                query,
                new String[]{userId.trim()}
        )) {
            while (cursor.moveToNext()) {
                stats.add(
                        new StateStat(
                                cursor.getString(0),
                                cursor.getInt(1)
                        )
                );
            }

        } catch (Exception e) {
            Log.e("DB_STATE_STATS", "Unable to load state statistics.", e);
        }

        return stats;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_GROUP_TABLE);
        db.execSQL(CREATE_BUDDY_TABLE);
        db.execSQL(CREATE_GROUP_BUDDY_TABLE);

        // Dummy USER
        ContentValues dummyUser = new ContentValues();
        dummyUser.put("USER_ID", "U001");
        dummyUser.put("USER_NAME", "Mia Lyana");
        dummyUser.put("USER_PIC", "profile.jpg");
        dummyUser.put("USER_PHONENUM", "0123456789");
        dummyUser.put("USER_EMAIL", "mia@gmail.com");
        dummyUser.put("USER_PASS", PasswordUtils.hashPassword("@Ml2018@"));
        dummyUser.put("USER_SEC_QUEST", "What is your favourite color?");
        dummyUser.put("USER_SEC_ANS", "Blue");
        db.insert(TABLE_USER, null, dummyUser);

        // Dummy GROUPS
        db.execSQL("INSERT INTO USER_GROUP VALUES(" +
                "'G001'," +
                "'U001'," +
                "'Family'," +
                "'2026-07-16');");

        db.execSQL("INSERT INTO USER_GROUP VALUES(" +
                "'G002'," +
                "'U001'," +
                "'Friends'," +
                "'2026-07-16');");

        db.execSQL("INSERT INTO USER_GROUP VALUES(" +
                "'G003'," +
                "'U001'," +
                "'Work'," +
                "'2026-07-16');");

        // Dummy BUDDIES
        db.execSQL("INSERT INTO BUDDY VALUES(" +
                "'B001'," +
                "'U001'," +
                "'Alice Tan'," +
                "'alice.jpg'," +
                "'Female'," +
                "'alice@gmail.com'," +
                "'0111111111'," +
                "'12 Jalan Mawar'," +
                "'Taman Indah'," +
                "'Seremban'," +
                "'Negeri Sembilan'," +
                "'2026-07-16');");

        db.execSQL("INSERT INTO BUDDY VALUES(" +
                "'B002'," +
                "'U001'," +
                "'Michael Lim'," +
                "'michael.jpg'," +
                "'Male'," +
                "'michael@gmail.com'," +
                "'0122222222'," +
                "'25 Jalan Melati'," +
                "'Taman Jaya'," +
                "'Kuala Lumpur'," +
                "'Wilayah Persekutuan'," +
                "'2026-07-16');");

        db.execSQL("INSERT INTO BUDDY VALUES(" +
                "'B003'," +
                "'U001'," +
                "'Sarah Lee'," +
                "'sarah.jpg'," +
                "'Female'," +
                "'sarah@gmail.com'," +
                "'0133333333'," +
                "'88 Jalan Anggerik'," +
                "'Bandar Baru'," +
                "'Shah Alam'," +
                "'Selangor'," +
                "'2026-07-16');");

        db.execSQL("INSERT INTO BUDDY VALUES(" +
                "'B004'," +
                "'U001'," +
                "'Daniel Wong'," +
                "'daniel.jpg'," +
                "'Male'," +
                "'daniel@gmail.com'," +
                "'0144444444'," +
                "'17 Jalan Dahlia'," +
                "'Taman Sri'," +
                "'Ipoh'," +
                "'Perak'," +
                "'2026-07-16');");

        db.execSQL("INSERT INTO BUDDY VALUES(" +
                "'B005'," +
                "'U001'," +
                "'Emily Chan'," +
                "'emily.jpg'," +
                "'Female'," +
                "'emily@gmail.com'," +
                "'0155555555'," +
                "'5 Jalan Kenanga'," +
                "'Taman Sentosa'," +
                "'Johor Bahru'," +
                "'Johor'," +
                "'2026-07-16');");

        db.execSQL("INSERT INTO BUDDY VALUES(" +
                "'B006'," +
                "'U001'," +
                "'Ryan Ng'," +
                "'ryan.jpg'," +
                "'Male'," +
                "'ryan@gmail.com'," +
                "'0166666666'," +
                "'66 Jalan Cempaka'," +
                "'Bandar Utama'," +
                "'Petaling Jaya'," +
                "'Selangor'," +
                "'2026-07-16');");

        // Family
        db.execSQL("INSERT INTO GROUP_BUDDY VALUES('G001','B001');");
        db.execSQL("INSERT INTO GROUP_BUDDY VALUES('G001','B005');");

        // Friends
        db.execSQL("INSERT INTO GROUP_BUDDY VALUES('G002','B002');");
        db.execSQL("INSERT INTO GROUP_BUDDY VALUES('G002','B003');");
        db.execSQL("INSERT INTO GROUP_BUDDY VALUES('G002','B006');");

        // Work
        db.execSQL("INSERT INTO GROUP_BUDDY VALUES('G003','B003');");
        db.execSQL("INSERT INTO GROUP_BUDDY VALUES('G003','B004');");
        db.execSQL("INSERT INTO GROUP_BUDDY VALUES('G003','B006');");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS GROUP_BUDDY");
        db.execSQL("DROP TABLE IF EXISTS BUDDY");
        db.execSQL("DROP TABLE IF EXISTS USER_GROUP");
        db.execSQL("DROP TABLE IF EXISTS USER");
        onCreate(db);
    }
}