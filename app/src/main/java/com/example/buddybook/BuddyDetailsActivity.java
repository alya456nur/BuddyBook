package com.example.buddybook;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;

public class BuddyDetailsActivity extends BaseActivity {

    private TextView tvBuddyName;
    private TextView tvBuddyStateHeader;
    private TextView tvBuddyEmail;
    private TextView tvBuddyPhone;
    private TextView tvBuddyGender;
    private TextView tvBuddyStateValue;
    private TextView tvBuddyAddress;
    private ImageView imgBuddyProfile;

    private View genderRow;
    private View genderDivider;
    private View addressRow;
    private View addressDivider;
    private View stateRow;

    private TextView btnEditBuddy;
    private View btnBackBuddyDetails;
    private View btnDeleteBuddy;
    private View btnAddBuddyToGroup;

    private String buddyId;
    private boolean isOwnerProfile;
    private Buddy currentBuddy;
    private DatabaseHelper dbHelper;

    private final ActivityResultLauncher<Intent> editBuddyLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            loadProfileFromDatabase();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buddy_details);

        dbHelper = DatabaseHelper.getInstance(getApplicationContext());

        connectViews();

        buddyId = getIntent().getStringExtra("buddy_id");
        isOwnerProfile = getIntent().getBooleanExtra("is_owner_profile", false);

        if (buddyId == null || buddyId.trim().isEmpty()) {
            Toast.makeText(this, "Unable to load the selected profile.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setClickListeners();
        loadProfileFromDatabase();
    }

    private void connectViews() {
        tvBuddyName = findViewById(R.id.tvBuddyDetailsName);
        tvBuddyStateHeader = findViewById(R.id.tvBuddyDetailsState);
        tvBuddyEmail = findViewById(R.id.tvBuddyEmail);
        tvBuddyPhone = findViewById(R.id.tvBuddyPhone);
        tvBuddyGender = findViewById(R.id.tvBuddyGender);
        tvBuddyStateValue = findViewById(R.id.tvBuddyStateValue);
        tvBuddyAddress = findViewById(R.id.tvBuddyAddress);
        imgBuddyProfile = findViewById(R.id.imgBuddyProfile);

        genderRow = findViewById(R.id.genderRow);
        genderDivider = findViewById(R.id.genderDivider);
        addressRow = findViewById(R.id.addressRow);
        addressDivider = findViewById(R.id.addressDivider);
        stateRow = findViewById(R.id.stateRow);

        btnBackBuddyDetails = findViewById(R.id.btnBackBuddyDetails);
        btnAddBuddyToGroup = findViewById(R.id.btnAddBuddyToGroup);
        btnDeleteBuddy = findViewById(R.id.btnDeleteBuddy);
        btnEditBuddy = findViewById(R.id.btnEditBuddy);
    }

    private void loadProfileFromDatabase() {
        if (isOwnerProfile) {
            loadOwnerProfile();
        } else {
            loadBuddyProfile();
        }
    }

    private void loadBuddyProfile() {
        currentBuddy = dbHelper.getBuddyById(buddyId, currentUserId);

        if (currentBuddy == null) {
            Toast.makeText(
                    this,
                    "Buddy was not found or does not belong to this account.",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        tvBuddyName.setText(safeText(currentBuddy.getBuddyName()));
        tvBuddyStateHeader.setVisibility(View.VISIBLE);
        tvBuddyStateHeader.setText(safeText(currentBuddy.getBuddyState()));
        tvBuddyEmail.setText(safeText(currentBuddy.getBuddyEmail()));
        tvBuddyPhone.setText(safeText(currentBuddy.getBuddyPhoneNum()));
        tvBuddyGender.setText(safeText(currentBuddy.getBuddyGender()));
        tvBuddyStateValue.setText(safeText(currentBuddy.getBuddyState()));
        tvBuddyAddress.setText(buildDisplayAddress(currentBuddy));

        ImageStorageUtils.loadCircularIntoImageView(
                this,
                imgBuddyProfile,
                currentBuddy.getBuddyPic(),
                R.drawable.ic_profile_person
        );

        btnDeleteBuddy.setVisibility(View.VISIBLE);
        btnAddBuddyToGroup.setVisibility(View.VISIBLE);
        btnEditBuddy.setText("Edit Buddy");

        genderRow.setVisibility(View.VISIBLE);
        genderDivider.setVisibility(View.VISIBLE);
        addressRow.setVisibility(View.VISIBLE);
        addressDivider.setVisibility(View.VISIBLE);
        stateRow.setVisibility(View.VISIBLE);
    }

    private void loadOwnerProfile() {
        User user = dbHelper.getUserProfile(buddyId);

        if (user == null) {
            Toast.makeText(this, "User profile was not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvBuddyName.setText(safeText(user.getUserName()));
        tvBuddyStateHeader.setVisibility(View.GONE);
        tvBuddyEmail.setText(safeText(user.getUserEmail()));
        tvBuddyPhone.setText(safeText(user.getUserPhoneNum()));

        ImageStorageUtils.loadCircularIntoImageView(
                this,
                imgBuddyProfile,
                user.getUserPic(),
                R.drawable.ic_profile_person
        );

        btnDeleteBuddy.setVisibility(View.GONE);
        btnAddBuddyToGroup.setVisibility(View.GONE);
        btnEditBuddy.setText("Edit Profile");

        genderRow.setVisibility(View.GONE);
        genderDivider.setVisibility(View.GONE);
        addressRow.setVisibility(View.GONE);
        addressDivider.setVisibility(View.GONE);
        stateRow.setVisibility(View.GONE);
    }

    private void setClickListeners() {
        btnBackBuddyDetails.setOnClickListener(this::animateAndClose);

        btnEditBuddy.setOnClickListener(v -> {
            animateOnly(v);

            Intent intent = new Intent(this, EditBuddyActivity.class);
            intent.putExtra("buddy_id", buddyId);
            intent.putExtra("is_owner_profile", isOwnerProfile);
            editBuddyLauncher.launch(intent);
        });

        btnDeleteBuddy.setOnClickListener(v -> {
            animateOnly(v);
            showDeleteConfirmationDialog();
        });

        btnAddBuddyToGroup.setOnClickListener(v -> {
            animateOnly(v);
            showChooseGroupDialog();
        });
    }

    private void showDeleteConfirmationDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_buddy);
        dialog.setCancelable(true);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView btnCancelDelete = dialog.findViewById(R.id.btnCancelDelete);
        TextView btnConfirmDelete = dialog.findViewById(R.id.btnConfirmDelete);

        btnCancelDelete.setOnClickListener(v -> dialog.dismiss());

        btnConfirmDelete.setOnClickListener(v -> {
            btnConfirmDelete.setEnabled(false);

            boolean deleted = dbHelper.deleteBuddy(buddyId, currentUserId);

            if (deleted) {
                dialog.dismiss();
                Toast.makeText(this, "Buddy deleted successfully.", Toast.LENGTH_SHORT).show();

                Intent resultIntent = new Intent();
                resultIntent.putExtra("deleted_buddy_id", buddyId);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                btnConfirmDelete.setEnabled(true);
                Toast.makeText(this, "Failed to delete buddy.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
        resizeDialog(window);
    }

    private void showChooseGroupDialog() {
        if (currentBuddy == null) {
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_choose_group);
        dialog.setCancelable(true);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView nameView = dialog.findViewById(R.id.tvChooseGroupBuddyName);
        LinearLayout groupsContainer = dialog.findViewById(R.id.groupsContainer);
        TextView btnCancel = dialog.findViewById(R.id.btnCancelChooseGroup);
        TextView btnConfirm = dialog.findViewById(R.id.btnConfirmChooseGroup);

        if (nameView != null) {
            nameView.setText("Choose groups for " + currentBuddy.getBuddyName());
        }

        List<UserGroup> groups = dbHelper.getGroupsByUserId(currentUserId);
        List<CheckBox> checkBoxes = new ArrayList<>();

        groupsContainer.removeAllViews();

        for (UserGroup group : groups) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(group.getGroupName());
            checkBox.setTag(group.getGroupId());
            checkBox.setChecked(dbHelper.isBuddyAlreadyInGroup(group.getGroupId(), buddyId));
            checkBox.setTextColor(Color.parseColor("#4A3A3A"));
            checkBox.setTextSize(10f);
            checkBox.setButtonTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#FF6F61"))
            );

            try {
                checkBox.setTypeface(ResourcesCompat.getFont(this, R.font.poppins_regular));
            } catch (Exception ignored) {
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(44)
            );
            checkBox.setLayoutParams(params);

            groupsContainer.addView(checkBox);
            checkBoxes.add(checkBox);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            List<String> selectedGroupIds = new ArrayList<>();

            for (CheckBox checkBox : checkBoxes) {
                if (checkBox.isChecked()) {
                    selectedGroupIds.add((String) checkBox.getTag());
                }
            }

            boolean saved = dbHelper.replaceBuddyGroups(
                    buddyId,
                    currentUserId,
                    selectedGroupIds
            );

            if (saved) {
                Toast.makeText(this, "Buddy groups updated.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Unable to update buddy groups.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
        resizeDialog(window);
    }

    private String buildDisplayAddress(Buddy buddy) {
        StringBuilder builder = new StringBuilder();
        appendAddress(builder, buddy.getBuddyAddress1());
        appendAddress(builder, buddy.getBuddyAddress2());
        appendAddress(builder, buddy.getBuddyAddress3());
        return builder.toString();
    }

    private void appendAddress(StringBuilder builder, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }

        if (builder.length() > 0) {
            builder.append("\n");
        }

        builder.append(value.trim());
    }

    private void resizeDialog(Window window) {
        if (window == null) {
            return;
        }

        WindowManager.LayoutParams params = window.getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.88);
        window.setAttributes(params);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private void animateOnly(View view) {
        view.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
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

    private void animateAndClose(View view) {
        animateOnly(view);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}