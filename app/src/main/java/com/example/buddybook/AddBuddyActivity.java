package com.example.buddybook;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class AddBuddyActivity extends BaseActivity {

    private View btnBackAddBuddy;
    private View btnAddPhoto;
    private View stateContainer;
    private View btnCancelAddBuddy;
    private View btnSaveBuddy;

    private TextView tvSelectedState;
    private EditText etBuddyAddress1;
    private EditText etBuddyAddress2;
    private EditText etBuddyAddress3;

    private EditText etBuddyName;
    private EditText etBuddyEmail;
    private EditText etBuddyPhone;

    private RadioButton radioMale;
    private RadioButton radioFemale;
    private RadioButton radioOther;

    private ShapeableImageView imgAddBuddyPhoto;
    private TextView tvAddBuddyPhoto;
    private Uri selectedImageUri;
    private DatabaseHelper dbHelper;

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri == null) return;

                        selectedImageUri = uri;
                        imgAddBuddyPhoto.setImageURI(uri);
                        imgAddBuddyPhoto.setScaleType(
                                ImageView.ScaleType.CENTER_CROP
                        );
                        imgAddBuddyPhoto.setPadding(0, 0, 0, 0);
                        tvAddBuddyPhoto.setVisibility(View.GONE);
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_buddy);

        dbHelper = DatabaseHelper.getInstance(getApplicationContext());

        btnBackAddBuddy = findViewById(R.id.btnBackAddBuddy);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        stateContainer = findViewById(R.id.stateContainer);
        btnCancelAddBuddy = findViewById(R.id.btnCancelAddBuddy);
        btnSaveBuddy = findViewById(R.id.btnSaveBuddy);
        tvSelectedState = findViewById(R.id.tvSelectedState);
        imgAddBuddyPhoto = findViewById(R.id.imgAddBuddyPhoto);
        tvAddBuddyPhoto = findViewById(R.id.tvAddBuddyPhoto);

        etBuddyAddress1 = findViewById(R.id.etBuddyAddress1);
        etBuddyAddress2 = findViewById(R.id.etBuddyAddress2);
        etBuddyAddress3 = findViewById(R.id.etBuddyAddress3);

        etBuddyName = findViewById(R.id.etBuddyName);
        etBuddyEmail = findViewById(R.id.etBuddyEmail);
        etBuddyPhone = findViewById(R.id.etBuddyPhone);

        radioMale = findViewById(R.id.radioMale);
        radioFemale = findViewById(R.id.radioFemale);
        radioOther = findViewById(R.id.radioOther);

        btnBackAddBuddy.setOnClickListener(v ->
                animateAndClose(v)
        );

        btnCancelAddBuddy.setOnClickListener(v ->
                animateAndClose(v)
        );

        btnAddPhoto.setOnClickListener(v -> {
            animateOnly(v);
            galleryLauncher.launch("image/*");
        });

        stateContainer.setOnClickListener(v ->
                showStateDialog()
        );

        btnSaveBuddy.setOnClickListener(v ->
                validateAndSaveBuddy(v)
        );
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

                    finish();

                    overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    );
                })
                .start();
    }

    private void validateAndSaveBuddy(View view) {

        String name = etBuddyName.getText().toString().trim();
        String email = etBuddyEmail.getText().toString().trim();
        String phone = etBuddyPhone.getText().toString().trim();
        String address1 = etBuddyAddress1.getText().toString().trim();
        String address2 = etBuddyAddress2.getText().toString().trim();
        String address3 = etBuddyAddress3.getText().toString().trim();
        String selectedState = tvSelectedState.getText().toString().trim();

        if (name.isEmpty()) {
            etBuddyName.setError("Full name is required");
            etBuddyName.requestFocus();
            return;
        }

        if (!radioMale.isChecked() && !radioFemale.isChecked() && !radioOther.isChecked()) {
            Toast.makeText(AddBuddyActivity.this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty()) {
            etBuddyEmail.setError("Email is required");
            etBuddyEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etBuddyEmail.setError("Enter a valid email");
            etBuddyEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etBuddyPhone.setError("Phone number is required");
            etBuddyPhone.requestFocus();
            return;
        }

        if (address1.isEmpty()) {
            etBuddyAddress1.setError("Address Line 1 is required");
            etBuddyAddress1.requestFocus();
            return;
        }

        if (address2.isEmpty()) {
            etBuddyAddress2.setError("Address Line 2 is required");
            etBuddyAddress2.requestFocus();
            return;
        }

        if (address3.isEmpty()) {
            etBuddyAddress3.setError("Postcode and city are required");
            etBuddyAddress3.requestFocus();
            return;
        }

        if (selectedState.isEmpty() || selectedState.equalsIgnoreCase("Select state")) {
            Toast.makeText(AddBuddyActivity.this, "Please select a state", Toast.LENGTH_SHORT).show();
            return;
        }

        String gender = "";
        if (radioMale.isChecked()) {
            gender = "Male";
        } else if (radioFemale.isChecked()) {
            gender = "Female";
        } else if (radioOther.isChecked()) {
            gender = "Other";
        }

        final String finalGender = gender;

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

                    String storedPhotoPath = "";

                    if (selectedImageUri != null) {
                        storedPhotoPath =
                                ImageStorageUtils.saveImageToInternalStorage(
                                        AddBuddyActivity.this,
                                        selectedImageUri,
                                        "buddy"
                                );

                        if (storedPhotoPath == null) {
                            Toast.makeText(
                                    AddBuddyActivity.this,
                                    "Unable to save the selected picture.",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }
                    }

                    if (currentUserId == null || currentUserId.trim().isEmpty()) {
                        ImageStorageUtils.deleteStoredImage(
                                AddBuddyActivity.this,
                                storedPhotoPath
                        );
                        Toast.makeText(
                                AddBuddyActivity.this,
                                "Your login session is invalid. Please log in again.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    String newBuddyId = dbHelper.createBuddy(
                            currentUserId,
                            name,
                            finalGender,
                            email,
                            phone,
                            address1,
                            address2,
                            address3,
                            selectedState,
                            storedPhotoPath
                    );

                    if (newBuddyId != null) {
                        Toast.makeText(AddBuddyActivity.this, "Buddy saved successfully!", Toast.LENGTH_SHORT).show();

                        showAddToGroupDialog(newBuddyId);

                        overridePendingTransition(
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                        );
                    } else {
                        ImageStorageUtils.deleteStoredImage(
                                AddBuddyActivity.this,
                                storedPhotoPath
                        );

                        Toast.makeText(
                                AddBuddyActivity.this,
                                "Failed to save buddy to database.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .start();
    }

    private void showStateDialog() {

        Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_states);
        dialog.setCancelable(true);

        Window window = dialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );
        }

        int[] stateIds = {
                R.id.stateJohor,
                R.id.stateKedah,
                R.id.stateKelantan,
                R.id.stateMelaka,
                R.id.stateNegeriSembilan,
                R.id.statePahang,
                R.id.statePenang,
                R.id.statePerak,
                R.id.statePerlis,
                R.id.stateSabah,
                R.id.stateSarawak,
                R.id.stateSelangor,
                R.id.stateTerengganu,
                R.id.stateKualaLumpur,
                R.id.stateLabuan,
                R.id.statePutrajaya
        };

        for (int stateId : stateIds) {

            TextView stateView = dialog.findViewById(stateId);

            stateView.setOnClickListener(v -> {

                TextView selectedState = (TextView) v;

                tvSelectedState.setText(
                        selectedState.getText()
                );

                tvSelectedState.setTextColor(
                        Color.parseColor("#4A3A3A")
                );

                dialog.dismiss();
            });
        }

        TextView btnClose =
                dialog.findViewById(R.id.btnCloseStateDialog);

        btnClose.setOnClickListener(v ->
                dialog.dismiss()
        );

        dialog.show();

        if (window != null) {

            WindowManager.LayoutParams params =
                    window.getAttributes();

            params.width = (int) (
                    getResources()
                            .getDisplayMetrics()
                            .widthPixels * 0.88
            );

            window.setAttributes(params);
        }
    }

    private void showAddToGroupDialog(String newBuddyId) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_choose_group);
        dialog.setCancelable(false);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        LinearLayout groupsContainer = dialog.findViewById(R.id.groupsContainer);
        TextView btnCancel = dialog.findViewById(R.id.btnCancelChooseGroup);
        TextView btnConfirm = dialog.findViewById(R.id.btnConfirmChooseGroup);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(getApplicationContext());
        List<UserGroup> availableGroups = dbHelper.getGroupsByUserId(currentUserId);

        List<CheckBox> checkBoxesList = new ArrayList<>();

        for (UserGroup group : availableGroups) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(group.getGroupName());
            checkBox.setTag(group.getGroupId());
            checkBox.setTextColor(Color.parseColor("#4A3A3A"));
            checkBox.setTextSize(10f);
            checkBox.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FF6F61")));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (44 * getResources().getDisplayMetrics().density));
            checkBox.setLayoutParams(params);

            try {
                checkBox.setTypeface(
                        androidx.core.content.res.ResourcesCompat.getFont(
                                this,
                                R.font.poppins_regular
                        )
                );
            } catch (Exception ignored) {
            }
            View divider = new View(this);
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
            divider.setLayoutParams(dividerParams);
            divider.setBackgroundColor(Color.parseColor("#F0E1D8"));
            groupsContainer.addView(checkBox);
            groupsContainer.addView(divider);
            checkBoxesList.add(checkBox);
        }

        btnConfirm.setOnClickListener(v -> {
            List<String> selectedGroupIds = new ArrayList<>();

            for (CheckBox cb : checkBoxesList) {
                if (cb.isChecked()) {
                    selectedGroupIds.add((String) cb.getTag()); // Retrieve the hidden DB ID string
                }
            }

            if (selectedGroupIds.isEmpty()) {
                Toast.makeText(AddBuddyActivity.this, "Please select at least one group", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean allLinked = true;
            for (String groupId : selectedGroupIds) {
                boolean success = dbHelper.addToGroup(groupId, newBuddyId);
                if (!success) allLinked = false;
            }

            if (allLinked) {
                Toast.makeText(AddBuddyActivity.this, "Successfully added to selected groups!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                Toast.makeText(AddBuddyActivity.this, "Partial mapping failures encountered saving relations.", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        dialog.show();

        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.88);
            window.setAttributes(params);
        }
    }
}