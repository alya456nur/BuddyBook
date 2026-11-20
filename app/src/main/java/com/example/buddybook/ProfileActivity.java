package com.example.buddybook;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends BaseActivity {

    private View btnLogout;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private ImageView imgUserProfile;

    private View navHome;
    private View navBuddies;
    private View navReports;
    private View navProfile;
    private View navAdd;

    private View optionMyProfile;
    private View optionEditAccount;
    private View optionChangePassword;
    private View optionAboutApp;
    private DatabaseHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = DatabaseHelper.getInstance(getApplicationContext());

        connectViews();
        setClickListeners();
        loadUserProfile();

    }

    private void connectViews() {

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        imgUserProfile = findViewById(R.id.imgUserProfile);
        optionMyProfile = findViewById(R.id.optionMyProfile);
        optionEditAccount = findViewById(R.id.optionEditAccount);
        optionChangePassword = findViewById(R.id.optionChangePassword);
        optionAboutApp = findViewById(R.id.optionAboutApp);
        btnLogout = findViewById(R.id.btnLogout);

        navHome = findViewById(R.id.navHome);
        navBuddies = findViewById(R.id.navBuddies);
        navReports = findViewById(R.id.navReports);
        navProfile = findViewById(R.id.navProfile);
        navAdd = findViewById(R.id.navAdd);
    }

    private void setClickListeners() {

        optionMyProfile.setOnClickListener(v ->
                openMyProfile(v)
        );

        optionEditAccount.setOnClickListener(v ->
                openEditAccount(v)
        );

        optionChangePassword.setOnClickListener(v -> {

            animateOnly(v);
            showChangePasswordDialog();
        });

        optionAboutApp.setOnClickListener(v -> {
            Toast.makeText(this, "BuddyBook v1.0.0", Toast.LENGTH_SHORT).show();
        });


        btnLogout.setOnClickListener(v -> {

            animateOnly(v);
            showLogoutDialog();
        });



        navHome.setOnClickListener(v ->
                animateAndOpenPage(
                        v,
                        HomeActivity.class
                )
        );

        navBuddies.setOnClickListener(v ->
                animateAndOpenPage(
                        v,
                        BuddiesActivity.class
                )
        );

        navReports.setOnClickListener(v ->
                animateAndOpenPage(
                        v,
                        ReportsActivity.class
                )
        );

        navProfile.setOnClickListener(v ->
                animateOnly(v)
        );

        navAdd.setOnClickListener(v ->
                animateAndOpenPage(
                        v,
                        AddBuddyActivity.class
                )
        );
    }

    private void loadUserProfile() {
        if (currentUserId != null) {
            User user = dbHelper.getUserProfile(currentUserId);

            if (user != null) {
                tvProfileName.setText(user.getUserName());
                tvProfileEmail.setText(user.getUserEmail());

                ImageStorageUtils.loadCircularIntoImageView(
                        this,
                        imgUserProfile,
                        user.getUserPic(),
                        R.drawable.ic_profile_person
                );
            } else {
                tvProfileName.setText("User Profile");
                tvProfileEmail.setText("guest@buddybook.com");
                imgUserProfile.setImageResource(
                        R.drawable.ic_profile_person
                );
            }
        }
    }

    private void openMyProfile(View view) {
        view.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(80)
                .withEndAction(() -> {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(80).start();

                    User currentUser = dbHelper.getUserProfile(currentUserId);

                    Intent intent = new Intent(ProfileActivity.this, BuddyDetailsActivity.class);

                    if (currentUser != null) {
                        intent.putExtra("buddy_id", currentUser.getUserId());
                        intent.putExtra("buddy_name", currentUser.getUserName());
                        intent.putExtra("buddy_email", currentUser.getUserEmail());
                        intent.putExtra("buddy_phone", currentUser.getUserPhoneNum());
                        intent.putExtra("buddy_photo", currentUser.getUserPic());
                    } else {
                        intent.putExtra("buddy_id", currentUserId);
                        intent.putExtra("buddy_name", tvProfileName.getText().toString());
                        intent.putExtra("buddy_email", tvProfileEmail.getText().toString());
                        intent.putExtra("buddy_phone", "012-3456789");
                        intent.putExtra("buddy_photo", "");
                    }

                    intent.putExtra("buddy_state", "");
                    intent.putExtra("buddy_address", "");
                    intent.putExtra("is_owner_profile", true);

                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                })
                .start();
    }

    private void openEditAccount(View view) {

        view.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(80)
                .withEndAction(() -> {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(80).start();

                    User currentUser = dbHelper.getUserProfile(currentUserId);

                    Intent intent = new Intent(ProfileActivity.this, EditBuddyActivity.class);

                    if (currentUser != null) {
                        intent.putExtra("buddy_id", currentUser.getUserId());
                        intent.putExtra("buddy_name", currentUser.getUserName());
                        intent.putExtra("buddy_email", currentUser.getUserEmail());
                        intent.putExtra("buddy_phone", currentUser.getUserPhoneNum());
                        intent.putExtra("buddy_photo", currentUser.getUserPic());
                    } else {
                        intent.putExtra("buddy_id", currentUserId);
                        intent.putExtra("buddy_name", tvProfileName.getText().toString());
                        intent.putExtra("buddy_email", tvProfileEmail.getText().toString());
                        intent.putExtra("buddy_phone", "012-3456789");
                        intent.putExtra("buddy_photo", "");
                    }

                    intent.putExtra("buddy_state", "");
                    intent.putExtra("buddy_address", "");
                    intent.putExtra("is_owner_profile", true);

                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                })
                .start();
    }

    private void showChangePasswordDialog() {

        Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(
                Window.FEATURE_NO_TITLE
        );

        dialog.setContentView(
                R.layout.dialog_change_password
        );

        dialog.setCancelable(true);

        Window window = dialog.getWindow();

        if (window != null) {

            window.setBackgroundDrawable(
                    new ColorDrawable(
                            Color.TRANSPARENT
                    )
            );
        }

        EditText etNewPassword =
                dialog.findViewById(
                        R.id.etNewPassword
                );

        EditText etConfirmPassword =
                dialog.findViewById(
                        R.id.etConfirmPassword
                );

        TextView btnCancelPassword =
                dialog.findViewById(
                        R.id.btnCancelPassword
                );

        TextView btnSavePassword =
                dialog.findViewById(
                        R.id.btnSavePassword
                );

        btnCancelPassword.setOnClickListener(v -> {

            animateOnly(v);
            dialog.dismiss();
        });

        btnSavePassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (newPassword.isEmpty()) {
                etNewPassword.setError("Enter a new password");
                etNewPassword.requestFocus();
            } else if (!isValidPassword(newPassword)) {
                etNewPassword.setError(
                        "Password must contain at least:\n" +
                                "• 8 characters\n" +
                                "• 1 uppercase letter\n" +
                                "• 1 lowercase letter\n" +
                                "• 1 number\n" +
                                "• 1 special character"
                );
                etNewPassword.requestFocus();
            } else if (confirmPassword.isEmpty()) {
                etConfirmPassword.setError("Confirm your password");
                etConfirmPassword.requestFocus();
            } else if (!newPassword.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
            } else {
                animateOnly(v);
                String hashedPassword;

                try {
                    hashedPassword = PasswordUtils.hashPassword(newPassword);
                } catch (Exception exception) {
                    Toast.makeText(
                            ProfileActivity.this,
                            "Unable to secure the new password.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                boolean success =
                        dbHelper.changePassword(
                                currentUserId,
                                hashedPassword
                        );

                if (success) {
                    Toast.makeText(ProfileActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();

        if (window != null) {

            WindowManager.LayoutParams params =
                    window.getAttributes();

            params.width = (int) (
                    getResources()
                            .getDisplayMetrics()
                            .widthPixels * 0.90
            );

            window.setAttributes(params);
        }
    }

    private void showLogoutDialog() {

        Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(
                Window.FEATURE_NO_TITLE
        );

        dialog.setContentView(
                R.layout.dialog_logout
        );

        dialog.setCancelable(true);

        Window window = dialog.getWindow();

        if (window != null) {

            window.setBackgroundDrawable(
                    new ColorDrawable(
                            Color.TRANSPARENT
                    )
            );
        }

        TextView btnCancelLogout =
                dialog.findViewById(
                        R.id.btnCancelLogout
                );

        TextView btnConfirmLogout =
                dialog.findViewById(
                        R.id.btnConfirmLogout
                );

        btnCancelLogout.setOnClickListener(v -> {

            animateOnly(v);
            dialog.dismiss();
        });

        btnConfirmLogout.setOnClickListener(v -> {

            v.animate()
                    .scaleX(0.94f)
                    .scaleY(0.94f)
                    .setDuration(80)
                    .withEndAction(() -> {

                        dialog.dismiss();

                        logoutUser();

                        overridePendingTransition(
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                        );
                    })
                    .start();
        });

        dialog.show();

        if (window != null) {

            WindowManager.LayoutParams params =
                    window.getAttributes();

            params.width = (int) (
                    getResources()
                            .getDisplayMetrics()
                            .widthPixels * 0.90
            );

            window.setAttributes(params);
        }
    }

    private void animateOnly(View view) {

        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(80)
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(80)
                                .start()
                )
                .start();
    }

    private void animateAndOpenPage(
            View view,
            Class<?> destination
    ) {

        view.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(80)
                .withEndAction(() -> {

                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(80)
                            .start();

                    Intent intent = new Intent(
                            ProfileActivity.this,
                            destination
                    );

                    startActivity(intent);

                    overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    );
                })
                .start();
    }

    // Min 8 letters, with 1 capital letter and 2 numbers
    private boolean isValidPassword(String password) {
        if (password.length() < 8) return false;

        String passwordPattern =
                "^(?=.*[A-Z])(?=.*\\d.*\\d).{8,}$";

        return password.matches(passwordPattern);
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }
}