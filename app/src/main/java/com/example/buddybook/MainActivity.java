package com.example.buddybook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String SESSION_PREFS = "UserSession";
    private static final String REMEMBER_PREFS = "RememberMePrefs";

    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_USER_NAME = "USER_NAME";
    private static final String KEY_IS_LOGGED_IN = "IS_LOGGED_IN";

    private static final String KEY_REMEMBER_ME = "REMEMBER_ME";
    private static final String KEY_REMEMBERED_EMAIL = "REMEMBERED_EMAIL";
    private static final String KEY_REMEMBERED_PASSWORD = "REMEMBERED_PASSWORD";

    private TextView btnLogin;
    private TextView tvRegister;
    private TextView tvForgotPassword;

    private EditText etPassword;
    private EditText etEmail;

    private ImageView imgPasswordVisibility;
    private ToggleButton checkRememberMe;

    private boolean passwordVisible = false;
    private boolean loginInProgress = false;

    private DatabaseHelper databaseHelper;
    private SharedPreferences sessionPreferences;
    private SharedPreferences rememberPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = DatabaseHelper.getInstance(
                getApplicationContext()
        );

        databaseHelper.getWritableDatabase();

        sessionPreferences = getSharedPreferences(
                SESSION_PREFS,
                MODE_PRIVATE
        );

        rememberPreferences = getSharedPreferences(
                REMEMBER_PREFS,
                MODE_PRIVATE
        );

        /*
         * Check the login session first.
         * A remembered login form is different from an active login session.
         */
        String userId = sessionPreferences.getString(
                KEY_USER_ID,
                null
        );

        boolean isLoggedIn = sessionPreferences.getBoolean(
                KEY_IS_LOGGED_IN,
                false
        );

        if (isLoggedIn &&
                userId != null &&
                !userId.trim().isEmpty()) {

            navigateToHome();
            return;
        }

        initViews();
        loadRememberedLogin();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        tvForgotPassword = findViewById(
                R.id.tvForgotPassword
        );

        imgPasswordVisibility = findViewById(
                R.id.imgPasswordVisibility
        );

        checkRememberMe = findViewById(
                R.id.checkRememberMe
        );
    }

    /**
     * Loads the most recently remembered email and password.
     */
    private void loadRememberedLogin() {
        boolean rememberMe = rememberPreferences.getBoolean(
                KEY_REMEMBER_ME,
                false
        );

        checkRememberMe.setChecked(rememberMe);

        if (!rememberMe) {
            return;
        }

        String rememberedEmail = rememberPreferences.getString(
                KEY_REMEMBERED_EMAIL,
                ""
        );

        String rememberedPassword = rememberPreferences.getString(
                KEY_REMEMBERED_PASSWORD,
                ""
        );

        etEmail.setText(rememberedEmail);
        etPassword.setText(rememberedPassword);

        etEmail.setSelection(etEmail.getText().length());
        etPassword.setSelection(etPassword.getText().length());
    }

    private void setupListeners() {
        tvForgotPassword.setOnClickListener(view -> {
            animateAlpha(view);

            Intent intent = new Intent(
                    MainActivity.this,
                    ForgotPasswordActivity.class
            );

            startActivity(intent);
        });

        imgPasswordVisibility.setOnClickListener(
                view -> togglePasswordVisibility()
        );

        btnLogin.setOnClickListener(
                this::validateAndLogin
        );

        tvRegister.setOnClickListener(view -> {
            animateAlpha(view);

            Intent intent = new Intent(
                    MainActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
        });

        /*
         * When the user unticks Remember me, immediately remove
         * the previously saved email and password.
         */
        checkRememberMe.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (!isChecked) {
                        clearRememberedLogin();
                    }
                }
        );
    }

    private void validateAndLogin(View view) {
        if (loginInProgress) {
            return;
        }

        String email = etEmail
                .getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        String password = etPassword
                .getText()
                .toString();

        etEmail.setError(null);
        etPassword.setError(null);

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etEmail.setError(
                    "Please enter a valid email address"
            );

            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        animateButton(
                view,
                () -> loginUser(email, password)
        );
    }

    private void loginUser(
            String email,
            String password
    ) {
        loginInProgress = true;
        btnLogin.setEnabled(false);

        try {
            /*
             * DatabaseHelper receives the entered password,
             * retrieves the stored password hash, and verifies it
             * using PasswordUtils.verifyPassword().
             */
            User user = databaseHelper.loginUser(
                    email,
                    password
            );

            if (user == null) {
                Toast.makeText(
                        MainActivity.this,
                        "Invalid email or password",
                        Toast.LENGTH_LONG
                ).show();

                /*
                 * Do not clear the password if Remember me is on,
                 * because the user may only have mistyped the email.
                 */
                if (!checkRememberMe.isChecked()) {
                    etPassword.setText("");
                }

                etPassword.requestFocus();
                return;
            }

            /*
             * Save or remove the remembered form only after
             * the credentials have been verified successfully.
             */
            if (checkRememberMe.isChecked()) {
                saveRememberedLogin(email, password);
            } else {
                clearRememberedLogin();
            }

            /*
             * Save the active login session.
             */
            sessionPreferences.edit()
                    .putString(
                            KEY_USER_ID,
                            user.getUserId()
                    )
                    .putString(
                            KEY_USER_NAME,
                            user.getUserName()
                    )
                    .putBoolean(
                            KEY_IS_LOGGED_IN,
                            true
                    )
                    .apply();

            Toast.makeText(
                    MainActivity.this,
                    "Welcome, " + user.getUserName() + "!",
                    Toast.LENGTH_SHORT
            ).show();

            navigateToHome();

        } catch (Exception exception) {
            Toast.makeText(
                    MainActivity.this,
                    "Login error: " +
                            exception.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

        } finally {
            loginInProgress = false;
            btnLogin.setEnabled(true);
        }
    }

    private void saveRememberedLogin(
            String email,
            String password
    ) {
        rememberPreferences.edit()
                .putBoolean(
                        KEY_REMEMBER_ME,
                        true
                )
                .putString(
                        KEY_REMEMBERED_EMAIL,
                        email
                )
                .putString(
                        KEY_REMEMBERED_PASSWORD,
                        password
                )
                .apply();
    }

    private void clearRememberedLogin() {
        rememberPreferences.edit()
                .remove(KEY_REMEMBERED_EMAIL)
                .remove(KEY_REMEMBERED_PASSWORD)
                .putBoolean(KEY_REMEMBER_ME, false)
                .apply();
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            etPassword.setTransformationMethod(
                    HideReturnsTransformationMethod.getInstance()
            );

            imgPasswordVisibility.setImageResource(
                    R.drawable.ic_visibility
            );

            imgPasswordVisibility.setContentDescription(
                    "Hide password"
            );

        } else {
            etPassword.setTransformationMethod(
                    PasswordTransformationMethod.getInstance()
            );

            imgPasswordVisibility.setImageResource(
                    R.drawable.ic_visibility_off
            );

            imgPasswordVisibility.setContentDescription(
                    "Show password"
            );
        }

        etPassword.setSelection(
                etPassword.getText().length()
        );
    }

    private void animateButton(
            View view,
            Runnable action
    ) {
        view.setEnabled(false);

        view.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(80)
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(80)
                                .withEndAction(() -> {
                                    view.setEnabled(true);
                                    action.run();
                                })
                                .start()
                )
                .start();
    }

    private void animateAlpha(View view) {
        view.animate()
                .alpha(0.6f)
                .setDuration(80)
                .withEndAction(() ->
                        view.animate()
                                .alpha(1f)
                                .setDuration(80)
                                .start()
                )
                .start();
    }

    private void navigateToHome() {
        Intent intent = new Intent(
                MainActivity.this,
                HomeActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}