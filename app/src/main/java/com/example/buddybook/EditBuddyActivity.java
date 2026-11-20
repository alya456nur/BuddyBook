package com.example.buddybook;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class EditBuddyActivity extends BaseActivity {

    private View btnBackEditBuddy;
    private View btnChangePhoto;
    private View stateContainerEdit;
    private View btnCancelEditBuddy;
    private View btnSaveChanges;

    private EditText etEditBuddyName;
    private EditText etEditBuddyEmail;
    private EditText etEditBuddyPhone;
    private EditText etEditBuddyAddress1;
    private EditText etEditBuddyAddress2;
    private EditText etEditBuddyAddress3;

    private View tvEditGenderLabel;
    private View tvEditAddress1Label;
    private View editAddress1Container;
    private View tvEditAddress2Label;
    private View editAddress2Container;
    private View tvEditAddress3Label;
    private View editAddress3Container;
    private View tvEditStateLabel;

    private boolean isOwnerProfile = false;

    private String buddyId = "";
    private String existingPhotoPath = "";

    private Uri pendingImageUri;

    private RadioGroup editGenderGroup;
    private RadioButton editRadioMale;
    private RadioButton editRadioFemale;
    private RadioButton editRadioOther;

    private TextView tvSelectedStateEdit;
    private ImageView imgBuddyPhoto;
    private TextView tvChangePhoto;

    private DatabaseHelper dbHelper;

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {

                        if (uri == null) {
                            return;
                        }

                        pendingImageUri = uri;

                        imgBuddyPhoto.setImageURI(uri);

                        imgBuddyPhoto.setScaleType(
                                ImageView.ScaleType.CENTER_CROP
                        );

                        imgBuddyPhoto.setPadding(
                                0,
                                0,
                                0,
                                0
                        );

                        tvChangePhoto.setVisibility(
                                View.GONE
                        );
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_edit_buddy
        );

        dbHelper = DatabaseHelper.getInstance(
                getApplicationContext()
        );

        connectViews();
        loadSelectedBuddy();
        setClickListeners();
    }

    private void connectViews() {

        btnBackEditBuddy =
                findViewById(
                        R.id.btnBackEditBuddy
                );

        btnChangePhoto =
                findViewById(
                        R.id.btnChangePhoto
                );

        stateContainerEdit =
                findViewById(
                        R.id.stateContainerEdit
                );

        btnCancelEditBuddy =
                findViewById(
                        R.id.btnCancelEditBuddy
                );

        btnSaveChanges =
                findViewById(
                        R.id.btnSaveChanges
                );

        etEditBuddyName =
                findViewById(
                        R.id.etEditBuddyName
                );

        etEditBuddyEmail =
                findViewById(
                        R.id.etEditBuddyEmail
                );

        etEditBuddyPhone =
                findViewById(
                        R.id.etEditBuddyPhone
                );

        etEditBuddyAddress1 =
                findViewById(
                        R.id.etEditBuddyAddress1
                );

        etEditBuddyAddress2 =
                findViewById(
                        R.id.etEditBuddyAddress2
                );

        etEditBuddyAddress3 =
                findViewById(
                        R.id.etEditBuddyAddress3
                );

        editGenderGroup =
                findViewById(
                        R.id.editGenderGroup
                );

        editRadioMale =
                findViewById(
                        R.id.editRadioMale
                );

        editRadioFemale =
                findViewById(
                        R.id.editRadioFemale
                );

        editRadioOther =
                findViewById(
                        R.id.editRadioOther
                );

        tvEditGenderLabel =
                findViewById(
                        R.id.tvEditGenderLabel
                );

        tvSelectedStateEdit =
                findViewById(
                        R.id.tvSelectedStateEdit
                );

        tvEditAddress1Label =
                findViewById(
                        R.id.tvEditAddress1Label
                );

        editAddress1Container =
                findViewById(
                        R.id.editAddress1Container
                );

        tvEditAddress2Label =
                findViewById(
                        R.id.tvEditAddress2Label
                );

        editAddress2Container =
                findViewById(
                        R.id.editAddress2Container
                );

        tvEditAddress3Label =
                findViewById(
                        R.id.tvEditAddress3Label
                );

        editAddress3Container =
                findViewById(
                        R.id.editAddress3Container
                );

        tvEditStateLabel =
                findViewById(
                        R.id.tvEditStateLabel
                );

        imgBuddyPhoto =
                findViewById(
                        R.id.imgBuddyPhoto
                );

        tvChangePhoto =
                findViewById(
                        R.id.tvChangePhoto
                );
    }

    private void loadSelectedBuddy() {

        Intent intent = getIntent();

        isOwnerProfile = intent.getBooleanExtra(
                "is_owner_profile",
                false
        );

        buddyId = intent.getStringExtra(
                "buddy_id"
        );

        if (buddyId == null
                || buddyId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Unable to load profile information.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        /*
         * Instead of depending only on values passed through Intent,
         * retrieve the latest values directly from SQLite.
         */
        if (isOwnerProfile) {
            loadUserFromDatabase();
        } else {
            loadBuddyFromDatabase();
        }
    }

    private void loadBuddyFromDatabase() {

        Buddy buddy = dbHelper.getBuddyById(
                buddyId,
                currentUserId
        );

        if (buddy == null) {

            Toast.makeText(
                    this,
                    "Buddy information was not found.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        etEditBuddyName.setText(
                safeText(
                        buddy.getBuddyName()
                )
        );

        etEditBuddyEmail.setText(
                safeText(
                        buddy.getBuddyEmail()
                )
        );

        etEditBuddyPhone.setText(
                safeText(
                        buddy.getBuddyPhoneNum()
                )
        );

        /*
         * Each address line is loaded directly from its
         * own database column.
         */
        etEditBuddyAddress1.setText(
                safeText(
                        buddy.getBuddyAddress1()
                )
        );

        etEditBuddyAddress2.setText(
                safeText(
                        buddy.getBuddyAddress2()
                )
        );

        etEditBuddyAddress3.setText(
                safeText(
                        buddy.getBuddyAddress3()
                )
        );

        String buddyState = safeText(
                buddy.getBuddyState()
        );

        if (!buddyState.isEmpty()) {

            tvSelectedStateEdit.setText(
                    buddyState
            );

            tvSelectedStateEdit.setTextColor(
                    Color.parseColor("#4A3A3A")
            );
        }

        selectGender(
                buddy.getBuddyGender()
        );

        existingPhotoPath = safeText(
                buddy.getBuddyPic()
        );

        loadExistingPhoto();
    }

    private void loadUserFromDatabase() {

        User user = dbHelper.getUserProfile(
                buddyId
        );

        if (user == null) {

            Toast.makeText(
                    this,
                    "User information was not found.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        etEditBuddyName.setText(
                safeText(
                        user.getUserName()
                )
        );

        etEditBuddyEmail.setText(
                safeText(
                        user.getUserEmail()
                )
        );

        etEditBuddyPhone.setText(
                safeText(
                        user.getUserPhoneNum()
                )
        );

        existingPhotoPath = safeText(
                user.getUserPic()
        );

        loadExistingPhoto();
        hideBuddyOnlyFields();
    }

    private void loadExistingPhoto() {

        ImageStorageUtils.loadCircularIntoImageView(
                this,
                imgBuddyPhoto,
                existingPhotoPath,
                R.drawable.ic_add_photo_camera
        );

        if (!existingPhotoPath.isEmpty()) {

            tvChangePhoto.setVisibility(
                    View.GONE
            );

        } else {

            tvChangePhoto.setVisibility(
                    View.VISIBLE
            );
        }
    }

    private void selectGender(
            String gender
    ) {

        if ("Male".equalsIgnoreCase(gender)) {

            editRadioMale.setChecked(true);

        } else if ("Female".equalsIgnoreCase(gender)) {

            editRadioFemale.setChecked(true);

        } else {

            editRadioOther.setChecked(true);
        }
    }

    private void hideBuddyOnlyFields() {

        tvEditGenderLabel.setVisibility(
                View.GONE
        );

        editGenderGroup.setVisibility(
                View.GONE
        );

        tvEditAddress1Label.setVisibility(
                View.GONE
        );

        editAddress1Container.setVisibility(
                View.GONE
        );

        tvEditAddress2Label.setVisibility(
                View.GONE
        );

        editAddress2Container.setVisibility(
                View.GONE
        );

        tvEditAddress3Label.setVisibility(
                View.GONE
        );

        editAddress3Container.setVisibility(
                View.GONE
        );

        tvEditStateLabel.setVisibility(
                View.GONE
        );

        stateContainerEdit.setVisibility(
                View.GONE
        );
    }

    private void setClickListeners() {

        btnBackEditBuddy.setOnClickListener(
                this::animateAndClose
        );

        btnCancelEditBuddy.setOnClickListener(
                this::animateAndClose
        );

        btnChangePhoto.setOnClickListener(v -> {

            animateOnly(v);

            galleryLauncher.launch(
                    "image/*"
            );
        });

        stateContainerEdit.setOnClickListener(
                v -> showStateDialog()
        );

        btnSaveChanges.setOnClickListener(v -> {

            if (isOwnerProfile) {

                validateAndSaveUser(v);

            } else {

                validateAndSaveBuddy(v);
            }
        });
    }

    private void validateAndSaveBuddy(
            View view
    ) {

        String name =
                etEditBuddyName
                        .getText()
                        .toString()
                        .trim();

        String email =
                etEditBuddyEmail
                        .getText()
                        .toString()
                        .trim();

        String phone =
                etEditBuddyPhone
                        .getText()
                        .toString()
                        .trim();

        String address1 =
                etEditBuddyAddress1
                        .getText()
                        .toString()
                        .trim();

        String address2 =
                etEditBuddyAddress2
                        .getText()
                        .toString()
                        .trim();

        String address3 =
                etEditBuddyAddress3
                        .getText()
                        .toString()
                        .trim();

        String selectedState =
                tvSelectedStateEdit
                        .getText()
                        .toString()
                        .trim();

        if (name.isEmpty()) {

            etEditBuddyName.setError(
                    "Full name is required"
            );

            etEditBuddyName.requestFocus();
            return;
        }

        if (email.isEmpty()) {

            etEditBuddyEmail.setError(
                    "Email is required"
            );

            etEditBuddyEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns
                .EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etEditBuddyEmail.setError(
                    "Enter a valid email"
            );

            etEditBuddyEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {

            etEditBuddyPhone.setError(
                    "Phone number is required"
            );

            etEditBuddyPhone.requestFocus();
            return;
        }

        if (!phone.matches(
                "^[0-9+\\- ]{9,15}$"
        )) {

            etEditBuddyPhone.setError(
                    "Enter a valid phone number"
            );

            etEditBuddyPhone.requestFocus();
            return;
        }

        if (editGenderGroup.getCheckedRadioButtonId()
                == -1) {

            Toast.makeText(
                    this,
                    "Please select a gender",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (address1.isEmpty()) {

            etEditBuddyAddress1.setError(
                    "Address Line 1 is required"
            );

            etEditBuddyAddress1.requestFocus();
            return;
        }

        if (address2.isEmpty()) {

            etEditBuddyAddress2.setError(
                    "Address Line 2 is required"
            );

            etEditBuddyAddress2.requestFocus();
            return;
        }

        if (address3.isEmpty()) {

            etEditBuddyAddress3.setError(
                    "Postcode and city are required"
            );

            etEditBuddyAddress3.requestFocus();
            return;
        }

        if (selectedState.isEmpty()
                || selectedState.equalsIgnoreCase(
                "Select State"
        )) {

            Toast.makeText(
                    this,
                    "Please select a state",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String gender = getSelectedGender();

        String finalPhotoPath =
                preparePhotoForSaving();

        if (pendingImageUri != null
                && finalPhotoPath == null) {

            Toast.makeText(
                    this,
                    "Unable to save the selected picture.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        boolean success = dbHelper.updateBuddy(
                buddyId,
                currentUserId,
                name,
                gender,
                email,
                phone,
                address1,
                address2,
                address3,
                selectedState,
                finalPhotoPath
        );

        finishPhotoUpdate(
                success,
                finalPhotoPath,
                view
        );
    }

    private void validateAndSaveUser(
            View view
    ) {

        String name =
                etEditBuddyName
                        .getText()
                        .toString()
                        .trim();

        String email =
                etEditBuddyEmail
                        .getText()
                        .toString()
                        .trim();

        String phone =
                etEditBuddyPhone
                        .getText()
                        .toString()
                        .trim();

        if (name.isEmpty()) {

            etEditBuddyName.setError(
                    "Full name is required"
            );

            etEditBuddyName.requestFocus();
            return;
        }

        if (email.isEmpty()) {

            etEditBuddyEmail.setError(
                    "Email is required"
            );

            etEditBuddyEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns
                .EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etEditBuddyEmail.setError(
                    "Enter a valid email"
            );

            etEditBuddyEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {

            etEditBuddyPhone.setError(
                    "Phone number is required"
            );

            etEditBuddyPhone.requestFocus();
            return;
        }

        if (!phone.matches(
                "^[0-9+\\- ]{9,15}$"
        )) {

            etEditBuddyPhone.setError(
                    "Enter a valid phone number"
            );

            etEditBuddyPhone.requestFocus();
            return;
        }

        String finalPhotoPath =
                preparePhotoForSaving();

        if (pendingImageUri != null
                && finalPhotoPath == null) {

            Toast.makeText(
                    this,
                    "Unable to save the selected picture.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        boolean success = dbHelper.updateUser(
                buddyId,
                name,
                email,
                phone,
                finalPhotoPath
        );

        finishPhotoUpdate(
                success,
                finalPhotoPath,
                view
        );
    }

    private String getSelectedGender() {

        int selectedId =
                editGenderGroup
                        .getCheckedRadioButtonId();

        if (selectedId == R.id.editRadioMale) {

            return "Male";

        } else if (selectedId
                == R.id.editRadioFemale) {

            return "Female";
        }

        return "Other";
    }

    private String preparePhotoForSaving() {

        if (pendingImageUri == null) {

            return existingPhotoPath;
        }

        return ImageStorageUtils
                .saveImageToInternalStorage(
                        this,
                        pendingImageUri,
                        isOwnerProfile
                                ? "user"
                                : "buddy"
                );
    }

    private void finishPhotoUpdate(
            boolean success,
            String finalPhotoPath,
            View view
    ) {

        if (success) {

            if (pendingImageUri != null
                    && !existingPhotoPath.equals(
                    finalPhotoPath
            )) {

                ImageStorageUtils.deleteStoredImage(
                        this,
                        existingPhotoPath
                );
            }

            existingPhotoPath =
                    finalPhotoPath == null
                            ? ""
                            : finalPhotoPath;

            pendingImageUri = null;

            Toast.makeText(
                    this,
                    "Changes saved successfully",
                    Toast.LENGTH_SHORT
            ).show();

            Intent resultIntent =
                    new Intent();

            resultIntent.putExtra(
                    "updated_photo",
                    existingPhotoPath
            );

            resultIntent.putExtra(
                    "updated_buddy_id",
                    buddyId
            );

            setResult(
                    RESULT_OK,
                    resultIntent
            );

            animateAndClose(view);

        } else {

            if (pendingImageUri != null
                    && finalPhotoPath != null) {

                ImageStorageUtils.deleteStoredImage(
                        this,
                        finalPhotoPath
                );
            }

            Toast.makeText(
                    this,
                    "Failed to save changes",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void showStateDialog() {

        Dialog dialog =
                new Dialog(this);

        dialog.requestWindowFeature(
                Window.FEATURE_NO_TITLE
        );

        dialog.setContentView(
                R.layout.dialog_states
        );

        dialog.setCancelable(true);

        Window window =
                dialog.getWindow();

        if (window != null) {

            window.setBackgroundDrawable(
                    new ColorDrawable(
                            Color.TRANSPARENT
                    )
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

            TextView stateView =
                    dialog.findViewById(
                            stateId
                    );

            if (stateView == null) {
                continue;
            }

            stateView.setOnClickListener(v -> {

                TextView selectedState =
                        (TextView) v;

                tvSelectedStateEdit.setText(
                        selectedState.getText()
                );

                tvSelectedStateEdit.setTextColor(
                        Color.parseColor(
                                "#4A3A3A"
                        )
                );

                dialog.dismiss();
            });
        }

        TextView btnClose =
                dialog.findViewById(
                        R.id.btnCloseStateDialog
                );

        if (btnClose != null) {

            btnClose.setOnClickListener(
                    v -> dialog.dismiss()
            );
        }

        dialog.show();

        if (window != null) {

            WindowManager.LayoutParams params =
                    window.getAttributes();

            params.width =
                    (int) (
                            getResources()
                                    .getDisplayMetrics()
                                    .widthPixels
                                    * 0.88
                    );

            window.setAttributes(
                    params
            );
        }
    }

    private void animateOnly(
            View view
    ) {

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

    private void animateAndClose(
            View view
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

                    finish();

                    overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    );
                })
                .start();
    }

    private String safeText(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }
}