package io.diendanx.labbooking.activities.SignIn;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.activities.Register.RegisterActivity;

public class SignInActivity extends AppCompatActivity {

    private Button signInButton, registerButton;

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
    }

    private void setListeners() {
        signInButton.setOnClickListener(v -> {
            // Handle sign-in logic
        });

        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, RegisterActivity.class));
        });
    }
}