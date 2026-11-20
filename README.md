# BuddyBook – Personal Contact Management App

BuddyBook is a Java-based Android application designed to help users organise and manage their personal contacts in one place. Users can save buddy information, organise contacts into groups, manage their profiles, and view statistics about their contact collection.

## About the Project

BuddyBook provides a simple way to manage personal connections through a dedicated mobile application.

Instead of keeping contact information in an unorganised list, users can create individual buddy profiles, categorise contacts into groups, and track their contact collection through a statistics dashboard.

The application uses SQLite for local data storage.

## Features

### User Authentication
- User registration
- Login and logout
- Remember login information
- Password visibility toggle
- Forgot password functionality
- Security question verification
- Password hashing

### Buddy Management
- Add new buddies
- View buddy profiles
- Edit buddy information
- Delete buddy records
- Store buddy profile pictures
- Save contact details such as name, gender, email, and phone number
- Store address information and Malaysian states
- Search and filter buddies

### Group Management
- Create custom contact groups
- View personal groups
- Add buddies to groups
- View group members
- Edit group names
- Remove buddies from groups
- Delete groups

### Dashboard
- View total number of buddies
- View total states covered
- Display recently added buddies
- Quick navigation to application features

### Reports and Statistics
- View gender distribution
- View contact distribution by Malaysian state
- Display contact statistics

### Profile Management
- View user profile
- Edit account information
- Change password
- Update profile picture
- Logout functionality

## Technologies Used

**Programming Language**
- Java

**Android Development**
- Android SDK
- Android Studio
- XML Layouts
- Material Components
- AndroidX AppCompat
- AndroidX Activity
- ConstraintLayout

**Database**
- SQLite
- SQLiteOpenHelper

**Additional Implementation**
- PBKDF2 password hashing
- Android internal storage for profile pictures
- Android SharedPreferences for remembered login information

## Project Structure

```text
BuddyBook/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/buddybook/
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── HomeActivity.java
│   │   │   │   ├── RegisterActivity.java
│   │   │   │   ├── ForgotPasswordActivity.java
│   │   │   │   ├── BuddiesActivity.java
│   │   │   │   ├── AddBuddyActivity.java
│   │   │   │   ├── EditBuddyActivity.java
│   │   │   │   ├── BuddyDetailsActivity.java
│   │   │   │   ├── MyGroupsActivity.java
│   │   │   │   ├── GroupDetailsActivity.java
│   │   │   │   ├── ReportsActivity.java
│   │   │   │   ├── ProfileActivity.java
│   │   │   │   ├── DatabaseHelper.java
│   │   │   │   ├── PasswordUtils.java
│   │   │   │   └── ImageStorageUtils.java
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   ├── layout/
│   │   │   │   ├── anim/
│   │   │   │   ├── mipmap/
│   │   │   │   └── values/
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── androidTest/
│   │
│   ├── build.gradle.kts
│   └── proguard-rules.pro
│
├── gradle/
│   ├── wrapper/
│   └── libs.versions.toml
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── .gitignore
```

## Getting Started

### Prerequisites

Make sure you have installed:

- Android Studio
- Android SDK
- JDK 11 or compatible JDK for the configured Android Gradle Plugin
- Android Emulator or a physical Android device

### 1. Clone the Repository

```bash
git clone https://github.com/alya456nur/BuddyBook.git
```

Navigate into the project directory:

```bash
cd buddybook
```

### 2. Open the Project

1. Launch Android Studio.
2. Select **Open**.
3. Navigate to the cloned BuddyBook project folder.
4. Open the project.
5. Allow Gradle to sync and download the required dependencies.

### 3. Run the Application

1. Connect an Android device or start an Android Emulator.
2. Enable USB debugging if using a physical device.
3. Select the target device in Android Studio.
4. Click **Run** to build and launch BuddyBook.

## Download

Download the latest BuddyBook APK:

[Download BuddyBook v1.0.0](https://github.com/alya456nur/BuddyBook/releases/tag/v1.0.0)

Install the APK on your Android device to try the application.

## Database

BuddyBook uses SQLite to store application data locally on the Android device.

The database is managed through `DatabaseHelper.java` and contains the following main tables:

- USER – stores user account and profile information.
- BUDDY – stores personal contact information.
- USER_GROUP – stores custom contact groups.
- GROUP_BUDDY – manages the relationship between buddies and groups.

## Security and Data Storage

- Passwords are hashed using PBKDF2 with a randomly generated salt.
- User and buddy records are stored in a local SQLite database.
- Selected profile pictures are copied into the application's internal storage.
- Remembered login information is managed through Android SharedPreferences.

## Developer

Developed as an academic Android application project.
