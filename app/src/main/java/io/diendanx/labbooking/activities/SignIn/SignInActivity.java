package io.diendanx.labbooking.activities.SignIn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import io.diendanx.labbooking.Helpers.ValidatorsUtils;
import io.diendanx.labbooking.R;
import io.diendanx.labbooking.activities.Home.HomeActivity;
import io.diendanx.labbooking.activities.Register.RegisterActivity;

public class SignInActivity extends AppCompatActivity {

    private Button signInButton, registerButton;
    private TextInputEditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private TextInputLayout emailInputLayout, passwordInputLayout;
    private CircularProgressIndicator progressIndicator;

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
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setListeners();
    }

    private void initViews() {
        signInButton = findViewById(R.id.btn_signin);
        registerButton = findViewById(R.id.btn_signup);
        emailEditText = findViewById(R.id.et_email_signin);
        passwordEditText = findViewById(R.id.et_password_signin);
        emailInputLayout = findViewById(R.id.et_email_wrapper_signin);
        passwordInputLayout = findViewById(R.id.et_password_wrapper_signin);
        progressIndicator = findViewById(R.id.progress_indicator_signin);
        mAuth = FirebaseAuth.getInstance();
    }

    private void setListeners() {

        // Validate email and password inputs
        emailEditText.setOnFocusChangeListener(this::emailValidate);
        passwordEditText.setOnFocusChangeListener(this::passwordValidate);

        signInButton.setOnClickListener(this::onSignInButtonClick);

        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, RegisterActivity.class));
        });
    }

    private void onSignInButtonClick(View v) {

        // Check if fields are validated before proceeding
        if (emailInputLayout.getError() != null ||
                passwordInputLayout.getError() != null) {
            return;
        }

        setProgress(true);
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Check if any field is empty and show error if so
        if (email.isEmpty() || password.isEmpty()) {
            setProgress(false);
            if (email.isEmpty()) {
                emailInputLayout.setError("Email cannot be empty");
            }
            if (password.isEmpty()) {
                passwordInputLayout.setError("Password cannot be empty");
            }
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(SignInActivity.this, HomeActivity.class));
                    } else {
                        setProgress(false);
                        clearInputFields();

                        Toast.makeText(SignInActivity.this, "Sign-in failed: ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearInputFields() {
        emailEditText.setText("");
        passwordEditText.setText("");
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);
    }

    private void setProgress(boolean isLoading) {
        if (isLoading) {
            progressIndicator.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.INVISIBLE);
        } else {
            progressIndicator.setVisibility(View.GONE);
            signInButton.setVisibility(View.VISIBLE);
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
}