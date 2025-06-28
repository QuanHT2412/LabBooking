package io.diendanx.labbooking.activities.Register;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    private View rootView;
    private TextView signInTextView;
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
        rootView = findViewById(R.id.main);
        mAuth = FirebaseAuth.getInstance();
        signInTextView = findViewById(R.id.txt_sign_in);
    }

    private void setListeners() {

        // Set up the on click listener for the register button
        registerButton.setOnClickListener(this::onBtnRegisterClick);

        // Add these TextWatchers to validate input in real-time
        ValidatorsUtils.setUpTextWatcher(emailEditText, emailInputLayout, input -> {
            if (input.isEmpty()) {
                return "Email cannot be empty";
            } else if (!ValidatorsUtils.isValidEmail(input)) {
                return "Invalid email format";
            }
            return null; // No error
        });

        ValidatorsUtils.setUpTextWatcher(passwordEditText, passwordInputLayout, input -> {
            if (input.isEmpty()) {
                return "Password cannot be empty";
            } else if (!ValidatorsUtils.isValidPassword(input)) {
                return "Password must:\n• Be 6-20 characters long\n• Contain uppercase letter\n• Contain lowercase letter\n• Contain number";
            }
            return null; // No error
        });

        ValidatorsUtils.setUpTextWatcher(confirmPasswordEditText, confirmPasswordInputLayout, input -> {
            String password = passwordEditText.getText().toString().trim();
            if (input.isEmpty()) {
                return "Confirm password cannot be empty";
            } else if (!input.equals(password)) {
                return "Confirm password does not match";
            }
            return null; // No error
        });

        ValidatorsUtils.setUpTextWatcher(nameEditText, nameInputLayout, input -> {
            if (input.isEmpty()) {
                return "Name cannot be empty";
            }
            return null; // No error
        });

        // Set up the sign-in text view to navigate to SignInActivity
        signInTextView.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, SignInActivity.class));
            finish(); // Optional: close RegisterActivity
        });

        // Set up touch listener to hide keyboard when touching outside input fields
        rootView.setOnTouchListener(this::onRootViewTouch);

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
            registerButton.setVisibility(View.GONE);
        } else {
            progressIndicator.setVisibility(View.GONE);
            registerButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean onRootViewTouch(View view, MotionEvent event) {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
        return false;
    }
}