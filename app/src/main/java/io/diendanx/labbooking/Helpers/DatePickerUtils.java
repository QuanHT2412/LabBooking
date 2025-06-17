package io.diendanx.labbooking.Helpers;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.diendanx.labbooking.R;

public class DatePickerUtils {

    public static void setupDatePicker(Context context, TextInputEditText editText) {
        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            editText.setText(dateFormat.format(calendar.getTime()));
        };

        editText.setOnClickListener(v -> {
            new DatePickerDialog(
                    context,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH))
                    .show();
        });
    }
}
