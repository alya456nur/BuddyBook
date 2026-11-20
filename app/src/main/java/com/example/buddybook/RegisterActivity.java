package com.example.buddybook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    /*
     * Password rules:
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one number
     * - At least one special character
     * - No spaces
     */
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile(
                    "^(?=.*[a-z])" +
                            "(?=.*[A-Z])" +
                            "(?=.*\\d)" +
                            "(?=.*[@$!%*?&.#_\\-])" +
                            "[A-Za-z\\d@$!%*?&.#_\\-]{8,}$"
            );

    /*
     * Allows Malaysian or international phone numbers.
     * Example:
     * 0123456789
     * +60123456789
     */
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{9,15}$");

    private Spinner spSecurityQuestion;

    private EditText etRegisterPassword;
    private EditText etConfirmPassword;
    private EditText etFullName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etSecurityAnswer;

    private ImageView imgRegisterPasswordVisibility;
    private ImageView imgConfirmPasswordVisibility;
    private ImageView btnBack;
    private ImageView imgAvatar;

    private TextView btnRegister;
    private TextView tvLogin;
    private TextView tvAddPhoto;

    private View avatarContainer;
    private ToggleButton checkTerms;

    private boolean registerPasswordVisible = false;
    private boolean confirmPasswordVisible = false;
    private boolean registrationInProgress = false;

    private DatabaseHelper dbHelper;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri == null) {
                            return;
                        }

                        selectedImageUri = uri;

                        imgAvatar.setImageURI(uri);
                        imgAvatar.setScaleType(
                                ImageView.ScaleType.CENTER_CROP
                        );

                        tvAddPhoto.setVisibility(View.GONE);
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = DatabaseHelper.getInstance(
                getApplicationContext()
        );

        dbHelper.getWritableDatabase();

        initViews();
        setupSecurityQuestions();
        setupListeners();
        setupBackButtonHandler();
    }

    private void initViews() {
        tvAddPhoto = findViewById(R.id.tvAddPhoto);
        avatarContainer = findViewById(R.id.avatarContainer);
        imgAvatar = findViewById(R.id.imgAvatar);

        etRegisterPassword = findViewById(
                R.id.etRegisterPassword
        );

        etConfirmPassword = findViewById(
                R.id.etConfirmPassword
        );

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPhone = findViewById(R.id.etPhone);

        etSecurityAnswer = findViewById(
                R.id.etSecurityAnswer
        );

        imgRegisterPasswordVisibility = findViewById(
                R.id.imgRegisterPasswordVisibility
        );

        imgConfirmPasswordVisibility = findViewById(
                R.id.imgConfirmPasswordVisibility
        );

        btnBack = findViewById(R.id.btnBack);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        spSecurityQuestion = findViewById(
                R.id.spSecurityQuestion
        );

        checkTerms = findViewById(R.id.checkTerms);
    }

    private void setupSecurityQuestions() {
        String[] questions = {
                "Select your security question",
                "What is your mother's first name?",
                "What is your favourite food?",
                "What was the name of your first school?",
                "What is your favourite color?",
                "What is your dream job?"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.item_spinner_question,
                        questions
                );

        adapter.setDropDownViewResource(
                R.layout.item_spinner_dropdown
        );

        spSecurityQuestion.setAdapter(adapter);
    }

    private void setupListeners() {
        avatarContainer.setOnClickListener(
                view -> galleryLauncher.launch("image/*")
        );

        imgRegisterPasswordVisibility.setOnClickListener(
                view -> toggleRegisterPassword()
        );

        imgConfirmPasswordVisibility.setOnClickListener(
                view -> toggleConfirmPassword()
        );

        btnBack.setOnClickListener(
                view -> returnToLogin()
        );

        tvLogin.setOnClickListener(
                view -> returnToLogin()
        );

        btnRegister.setOnClickListener(
                this::handleRegistration
        );
    }

    private void setupBackButtonHandler() {
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        returnToLogin();
                    }
                }
        );
    }

    private void handleRegistration(View view) {
        if (registrationInProgress) {
            return;
        }

        clearErrors();

        String fullName = etFullName
                .getText()
                .toString()
                .trim();

        String email = etEmail
                .getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        String phone = etPhone
                .getText()
                .toString()
                .trim();

        String password = etRegisterPassword
                .getText()
                .toString();

        String confirmPassword = etConfirmPassword
                .getText()
                .toString();

        String securityAnswer = normalizeSecurityAnswer(
                etSecurityAnswer
                        .getText()
                        .toString()
        );

        int selectedQuestionPosition =
                spSecurityQuestion.getSelectedItemPosition();

        String securityQuestion =
                selectedQuestionPosition > 0
                        ? spSecurityQuestion
                          .getSelectedItem()
                          .toString()
                        : "";

        boolean valid = validateRegistrationForm(
                fullName,
                email,
                phone,
                password,
                confirmPassword,
                selectedQuestionPosition,
                securityAnswer
        );

        if (!valid) {
            return;
        }

        registerUser(
                view,
                fullName,
                email,
                phone,
                password,
                securityQuestion,
                securityAnswer
        );
    }

    private boolean validateRegistrationForm(
            String fullName,
            String email,
            String phone,
            String password,
            String confirmPassword,
            int securityQuestionPosition,
            String securityAnswer
    ) {
        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return false;
        }

        if (fullName.length() < 2) {
            etFullName.setError(
                    "Please enter your complete name"
            );

            etFullName.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etEmail.setError(
                    "Please enter a valid email address"
            );

            etEmail.requestFocus();
            return false;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return false;
        }

        String cleanedPhone = phone
                .replace(" ", "")
                .replace("-", "");

        if (!PHONE_PATTERN
                .matcher(cleanedPhone)
                .matches()) {

            etPhone.setError(
                    "Enter a valid phone number"
            );

            etPhone.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etRegisterPassword.setError(
                    "Password is required"
            );

            etRegisterPassword.requestFocus();
            return false;
        }

        if (!PASSWORD_PATTERN
                .matcher(password)
                .matches()) {

            etRegisterPassword.setError(
                    "Use at least 8 characters with uppercase, " +
                            "lowercase, number and special character"
            );

            etRegisterPassword.requestFocus();

            Toast.makeText(
                    this,
                    "Password example: Buddy@123",
                    Toast.LENGTH_LONG
            ).show();

            return false;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError(
                    "Please confirm your password"
            );

            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(
                    "Passwords do not match"
            );

            etConfirmPassword.requestFocus();
            return false;
        }

        if (securityQuestionPosition == 0) {
            Toast.makeText(
                    this,
                    "Please select a security question",
                    Toast.LENGTH_SHORT
            ).show();

            spSecurityQuestion.requestFocus();
            return false;
        }

        if (securityAnswer.isEmpty()) {
            etSecurityAnswer.setError(
                    "Security answer is required"
            );

            etSecurityAnswer.requestFocus();
            return false;
        }

        if (securityAnswer.length() < 2) {
            etSecurityAnswer.setError(
                    "Please enter a valid security answer"
            );

            etSecurityAnswer.requestFocus();
            return false;
        }

        if (!checkTerms.isChecked()) {
            Toast.makeText(
                    this,
                    "Please accept the Terms and Conditions",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        return true;
    }

    private void registerUser(
            View button,
            String fullName,
            String email,
            String phone,
            String password,
            String securityQuestion,
            String securityAnswer
    ) {
        registrationInProgress = true;
        button.setEnabled(false);

        button.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(80)
                .withEndAction(() ->
                        button.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(80)
                                .withEndAction(() -> performRegistration(
                                        button,
                                        fullName,
                                        email,
                                        phone,
                                        password,
                                        securityQuestion,
                                        securityAnswer
                                ))
                                .start()
                )
                .start();
    }

    private void performRegistration(
            View button,
            String fullName,
            String email,
            String phone,
            String password,
            String securityQuestion,
            String securityAnswer
    ) {
        try {
            if (dbHelper.isEmailExists(email)) {
                etEmail.setError(
                        "This email is already registered"
                );

                etEmail.requestFocus();

                Toast.makeText(
                        RegisterActivity.this,
                        "An account with this email already exists",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            String imagePath = null;

            if (selectedImageUri != null) {
                imagePath =
                        ImageStorageUtils.saveImageToInternalStorage(
                                RegisterActivity.this,
                                selectedImageUri,
                                "user"
                        );

                if (imagePath == null) {
                    Toast.makeText(
                            RegisterActivity.this,
                            "Unable to save the selected profile picture.",
                            Toast.LENGTH_LONG
                    ).show();

                    return;
                }
            }

            /*
             * The password is hashed inside DatabaseHelper
             * before it is stored.
             */
            boolean success = dbHelper.registerUser(
                    fullName,
                    email,
                    phone,
                    password,
                    securityQuestion,
                    securityAnswer,
                    imagePath
            );

            if (success) {
                Toast.makeText(
                        RegisterActivity.this,
                        "Registration successful. Please log in.",
                        Toast.LENGTH_LONG
                ).show();

                returnToLogin();

            } else {
                ImageStorageUtils.deleteStoredImage(
                        RegisterActivity.this,
                        imagePath
                );

                Toast.makeText(
                        RegisterActivity.this,
                        "Registration failed. Please try again.",
                        Toast.LENGTH_LONG
                ).show();
            }

        } catch (Exception exception) {
            Toast.makeText(
                    RegisterActivity.this,
                    "Registration error: " +
                            exception.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

        } finally {
            registrationInProgress = false;
            button.setEnabled(true);
        }
    }


    /**
     * Makes security answers case-insensitive and space-insensitive.
     *
     * Examples:
     * "Blue Sky", "blue sky", and "BLUESKY" all become "BLUESKY".
     */
    private String normalizeSecurityAnswer(String answer) {
        if (answer == null) {
            return "";
        }

        return answer
                .trim()
                .replaceAll("\\s+", "")
                .toUpperCase(Locale.ROOT);
    }

    private void clearErrors() {
        etFullName.setError(null);
        etEmail.setError(null);
        etPhone.setError(null);
        etRegisterPassword.setError(null);
        etConfirmPassword.setError(null);
        etSecurityAnswer.setError(null);
    }

    private void toggleRegisterPassword() {
        registerPasswordVisible =
                !registerPasswordVisible;

        if (registerPasswordVisible) {
            etRegisterPassword.setTransformationMethod(
                    HideReturnsTransformationMethod.getInstance()
            );

            imgRegisterPasswordVisibility.setImageResource(
                    R.drawable.ic_visibility
            );

            imgRegisterPasswordVisibility.setContentDescription(
                    "Hide password"
            );

        } else {
            etRegisterPassword.setTransformationMethod(
                    PasswordTransformationMethod.getInstance()
            );

            imgRegisterPasswordVisibility.setImageResource(
                    R.drawable.ic_visibility_off
            );

            imgRegisterPasswordVisibility.setContentDescription(
                    "Show password"
            );
        }

        etRegisterPassword.setSelection(
                etRegisterPassword.getText().length()
        );
    }

    private void toggleConfirmPassword() {
        confirmPasswordVisible =
                !confirmPasswordVisible;

        if (confirmPasswordVisible) {
            etConfirmPassword.setTransformationMethod(
                    HideReturnsTransformationMethod.getInstance()
            );

            imgConfirmPasswordVisibility.setImageResource(
                    R.drawable.ic_visibility
            );

            imgConfirmPasswordVisibility.setContentDescription(
                    "Hide password"
            );

        } else {
            etConfirmPassword.setTransformationMethod(
                    PasswordTransformationMethod.getInstance()
            );

            imgConfirmPasswordVisibility.setImageResource(
                    R.drawable.ic_visibility_off
            );

            imgConfirmPasswordVisibility.setContentDescription(
                    "Show password"
            );
        }

        etConfirmPassword.setSelection(
                etConfirmPassword.getText().length()
        );
    }

    private void returnToLogin() {
        Intent intent = new Intent(
                RegisterActivity.this,
                MainActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);
        finish();
    }
}