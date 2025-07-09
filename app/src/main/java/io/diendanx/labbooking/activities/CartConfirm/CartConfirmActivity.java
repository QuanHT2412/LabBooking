package io.diendanx.labbooking.activities.CartConfirm;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.sql.Time;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.diendanx.labbooking.Helpers.DatePickerUtils;
import io.diendanx.labbooking.Helpers.TimePickerUtils;
import io.diendanx.labbooking.R;
import io.diendanx.labbooking.activities.CartConfirm.adapters.FacilityAdapter;
import io.diendanx.labbooking.models.Cart;
import io.diendanx.labbooking.models.Facility;
import io.diendanx.labbooking.repository.BaseRepository;
import io.diendanx.labbooking.repository.CartRepository;
import io.diendanx.labbooking.repository.FacilityRepository;

public class CartConfirmActivity extends AppCompatActivity {
    private TextInputEditText etDate, etStartTime, etEndTime;
    private RecyclerView facilityRecyclerView;
    private FacilityRepository facilityRepository;
    private CartRepository cartRepository;
    private Button btnAddToCart;
    private String packageId;
    private FirebaseAuth mAuth;
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
        // get packageId from intent
        packageId = getIntent().getStringExtra("package_id");
        initViews();
        setListeners();
    }

    private void initViews() {
        etDate = findViewById(R.id.et_date);
        etStartTime = findViewById(R.id.et_starttime);
        etEndTime = findViewById(R.id.et_endtime);
        facilityRecyclerView = findViewById(R.id.facilityRecyclerView);
        btnAddToCart = findViewById(R.id.btn_add);
        mAuth = FirebaseAuth.getInstance();
        facilityRepository = new FacilityRepository();
        cartRepository = new CartRepository();

        DatePickerUtils.setupDatePicker(this, etDate);
        TimePickerUtils.setupTimePicker(this, etStartTime);
        TimePickerUtils.setupTimePicker(this, etEndTime);

        // Setup facilities checkboxes
        facilityRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        facilityRepository.getAll(new BaseRepository.ListCallback<Facility>() {
            @Override
            public void onSuccess(List<Facility> items) {
                facilityRecyclerView.setAdapter(new FacilityAdapter(items));
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    private void setListeners() {
        // Setup toolbar back navigation
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        btnAddToCart.setOnClickListener(v -> handleBtnAddToCartClicked());
    }

    private void handleBtnAddToCartClicked() {
        // Validate inputs
        String date = etDate.getText().toString();
        String startTime = etStartTime.getText().toString();
        String endTime = etEndTime.getText().toString();

        if (date.isEmpty()) {
            etDate.setError("Please select a date");
            return;
        }
        if (startTime.isEmpty()) {
            etStartTime.setError("Please select a start time");
            return;
        }
        if (endTime.isEmpty()) {
            etEndTime.setError("Please select an end time");
            return;
        }

        // Get selected facilities
        FacilityAdapter adapter = (FacilityAdapter) facilityRecyclerView.getAdapter();
        Set<Facility> selectedFacilities = adapter != null ? adapter.getSelectedFacilities() : null;

        // create cart object
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID().toString());
        cart.setPackageId(packageId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime localDatetime = LocalDateTime.parse(date + " " + startTime, formatter);
        java.util.Date utilDate = java.util.Date.from(localDatetime.atZone(ZoneId.systemDefault()).toInstant());
        Timestamp dateTimestamp = new Timestamp(utilDate);
        cart.setStartDate(dateTimestamp);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime t1 = LocalTime.parse(startTime, timeFormatter);
        LocalTime t2 = LocalTime.parse(endTime, timeFormatter);

        long minutesDifference = ChronoUnit.MINUTES.between(t1, t2);
        cart.setDuration((int) minutesDifference);
        cart.setUserId(mAuth.getCurrentUser().getUid());

        if (selectedFacilities != null && !selectedFacilities.isEmpty()) {
            List<String> facilityIds = selectedFacilities.stream()
                    .map(Facility::getId)
                    .collect(Collectors.toList());
            cart.setSelectedFacilities(facilityIds);
        }

        long totalPrice = 0;

        String packagePriceStr = getIntent().getStringExtra("package_price");
        long packagePrice = Long.parseLong(packagePriceStr.replaceAll("\\D", ""));

        totalPrice += packagePrice * (long)(Math.ceil((double) cart.getDuration() / 60));

        if (selectedFacilities != null && !selectedFacilities.isEmpty()) {
            for (Facility facility : selectedFacilities) {
                totalPrice += (long)facility.getPrice();
            }
        }

        cart.setTotalPrice(totalPrice);

        cartRepository.add(cart.getId(), cart, new BaseRepository.ItemCallback<Cart>() {
            @Override
            public void onSuccess(Cart item) {
                Toast.makeText(getApplicationContext(), "Added to cart successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to add to cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


}