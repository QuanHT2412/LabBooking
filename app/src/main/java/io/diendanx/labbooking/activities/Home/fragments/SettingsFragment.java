package io.diendanx.labbooking.activities.Home.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.activities.SignIn.SignInActivity;

public class SettingsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private Button btnSignOut;
    private Button btnViewOrders;
    private TextView tvUsername, tvName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        initViews(v);
        setListeners();
        setUserInfo();

        return v;
    }

    private void setListeners() {
        btnSignOut.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(getActivity(), SignInActivity.class));
        });

        btnViewOrders.setOnClickListener(view -> {
            // Launch OrderListActivity to show user's orders
            Intent intent = new Intent(getActivity(), io.diendanx.labbooking.activities.Order.OrderListActivity.class);
            startActivity(intent);
        });
    }

    private void initViews(View v) {
        btnSignOut = v.findViewById(R.id.btn_logout);
        btnViewOrders = v.findViewById(R.id.btn_view_orders);
        tvUsername = v.findViewById(R.id.tv_username);
        tvName = v.findViewById(R.id.tv_name);
        mAuth = FirebaseAuth.getInstance();

    }

    private void setUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            String email = user.getEmail();

            if (displayName != null && !displayName.isEmpty()) {
                tvUsername.setText("Username: " + displayName);
            } else {
                tvUsername.setText("Username: (not set)");
            }

            if (email != null && !email.isEmpty()) {
                tvName.setText("Gmail: " + email);
            } else {
                tvName.setText("Gmail: (not set)");
            }
        } else {
            tvUsername.setText("Username: (not signed in)");
            tvName.setText("Gmail: (not signed in)");
        }
    }
}