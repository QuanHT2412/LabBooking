package io.diendanx.labbooking.activities.Home.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.activities.SignIn.SignInActivity;

public class SettingsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private Button btnSignOut;

    public SettingsFragment() {
        // Required empty public constructor
    }

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

        // Inflate the layout for this fragment
        return v;
    }

    private void setListeners() {
        btnSignOut.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(getActivity(), SignInActivity.class));
        });
    }

    private void initViews(View v) {
        btnSignOut = v.findViewById(R.id.btn_logout);
        mAuth = FirebaseAuth.getInstance();
    }
}