package io.diendanx.labbooking.activities.OrderConfirmation;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.models.Cart;
import io.diendanx.labbooking.models.Order;
import io.diendanx.labbooking.models.OrderItem;
import io.diendanx.labbooking.models.Package;
import io.diendanx.labbooking.repository.BaseRepository;
import io.diendanx.labbooking.repository.CartRepository;
import io.diendanx.labbooking.repository.OrdersItemRepository;
import io.diendanx.labbooking.repository.OrdersRepository;
import io.diendanx.labbooking.repository.PackageRepository;

public class OrderConfirmationActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private TextInputEditText etName, etEmail, etPhone;
    private Button btnConfirm;
    private FirebaseAuth mAuth;
    private CartRepository cartRepository;
    private PackageRepository packageRepository;
    private OrdersRepository orderRepository;
    private OrdersItemRepository orderItemRepository;
    private int countOrderItems = 0;
    private List<Package> packages = new ArrayList<>();
    private TableLayout tableOrderSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_confirmation);
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

        // get all packages from firebase
        packageRepository.getAll(new BaseRepository.ListCallback<Package>() {
            @Override
            public void onSuccess(List<Package> items) {
                packages.addAll(items);
            }

            @Override
            public void onFailure(Exception e) {
                // Handle failure
            }
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.topAppBar);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        btnConfirm = findViewById(R.id.btn_confirm);
        tableOrderSummary = findViewById(R.id.table_order_summary);
        mAuth = FirebaseAuth.getInstance();
        cartRepository = new CartRepository();
        packageRepository = new PackageRepository();
        orderRepository = new OrdersRepository();
        orderItemRepository = new OrdersItemRepository();

        etEmail.setText(mAuth.getCurrentUser().getEmail());
    }

    private void setListeners() {
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        btnConfirm.setOnClickListener(v -> btnConfirmClicked());
    }

    private void btnConfirmClicked() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            return;
        }

        // get the cart from firebase
        String userId = mAuth.getCurrentUser().getUid();
        cartRepository.countCartItems(userId, new BaseRepository.ListCallback<Cart>() {
            @Override
            public void onSuccess(List<Cart> items) {
                createOrder(items, name, email, phone);
                countOrderItems = items.size();
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

        // For example, save the order details to the database or send a confirmation email

    }

    private void createOrder(List<Cart> items, String name, String email, String phone) {
        // create order
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setUserId(mAuth.getCurrentUser().getUid());
        order.setUsername(name);
        order.setPhone(phone);
        order.setEmail(email);
        order.setCreated_at(com.google.firebase.Timestamp.now());
        order.setOrderItems(new ArrayList<>());


        long totalPrice = 0;
        // create order items
        List<OrderItem> orderItems = new ArrayList<>();

        for (Cart cart : items) {
            OrderItem orderItem = new OrderItem();
            orderItem.setId(UUID.randomUUID().toString());
            orderItem.setOrderId(order.getId());
            orderItem.setPackageId(cart.getPackageId());
            orderItem.setDuration(cart.getDuration());
            orderItem.setStartDate(cart.getStartDate());
            orderItem.setSelectedFacilities(cart.getSelectedFacilities());
            for (Package pkg : packages) {
                if (pkg.getId().equals(cart.getPackageId())) {
                    orderItem.setPackageName(pkg.getName());
                    orderItem.setPackagePrice(pkg.getPrice());
                    break;
                }
            }
            orderItems.add(orderItem);
            order.getOrderItems().add(orderItem.getId());
            totalPrice += cart.getTotalPrice();
        }

        order.setTotalPrice(totalPrice);

        // add order to firebase
        orderRepository.add(order.getId(), order, new BaseRepository.ItemCallback<Order>() {
            @Override
            public void onSuccess(Order item) {
                // add order items to firebase
                addOrderItems(orderItems);
            }

            @Override
            public void onFailure(Exception e) {
                // Handle failure to create order
            }
        });
        // Clear the cart after order is created
        clearCart(items);

    }

    private void addOrderItems(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            orderItemRepository.add(orderItem.getId(), orderItem, new BaseRepository.ItemCallback<OrderItem>() {
                @Override
                public void onSuccess(OrderItem item) {
                    countOrderItems--;
                    if (countOrderItems == 0) {
                        Toast.makeText(OrderConfirmationActivity.this, "Order placed successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity after successful order placement
                    }
                }

                @Override
                public void onFailure(Exception e) {
                }
            });
        }
    }

    private void clearCart(List<Cart> carts) {
        String userId = mAuth.getCurrentUser().getUid();
        for (Cart cart : carts) {
            cartRepository.delete(cart.getId(), new BaseRepository.ItemCallback<Cart>() {
                @Override
                public void onSuccess(Cart item) {
                    // Cart item deleted successfully
                }

                @Override
                public void onFailure(Exception e) {
                    // Handle failure to delete cart item
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrderSummary();
    }

    private void loadOrderSummary() {
        String userId = mAuth.getCurrentUser().getUid();
        cartRepository.countCartItems(userId, new BaseRepository.ListCallback<Cart>() {
            @Override
            public void onSuccess(List<Cart> items) {
                displayOrderSummary(items);
            }
            @Override
            public void onFailure(Exception e) {
                // Handle error
            }
        });
    }

    private void displayOrderSummary(List<Cart> items) {
        tableOrderSummary.removeAllViews();
        Context context = this;
        // Header row
        TableRow headerRow = new TableRow(context);
        // Icon header
        TextView tvHeaderIcon = new TextView(context);
        tvHeaderIcon.setText("");
        tvHeaderIcon.setPadding(0, 0, 24, 0);
        headerRow.addView(tvHeaderIcon);
        // Package name
        TextView tvHeader1 = new TextView(context);
        tvHeader1.setText("Package");
        tvHeader1.setPadding(0, 0, 16, 0);
        tvHeader1.setTextColor(ContextCompat.getColor(context, R.color.main_900));
        tvHeader1.setTypeface(tvHeader1.getTypeface(), android.graphics.Typeface.BOLD);
        // Date
        TextView tvHeader2 = new TextView(context);
        tvHeader2.setText("Date");
        tvHeader2.setPadding(0, 0, 16, 0);
        tvHeader2.setTextColor(ContextCompat.getColor(context, R.color.main_900));
        tvHeader2.setTypeface(tvHeader2.getTypeface(), android.graphics.Typeface.BOLD);
        // Time
        TextView tvHeader3 = new TextView(context);
        tvHeader3.setText("Time");
        tvHeader3.setPadding(0, 0, 16, 0);
        tvHeader3.setTextColor(ContextCompat.getColor(context, R.color.main_900));
        tvHeader3.setTypeface(tvHeader3.getTypeface(), android.graphics.Typeface.BOLD);
        // Duration
        TextView tvHeader4 = new TextView(context);
        tvHeader4.setText("Duration");
        tvHeader4.setPadding(0, 0, 16, 0);
        tvHeader4.setTextColor(ContextCompat.getColor(context, R.color.main_900));
        tvHeader4.setTypeface(tvHeader4.getTypeface(), android.graphics.Typeface.BOLD);
        headerRow.addView(tvHeader1);
        headerRow.addView(tvHeader2);
        headerRow.addView(tvHeader3);
        headerRow.addView(tvHeader4);
        tableOrderSummary.addView(headerRow);

        long totalPrice = 0;
        boolean alternate = false;
        for (Cart cart : items) {
            String packageName = "";
            String date = "-";
            String startTime = "-";
            String durationStr = "-";
            int iconRes = R.drawable.ic_package;
            if (cart.getStartDate() != null && cart.getDuration() > 0) {
                java.util.Date start = cart.getStartDate().toDate();
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
                java.text.SimpleDateFormat hourFormat = new java.text.SimpleDateFormat("HH:mm");
                date = dateFormat.format(start);
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(start);
                startTime = hourFormat.format(cal.getTime());
                durationStr = String.format("%.2f", (double)cart.getDuration()/60);
            }
            for (Package pkg : packages) {
                if (pkg.getId().equals(cart.getPackageId())) {
                    packageName = pkg.getName();
                    if (packageName.endsWith(" Package")) {
                        packageName = packageName.replace(" Package", "");
                    }
                    break;
                }
            }
            TableRow row = new TableRow(context);
            if (alternate) {
                row.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_table_row_alt));
            }
            // Icon
            TextView tvIcon = new TextView(context);
            tvIcon.setPadding(0, 0, 24, 0);
            tvIcon.setText("\uD83D\uDCE6"); // 📦 fallback icon
            tvIcon.setTextSize(20);
            row.addView(tvIcon);
            // Package name
            TextView tvName = new TextView(context);
            tvName.setText(packageName);
            tvName.setPadding(0, 0, 16, 0);
            tvName.setTextColor(ContextCompat.getColor(context, R.color.main_900));
            tvName.setTextSize(16);
            row.addView(tvName);
            // Date
            TextView tvDate = new TextView(context);
            tvDate.setText(date);
            tvDate.setPadding(0, 0, 16, 0);
            tvDate.setTextColor(ContextCompat.getColor(context, R.color.main_700));
            tvDate.setTextSize(15);
            row.addView(tvDate);
            // Start Time
            TextView tvTime = new TextView(context);
            tvTime.setText(startTime);
            tvTime.setPadding(0, 0, 16, 0);
            tvTime.setTextColor(ContextCompat.getColor(context, R.color.main_700));
            tvTime.setTextSize(15);
            row.addView(tvTime);
            // Duration (hr)
            TextView tvDuration = new TextView(context);
            tvDuration.setText(durationStr + " hr");
            tvDuration.setPadding(0, 0, 16, 0);
            tvDuration.setTextColor(ContextCompat.getColor(context, R.color.main_700));
            tvDuration.setTextSize(15);
            row.addView(tvDuration);
            tableOrderSummary.addView(row);
            totalPrice += cart.getTotalPrice();
            alternate = !alternate;
        }
        // Total row
        TableRow totalRow = new TableRow(context);
        TextView tvTotalLabel = new TextView(context);
        tvTotalLabel.setText("Total");
        tvTotalLabel.setPadding(0, 20, 16, 0);
        tvTotalLabel.setTextColor(ContextCompat.getColor(context, R.color.main_900));
        tvTotalLabel.setTypeface(tvTotalLabel.getTypeface(), android.graphics.Typeface.BOLD);
        tvTotalLabel.setTextSize(16);
        // Add empty cells for alignment
        TextView emptyCell1 = new TextView(context);
        TextView emptyCell2 = new TextView(context);
        TextView emptyCell3 = new TextView(context);
        TextView tvTotalValue = new TextView(context);
        tvTotalValue.setText(String.format("%,d VND", totalPrice));
        tvTotalValue.setPadding(0, 20, 0, 0); // Remove right padding to keep it left-aligned
        tvTotalValue.setGravity(android.view.Gravity.START);
        tvTotalValue.setTextColor(ContextCompat.getColor(context, R.color.main_900));
        tvTotalValue.setTypeface(tvTotalValue.getTypeface(), android.graphics.Typeface.BOLD);
        tvTotalValue.setTextSize(14); // smaller size for total amount
        totalRow.addView(tvTotalLabel);
        totalRow.addView(emptyCell1);
        totalRow.addView(emptyCell2);
        totalRow.addView(emptyCell3);
        totalRow.addView(tvTotalValue);
        tableOrderSummary.addView(totalRow);
    }
}