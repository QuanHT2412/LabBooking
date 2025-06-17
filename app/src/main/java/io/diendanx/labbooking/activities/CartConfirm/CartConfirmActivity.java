package io.diendanx.labbooking.activities.CartConfirm;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.diendanx.labbooking.Helpers.DatePickerUtils;
import io.diendanx.labbooking.Helpers.TimePickerUtils;
import io.diendanx.labbooking.R;

public class CartConfirmActivity extends AppCompatActivity {
    private TextInputEditText etDate, etTime;
    private AutoCompleteTextView tvDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart_confirm);
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
        etDate = findViewById(R.id.et_date);
        etTime = findViewById(R.id.et_time);
        tvDuration = findViewById(R.id.et_duration);

        DatePickerUtils.setupDatePicker(this, etDate);
        TimePickerUtils.setupTimePicker(this, etTime);

        // set up duration dropdown
        String[] durations = {"30 minutes", "1 hour", "2 hours", "3 hours"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, durations);
        tvDuration.setAdapter(adapter);
        tvDuration.setText(durations[0], false);
    }

    private void setListeners() {

    }



}