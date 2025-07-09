package io.diendanx.labbooking.activities.OrderConfirmation;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
import io.diendanx.labbooking.repository.OrderItemRepository;
import io.diendanx.labbooking.repository.OrderRepository;
import io.diendanx.labbooking.repository.PackageRepository;

public class OrderConfirmationActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private TextInputEditText etName, etEmail, etPhone;
    private Button btnConfirm;
    private FirebaseAuth mAuth;
    private CartRepository cartRepository;
    private PackageRepository packageRepository;
    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private int countOrderItems = 0;
    private List<Package> packages = new ArrayList<>();

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
        mAuth = FirebaseAuth.getInstance();
        cartRepository = new CartRepository();
        packageRepository = new PackageRepository();
        orderRepository = new OrderRepository();
        orderItemRepository = new OrderItemRepository();

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
}