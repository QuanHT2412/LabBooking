package io.diendanx.labbooking.activities.SignIn;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
    private View rootView;

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
        rootView = findViewById(R.id.main);
        mAuth = FirebaseAuth.getInstance();
    }

    private void setListeners() {

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

        signInButton.setOnClickListener(this::onSignInButtonClick);
        rootView.setOnTouchListener(this::onRootViewTouch);

        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, RegisterActivity.class));
        });
    }

    private void onSignInButtonClick(View v) {

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Check if fields are validated before proceeding
        if (emailInputLayout.getError() != null ||
                passwordInputLayout.getError() != null) {
            return;
        }

        setProgress(true);
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

                        Toast.makeText(SignInActivity.this, "Sign-in failed",
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

    private boolean onRootViewTouch(View v, MotionEvent event) {
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