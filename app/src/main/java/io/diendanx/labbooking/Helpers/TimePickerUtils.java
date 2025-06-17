package io.diendanx.labbooking.Helpers;

import android.app.TimePickerDialog;
import android.content.Context;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimePickerUtils {
    public static void setupTimePicker(Context context, TextInputEditText editText) {
        final Calendar calendar = Calendar.getInstance();
        final TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            editText.setText(timeFormat.format(calendar.getTime()));
        };

        editText.setOnClickListener(v -> {
            new TimePickerDialog(
                    context,
                    timeSetListener,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true)
                    .show();
        });
    }
}
