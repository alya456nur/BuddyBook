package com.example.buddybook;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class GroupDetailsActivity extends AppCompatActivity {

    private List<Buddy> currentBuddiesList;
    private TextView tvGroupDetailsTitle;
    private TextView tvGroupMemberCount;

    private LinearLayout membersContainer;

    private EditText etGroupName;

    private View btnBackGroupDetails;
    private TextView btnDeleteGroup;
    private TextView btnEditGroup;

    private String groupName;
    private String groupId;

    private boolean isEditing = false;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        dbHelper = DatabaseHelper.getInstance(getApplicationContext());

        connectViews();
        loadGroup();
        setClickListeners();
    }

    private void connectViews() {
        tvGroupDetailsTitle = findViewById(R.id.tvGroupDetailsTitle);
        tvGroupMemberCount = findViewById(R.id.tvGroupMemberCount);
        etGroupName = findViewById(R.id.etGroupName);
        btnBackGroupDetails = findViewById(R.id.btnBackGroupDetails);
        btnDeleteGroup = findViewById(R.id.btnDeleteGroup);
        btnEditGroup = findViewById(R.id.btnEditGroup);

        membersContainer = findViewById(R.id.groupMemberContainer);
    }

    private void loadGroup() {
        groupId = getIntent().getStringExtra("group_id");
        groupName = getIntent().getStringExtra("group_name");

        // Default if empty
        if (groupName == null) {
            groupName = "Family";
        }

        tvGroupDetailsTitle.setText(groupName);
        etGroupName.setText(groupName);

        loadMembersFromDatabase();
    }

    private void loadMembersFromDatabase() {
        currentBuddiesList = dbHelper.getBuddiesByGroupId(groupId);

        membersContainer.removeAllViews();

        if (currentBuddiesList == null || currentBuddiesList.isEmpty()) {
            tvGroupMemberCount.setText("0 members");

            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("There's no members");
            tvEmpty.setTextSize(14);
            tvEmpty.setTextColor(Color.GRAY);
            tvEmpty.setGravity(Gravity.CENTER);
            tvEmpty.setPadding(0, 50, 0, 50);

            membersContainer.addView(tvEmpty);
            return;
        }

        tvGroupMemberCount.setText(currentBuddiesList.size() + " members");

        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);

        for (Buddy buddy : currentBuddiesList) {
            View memberCard = inflater.inflate(R.layout.item_member_card, membersContainer, false);

            TextView tvCardBuddyName = memberCard.findViewById(R.id.tvCardBuddyName);
            TextView tvCardBuddyStateGender = memberCard.findViewById(R.id.tvCardBuddyStateGender);
            TextView tvCardBuddyEmail = memberCard.findViewById(R.id.tvCardBuddyEmail);
            CheckBox checkMember = memberCard.findViewById(R.id.checkMember);

            tvCardBuddyName.setText(buddy.getBuddyName());
            tvCardBuddyStateGender.setText(buddy.getBuddyState() + " • " + buddy.getBuddyGender());
            tvCardBuddyEmail.setText(buddy.getBuddyEmail());

            if (checkMember != null) {
                checkMember.setVisibility(isEditing ? View.VISIBLE : View.GONE);
                checkMember.setEnabled(isEditing);
                checkMember.setChecked(false);
                checkMember.setTag(buddy);
            }

            memberCard.setOnClickListener(v -> {
                if (isEditing) {
                    if (checkMember != null) {
                        checkMember.setChecked(!checkMember.isChecked());
                    }
                } else {
                    openBuddyProfile(v, buddy);
                }
            });

            membersContainer.addView(memberCard);
        }
    }

    private void openBuddyProfile(View view, Buddy buddy) {
        Intent intent = new Intent(GroupDetailsActivity.this, BuddyDetailsActivity.class);

        intent.putExtra("buddy_id", buddy.getBuddyId());

        intent.putExtra("buddy_name", buddy.getBuddyName());
        intent.putExtra("buddy_state", buddy.getBuddyState());
        intent.putExtra("buddy_gender", buddy.getBuddyGender());
        intent.putExtra("buddy_email", buddy.getBuddyEmail());
        intent.putExtra("buddy_phone", buddy.getBuddyPhoneNum());

        StringBuilder fullAddress = new StringBuilder();
        if (buddy.getBuddyAddress1() != null) fullAddress.append(buddy.getBuddyAddress1()).append("\n");
        if (buddy.getBuddyAddress2() != null) fullAddress.append(buddy.getBuddyAddress2()).append("\n");
        if (buddy.getBuddyAddress3() != null) fullAddress.append(buddy.getBuddyAddress3());

        intent.putExtra("buddy_address", fullAddress.toString().trim());

        if (buddy.getBuddyPic() != null) {
            intent.putExtra("buddy_pic", buddy.getBuddyPic());
        }

        startActivity(intent);

        overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );
    }

    private void setClickListeners() {
        btnBackGroupDetails.setOnClickListener(v -> {
            if (isEditing) {
                finishEditing();
            } else {
                finish();
            }
        });

        btnEditGroup.setOnClickListener(v -> {
            animateOnly(v);

            if (isEditing) {
                finishEditing();
            } else {
                startEditing();
            }
        });

        btnDeleteGroup.setOnClickListener(v -> {
            animateOnly(v);

            if (isEditing) {
                deleteSelectedMembers();
            } else {
                showDeleteGroupDialog();
            }
        });
    }

    private void startEditing() {
        isEditing = true;

        tvGroupDetailsTitle.setVisibility(View.GONE);
        etGroupName.setVisibility(View.VISIBLE);

        etGroupName.setText(groupName);
        etGroupName.setSelection(
                etGroupName.getText().length()
        );

        etGroupName.requestFocus();

        btnEditGroup.setText("Done");
        btnDeleteGroup.setText("Delete Selected");

        loadMembersFromDatabase();
    }

    private void finishEditing() {
        String updatedGroupName = etGroupName.getText().toString().trim();

        if (updatedGroupName.isEmpty()) {
            etGroupName.setError("Group name is required");
            etGroupName.requestFocus();
            return;
        }

        boolean isSaved = dbHelper.updateGroupName(groupId, updatedGroupName);

        if (isSaved) {
            groupName = updatedGroupName;

            tvGroupDetailsTitle.setText(groupName);
            tvGroupDetailsTitle.setVisibility(View.VISIBLE);

            etGroupName.setVisibility(View.GONE);

            btnEditGroup.setText("Edit Group");
            btnDeleteGroup.setText("Delete Group");

            isEditing = false;

            loadMembersFromDatabase();

            Toast.makeText(
                    this,
                    "Group updated successfully",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    this,
                    "Failed to update group name in database",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void deleteSelectedMembers() {
        boolean memberSelected = false;
        boolean allRemoved = true;

        for (int i = 0; i < membersContainer.getChildCount(); i++) {
            View memberCard = membersContainer.getChildAt(i);
            CheckBox checkMember =
                    memberCard.findViewById(R.id.checkMember);

            if (checkMember != null
                    && checkMember.getVisibility() == View.VISIBLE
                    && checkMember.isChecked()) {

                memberSelected = true;

                Buddy buddyToRemove =
                        (Buddy) checkMember.getTag();

                if (buddyToRemove != null) {
                    boolean removed =
                            dbHelper.deleteBuddyFromGroup(
                                    groupId,
                                    buddyToRemove.getBuddyId()
                            );

                    if (!removed) {
                        allRemoved = false;
                    }
                }
            }
        }

        if (!memberSelected) {
            Toast.makeText(
                    this,
                    "Select at least one buddy",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        loadMembersFromDatabase();

        Toast.makeText(
                this,
                allRemoved
                        ? "Selected buddies removed"
                        : "Some buddies could not be removed",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void showDeleteGroupDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_group);
        dialog.setCancelable(true);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView btnCancelDeleteGroup = dialog.findViewById(R.id.btnCancelDeleteGroup);
        TextView btnConfirmDeleteGroup = dialog.findViewById(R.id.btnConfirmDeleteGroup);

        btnCancelDeleteGroup.setOnClickListener(v -> {
            animateOnly(v);
            dialog.dismiss();
        });

        btnConfirmDeleteGroup.setOnClickListener(v -> {
            btnConfirmDeleteGroup.setEnabled(false);

            boolean deleted = dbHelper.deleteGroup(groupId);

            if (!deleted) {
                btnConfirmDeleteGroup.setEnabled(true);

                Toast.makeText(
                        GroupDetailsActivity.this,
                        "Failed to delete group",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Toast.makeText(
                    GroupDetailsActivity.this,
                    "Group deleted",
                    Toast.LENGTH_SHORT
            ).show();

            dialog.dismiss();

            Intent intent = new Intent(
                    GroupDetailsActivity.this,
                    MyGroupsActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            );

            startActivity(intent);

            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );

            finish();
        });

        dialog.show();

        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.88);
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
}