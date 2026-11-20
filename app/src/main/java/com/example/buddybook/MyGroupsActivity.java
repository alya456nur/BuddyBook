package com.example.buddybook;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MyGroupsActivity extends BaseActivity {

    private View btnBackGroups;
    private View btnCreateGroup;
    private LinearLayout groupsContainer;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups);

        dbHelper = new DatabaseHelper(this);

        connectViews();
        setClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGroups();
    }

    private void connectViews() {
        btnBackGroups = findViewById(R.id.btnBackGroups);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);

        groupsContainer = findViewById(R.id.groupsContainer);
    }

    private void setClickListeners() {
        btnBackGroups.setOnClickListener(v -> animateAndClose(v));

        btnCreateGroup.setOnClickListener(v -> {
            animateOnly(v);
            showCreateGroupDialog();
        });
    }

    private void loadGroups() {
        if (groupsContainer == null) return;

        groupsContainer.removeAllViews();
        List<Group> groups = dbHelper.getMyGroups(currentUserId);

        if (groups == null || groups.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No groups yet. Create a new group!");
            emptyText.setTextSize(16);
            emptyText.setTextColor(getResources().getColor(android.R.color.darker_gray));
            emptyText.setPadding(32, 64, 32, 64);
            groupsContainer.addView(emptyText);
            return;
        }


        LayoutInflater inflater = LayoutInflater.from(this);

        for (Group group : groups) {

            View groupCard = inflater.inflate(R.layout.item_group_card, groupsContainer, false);

            TextView tvGroupName = groupCard.findViewById(R.id.tvGroupName);
            TextView tvMemberCount = groupCard.findViewById(R.id.tvCountMembers);
            TextView tvGroupInitial = groupCard.findViewById(R.id.tvGroupNameInitial);

            tvGroupName.setText(group.getGroupName());
            tvMemberCount.setText(group.getMemberCount() + " members");

            // Set initial
            if (group.getGroupName() != null && !group.getGroupName().isEmpty()) {
                String initial = group.getGroupName().substring(0, 1).toUpperCase();
                tvGroupInitial.setText(initial);
            }

            groupCard.setOnClickListener(v -> {
                Intent intent = new Intent(MyGroupsActivity.this, GroupDetailsActivity.class);
                intent.putExtra("group_id", String.valueOf(group.getId()));
                intent.putExtra("group_name", group.getGroupName());
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });

            groupsContainer.addView(groupCard);
        }
    }

    private void showCreateGroupDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_create_group);
        dialog.setCancelable(true);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText etNewGroupName = dialog.findViewById(R.id.etNewGroupName);
        TextView btnCancelCreateGroup = dialog.findViewById(R.id.btnCancelCreateGroup);
        TextView btnConfirmCreateGroup = dialog.findViewById(R.id.btnConfirmCreateGroup);

        btnCancelCreateGroup.setOnClickListener(v -> {
            animateOnly(v);
            dialog.dismiss();
        });

        btnConfirmCreateGroup.setOnClickListener(v -> {
            String groupName = etNewGroupName.getText().toString().trim();

            if (groupName.isEmpty()) {
                etNewGroupName.setError("Group name is required");
                etNewGroupName.requestFocus();
                return;
            }

            v.animate()
                    .scaleX(0.94f)
                    .scaleY(0.94f)
                    .setDuration(80)
                    .withEndAction(() -> {
                        boolean success = dbHelper.createGroup(currentUserId, groupName);
                        if (success) {
                            Toast.makeText(MyGroupsActivity.this, groupName + " group created successfully!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadGroups();
                        } else {
                            Toast.makeText(MyGroupsActivity.this, "Failed to create group", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .start();
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
                .scaleX(0.96f)
                .scaleY(0.96f)
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
        view.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(80)
                .withEndAction(() -> {
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                })
                .start();
    }
}