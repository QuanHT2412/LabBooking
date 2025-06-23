package io.diendanx.labbooking.activities.Register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

import io.diendanx.labbooking.Helpers.ValidatorsUtils;
import io.diendanx.labbooking.R;
import io.diendanx.labbooking.activities.Home.HomeActivity;
import io.diendanx.labbooking.activities.SignIn.SignInActivity;

public class RegisterActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private Button registerButton;
    private TextInputEditText emailEditText, passwordEditText, confirmPasswordEditText, nameEditText;
    private FirebaseAuth mAuth;
    private CircularProgressIndicator progressIndicator;
    private TextInputLayout emailInputLayout, passwordInputLayout, confirmPasswordInputLayout, nameInputLayout;

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        // Set status bar color to main_900
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.main_900));
        // Set status bar text color to white
        getWindow().getDecorView().setSystemUiVisibility(
                getWindow().getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.topAppBar);
        registerButton = findViewById(R.id.btn_register);
        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_password);
        confirmPasswordEditText = findViewById(R.id.et_confirm_password);
        nameEditText = findViewById(R.id.et_name);
        emailInputLayout = findViewById(R.id.et_email_wrapper);
        passwordInputLayout = findViewById(R.id.et_password_wrapper);
        confirmPasswordInputLayout = findViewById(R.id.et_confirm_password_wrapper);
        nameInputLayout = findViewById(R.id.et_name_wrapper);
        progressIndicator = findViewById(R.id.progress_indicator_register);
        mAuth = FirebaseAuth.getInstance();
    }

    private void setListeners() {
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        registerButton.setOnClickListener(this::onBtnRegisterClick);

        // Set focus change listeners to validate input fields
        emailEditText.setOnFocusChangeListener(this::emailValidate);
        passwordEditText.setOnFocusChangeListener(this::passwordValidate);
        confirmPasswordEditText.setOnFocusChangeListener(this::confirmPasswordValidate);
        nameEditText.setOnFocusChangeListener(this::nameValidate);
    }

    private void onBtnRegisterClick(View view) {

        // Check if fields are validated before proceeding
        if (emailInputLayout.getError() != null ||
                passwordInputLayout.getError() != null ||
                confirmPasswordInputLayout.getError() != null) {
            return;
        }

        setProgress(true);
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();

        // Check if any field is empty and show error if so
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()) {
            setProgress(false);
            if (email.isEmpty()) {
                emailInputLayout.setError("Email cannot be empty");
            }
            if (password.isEmpty()) {
                passwordInputLayout.setError("Password cannot be empty");
            }
            if (confirmPassword.isEmpty()) {
                confirmPasswordInputLayout.setError("Confirm password cannot be empty");
            }
            if (name.isEmpty()) {
                nameInputLayout.setError("Name cannot be empty");
            }
            return;
        }

        // Handle registration logic here, e.g., using FirebaseAuth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Set display name
                        mAuth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build());
                        // Registration successful, navigate to HomeActivity or another activity
                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                    } else {
                        setProgress(false);

                        clearInputFields();

                        Toast.makeText(RegisterActivity.this, "Register failed: ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearInputFields() {
        emailEditText.setText("");
        passwordEditText.setText("");
        confirmPasswordEditText.setText("");
        nameEditText.setText("");
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);
        confirmPasswordInputLayout.setError(null);
        nameInputLayout.setError(null);
    }

    private void setProgress(boolean isLoading) {
        if (isLoading) {
            progressIndicator.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.INVISIBLE);
        } else {
            progressIndicator.setVisibility(View.GONE);
            registerButton.setVisibility(View.VISIBLE);
        }
    }

    private void emailValidate(View v, boolean hasFocus) {
        if (!hasFocus) {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                emailInputLayout.setError("Email cannot be empty");
            } else if (!ValidatorsUtils.isValidEmail(email)) {
                emailInputLayout.setError("Invalid email format");
            } else {
                emailInputLayout.setError(null);
            }
        }
    }

    private void passwordValidate(View v, boolean hasFocus) {
        if (!hasFocus) {
            String password = passwordEditText.getText().toString().trim();
            if (password.isEmpty()) {
                passwordInputLayout.setError("Password cannot be empty");
            } else if (!ValidatorsUtils.isValidPassword(password)) {
                String error = "Password must:\n• Be 6–20 characters long\n• Contain uppercase letter\n• Contain lowercase letter\n• Contain number";
                passwordInputLayout.setError(error);
            } else {
                passwordInputLayout.setError(null);
            }
        }
    }

    private void confirmPasswordValidate(View v, boolean hasFocus) {
        if (!hasFocus) {
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if (confirmPassword.isEmpty()) {
                confirmPasswordInputLayout.setError("Confirm password cannot be empty");
            } else if (!confirmPassword.equals(password)) {
                confirmPasswordInputLayout.setError("Confirm password does not match");
            } else {
                confirmPasswordInputLayout.setError(null);
            }
        }
    }

    private void nameValidate(View v, boolean hasFocus) {
        if (!hasFocus) {
            String name = nameEditText.getText().toString().trim();
            if (name.isEmpty()) {
                nameInputLayout.setError("Name cannot be empty");
            } else if (name.length() < 2 || name.length() > 50) {
                nameInputLayout.setError("Name must be between 2 and 50 characters");
            } else {
                nameInputLayout.setError(null);
            }
        }
    }
}