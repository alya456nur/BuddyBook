package com.example.buddybook;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns; // Ditambah untuk semakan format email
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etForgotEmail;
    private EditText etForgotSecurityAnswer;
    private EditText etNewResetPassword;
    private EditText etConfirmResetPassword;

    private Spinner spForgotSecurityQuestion;

    private TextView btnVerifyIdentity;
    private TextView btnResetPassword;
    private TextView tvReturnToLogin;
    private TextView tvNewPasswordLabel;
    private TextView tvConfirmResetPasswordLabel;

    private View newPasswordContainer;
    private View confirmResetPasswordContainer;

    private ImageView imgNewResetVisibility;
    private ImageView imgConfirmResetVisibility;
    private ImageView btnBackForgot;

    private boolean newPasswordVisible = false;
    private boolean confirmPasswordVisible = false;
    private boolean identityVerified = false;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        dbHelper = DatabaseHelper.getInstance(getApplicationContext());

        connectViews();
        setupSecurityQuestions();
        setClickListeners();
    }

    private void connectViews() {
        etForgotEmail = findViewById(R.id.etForgotEmail);
        etForgotSecurityAnswer = findViewById(R.id.etForgotSecurityAnswer);
        etNewResetPassword = findViewById(R.id.etNewResetPassword);
        etConfirmResetPassword = findViewById(R.id.etConfirmResetPassword);
        spForgotSecurityQuestion = findViewById(R.id.spForgotSecurityQuestion);
        btnVerifyIdentity = findViewById(R.id.btnVerifyIdentity);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBackForgot = findViewById(R.id.btnBackForgot);
        tvReturnToLogin = findViewById(R.id.tvReturnToLogin);
        tvNewPasswordLabel = findViewById(R.id.tvNewPasswordLabel);
        tvConfirmResetPasswordLabel = findViewById(R.id.tvConfirmResetPasswordLabel);
        newPasswordContainer = findViewById(R.id.newPasswordContainer);
        confirmResetPasswordContainer = findViewById(R.id.confirmResetPasswordContainer);
        imgNewResetVisibility = findViewById(R.id.imgNewResetVisibility);
        imgConfirmResetVisibility = findViewById(R.id.imgConfirmResetVisibility);
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_question,
                questions
        );

        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spForgotSecurityQuestion.setAdapter(adapter);
    }

    private void setClickListeners() {
        btnBackForgot.setOnClickListener(v -> returnToLogin());
        tvReturnToLogin.setOnClickListener(v -> returnToLogin());
        btnVerifyIdentity.setOnClickListener(v -> verifyIdentity());
        btnResetPassword.setOnClickListener(v -> resetPassword());
        imgNewResetVisibility.setOnClickListener(v -> toggleNewPassword());
        imgConfirmResetVisibility.setOnClickListener(v -> toggleConfirmPassword());
    }

    private void verifyIdentity() {
        String email = etForgotEmail.getText().toString().trim();
        String securityQuestion = spForgotSecurityQuestion.getSelectedItem().toString();
        String securityAnswer = normalizeSecurityAnswer(
                etForgotSecurityAnswer
                        .getText()
                        .toString()
        );

        if (email.isEmpty()) {
            etForgotEmail.setError("Enter your email");
            etForgotEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etForgotEmail.setError("Enter a valid email address");
            etForgotEmail.requestFocus();
            return;
        }

        if (spForgotSecurityQuestion.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Select a security question", Toast.LENGTH_SHORT).show();
            return;
        }

        if (securityAnswer.isEmpty()) {
            etForgotSecurityAnswer.setError("Enter your security answer");
            etForgotSecurityAnswer.requestFocus();
            return;
        }

        boolean isIdentified = dbHelper.verifySecurityAnswer(email, securityQuestion, securityAnswer);

        if (isIdentified) {
            identityVerified = true;
            enablePasswordSection();
            Toast.makeText(this, "Identity verified successfully", Toast.LENGTH_SHORT).show();
        } else {
            identityVerified = false;
            Toast.makeText(this, "Incorrect email, question, or answer", Toast.LENGTH_SHORT).show();
            etForgotSecurityAnswer.setError("Invalid identity credentials");
            etForgotSecurityAnswer.requestFocus();
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

    private void enablePasswordSection() {
        etNewResetPassword.setEnabled(true);
        etConfirmResetPassword.setEnabled(true);

        newPasswordContainer.setAlpha(1f);
        confirmResetPasswordContainer.setAlpha(1f);

        tvNewPasswordLabel.setTextColor(Color.parseColor("#4A3A3A"));
        tvConfirmResetPasswordLabel.setTextColor(Color.parseColor("#4A3A3A"));

        imgNewResetVisibility.setClickable(true);
        imgNewResetVisibility.setFocusable(true);

        imgConfirmResetVisibility.setClickable(true);
        imgConfirmResetVisibility.setFocusable(true);

        btnResetPassword.setEnabled(true);
        btnResetPassword.setClickable(true);
        btnResetPassword.setFocusable(true);
        btnResetPassword.setAlpha(1f);

        etNewResetPassword.requestFocus();
    }

    private void resetPassword() {
        if (!identityVerified) {
            return;
        }

        String newPassword = etNewResetPassword.getText().toString().trim();
        String confirmPassword = etConfirmResetPassword.getText().toString().trim();

        if (newPassword.isEmpty()) {
            etNewResetPassword.setError("Enter a new password");
            etNewResetPassword.requestFocus();
            return;
        }

        if (!isValidPassword(newPassword)) {
            etNewResetPassword.setError(
                    "Password must contain at least:\n" +
                            "• 8 characters\n" +
                            "• 1 uppercase letter\n" +
                            "• 1 lowercase letter\n" +
                            "• 1 number\n" +
                            "• 1 special character"
            );
            etNewResetPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmResetPassword.setError("Confirm your new password");
            etConfirmResetPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmResetPassword.setError("Passwords do not match");
            etConfirmResetPassword.requestFocus();
            return;
        }

        btnResetPassword.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(80)
                .withEndAction(() -> {
                    String email = etForgotEmail.getText().toString().trim();

                    String hashedPassword;

                    try {
                        hashedPassword = PasswordUtils.hashPassword(newPassword);
                    } catch (Exception exception) {
                        Toast.makeText(
                                ForgotPasswordActivity.this,
                                "Unable to secure the new password.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    boolean updateSuccess =
                            dbHelper.updatePassword(email, hashedPassword);

                    btnResetPassword.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(80)
                            .withEndAction(() -> {
                                if (updateSuccess) {
                                    Toast.makeText(ForgotPasswordActivity.this, "Password reset successfully", Toast.LENGTH_SHORT).show();
                                    returnToLogin();
                                } else {
                                    Toast.makeText(ForgotPasswordActivity.this, "Failed to update password. Try again.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .start();
                })
                .start();
    }

    private void toggleNewPassword() {
        if (!identityVerified) {
            return;
        }

        if (newPasswordVisible) {
            etNewResetPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imgNewResetVisibility.setImageResource(R.drawable.ic_visibility_off);
            newPasswordVisible = false;
        } else {
            etNewResetPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imgNewResetVisibility.setImageResource(R.drawable.ic_visibility);
            newPasswordVisible = true;
        }

        etNewResetPassword.setSelection(etNewResetPassword.getText().length());
    }

    private void toggleConfirmPassword() {
        if (!identityVerified) {
            return;
        }

        if (confirmPasswordVisible) {
            etConfirmResetPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imgConfirmResetVisibility.setImageResource(R.drawable.ic_visibility_off);
            confirmPasswordVisible = false;
        } else {
            etConfirmResetPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imgConfirmResetVisibility.setImageResource(R.drawable.ic_visibility);
            confirmPasswordVisible = true;
        }

        etConfirmResetPassword.setSelection(etConfirmResetPassword.getText().length());
    }

    private void returnToLogin() {
        Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private boolean isValidPassword(String password) {

        /*
         * Password requirements:
         * - At least 8 characters
         * - 1 uppercase
         * - 1 lowercase
         * - 1 number
         * - 1 special character
         */

        String passwordPattern =
                "^(?=.*[a-z])" +
                        "(?=.*[A-Z])" +
                        "(?=.*\\d)" +
                        "(?=.*[@$!%*?&^#()_+=\\-{}\\[\\]:;\"'<>,./\\\\|`~])" +
                        ".{8,}$";

        return password.matches(passwordPattern);
    }
}