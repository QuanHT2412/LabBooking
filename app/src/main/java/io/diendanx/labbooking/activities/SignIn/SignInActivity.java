package io.diendanx.labbooking.activities.SignIn;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.activities.Home.HomeActivity;
import io.diendanx.labbooking.activities.Register.RegisterActivity;

public class SignInActivity extends AppCompatActivity {

    private Button signInButton, registerButton;
    private TextInputEditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;

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
        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_password);
        mAuth = FirebaseAuth.getInstance();
    }

    private void setListeners() {
        signInButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                // Show error message
                return;
            }

            // Handle sign-in logic here, e.g., using FirebaseAuth
             mAuth.signInWithEmailAndPassword(email, password)
                     .addOnCompleteListener(task -> {
                         if (task.isSuccessful()) {
                             startActivity(new Intent(SignInActivity.this, HomeActivity.class));
                             finish();
                         } else {
                             // Show error message
                         }
                     });
        });

        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, RegisterActivity.class));
        });
    }
}