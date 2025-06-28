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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import io.diendanx.labbooking.Helpers.ValidatorsUtils;
import io.diendanx.labbooking.R;
import io.diendanx.labbooking.activities.Home.HomeActivity;
import io.diendanx.labbooking.activities.MainActivity;
import io.diendanx.labbooking.activities.Register.RegisterActivity;
import io.diendanx.labbooking.models.Cart;

public class SignInActivity extends AppCompatActivity {

    private Button signInButton;
    private TextView registerTv;
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

        FirebaseFirestore.getInstance().collection("cart")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            var cartId = document.getId();

                            CollectionReference facilityCollection = document.getReference().collection("facility");

                            facilityCollection.get().addOnCompleteListener(facilityTask -> {
                                if (facilityTask.isSuccessful()) {
                                    for (QueryDocumentSnapshot facilityDoc : facilityTask.getResult()) {
                                        String facilityId = facilityDoc.getId();
                                        // Process each facility document as needed
                                        Toast.makeText(SignInActivity.this, "Facility ID: " + facilityId, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(SignInActivity.this, "Failed to load facilities.", Toast.LENGTH_SHORT).show();
                                }
                            });

//                            var cartItem = document.toObject(Cart.class);
//                            Toast.makeText(SignInActivity.this, "Cart item loaded: " + cartItem.getId(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignInActivity.this, "Failed to load cart items.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initViews() {
        signInButton = findViewById(R.id.btn_signin);
        registerTv = findViewById(R.id.txt_sign_up);
        emailEditText = findViewById(R.id.et_email_signin);
        passwordEditText = findViewById(R.id.et_password_signin);
        emailInputLayout = findViewById(R.id.et_email_wrapper_signin);
        passwordInputLayout = findViewById(R.id.et_password_wrapper_signin);
        progressIndicator = findViewById(R.id.progress_indicator_signin);
        rootView = findViewById(R.id.main);
        mAuth = FirebaseAuth.getInstance();
    }

    private void setListeners() {

        // Set up text watchers for validation
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

        // Set up the on click listener for the sign-in button
        signInButton.setOnClickListener(this::onSignInButtonClick);

        // Set up the on touch listener for the root view to hide keyboard when touching outside input fields
        rootView.setOnTouchListener(this::onRootViewTouch);

        // Set up the on click listener for the register text view
        registerTv.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, RegisterActivity.class));
            finish();
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

                        Toast.makeText(SignInActivity.this, "Email or password is incorrect",
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
            signInButton.setVisibility(View.GONE);
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