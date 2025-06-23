package io.diendanx.labbooking.activities.Home;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Stack;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.activities.Cart.CartActivity;
import io.diendanx.labbooking.activities.Home.adapters.HomePagerAdapter;
import io.diendanx.labbooking.activities.SignIn.SignInActivity;
import io.diendanx.labbooking.services.CartNotificationService;

public class HomeActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;
    private MaterialToolbar topAppBar;
    private TextView tvCartBadge;
    private CartNotificationService cartNotificationService;
    private final Stack<Integer> fragmentBackStack = new Stack<>();
    private final int REQUEST_NOTIFICATION_PERMISSION = 100;
    private int cartItemCount = 0;
    private Button btnSignOut;
    private FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
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
        handleBackPress();
        cartItemCount = countCartItems();
        cartNotificationService = new CartNotificationService(this);
        tryShowCartNotification();

    }

    private void initViews() {
        viewPager = findViewById(R.id.fragment_container);
        bottomNav = findViewById(R.id.bottom_navigation);
        topAppBar = findViewById(R.id.top_app_bar);
        tvCartBadge = findViewById(R.id.tv_cart_badge);
        mAuth = FirebaseAuth.getInstance();

        // set adapter for viewPager
        viewPager.setAdapter(new HomePagerAdapter(this));
        viewPager.setUserInputEnabled(false); // Disable swipe gestures
    }

    private void setListeners() {

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            int newPosition = -1;
            if (itemId == R.id.nav_home) {
                newPosition = 0;
            } else if (itemId == R.id.nav_chat) {
                newPosition = 1;
            } else if (itemId == R.id.nav_settings) {
                newPosition = 2;
            }
            if (newPosition != -1 && viewPager.getCurrentItem() != newPosition) {
                fragmentBackStack.push(viewPager.getCurrentItem());
                viewPager.setCurrentItem(newPosition, true);
                return true;
            }
            return false;
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        bottomNav.setSelectedItemId(R.id.nav_home);
                        break;
                    case 1:
                        bottomNav.setSelectedItemId(R.id.nav_chat);
                        break;
                    case 2:
                        bottomNav.setSelectedItemId(R.id.nav_settings);
                        break;
                }
            }
        });

        topAppBar.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.action_cart) {
                startActivity(new Intent(this, CartActivity.class));
            }
            return false;
        });
    }

    private void handleBackPress() {
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!fragmentBackStack.isEmpty()) {
                    int lastPosition = fragmentBackStack.pop();
                    viewPager.setCurrentItem(lastPosition, true);
                    bottomNav.setSelectedItemId(lastPosition == 0 ?
                            R.id.nav_home :
                            lastPosition == 1 ?
                                    R.id.nav_chat : R.id.nav_settings
                    );
                } else {
                    finishAffinity();
                }
            }
        });
    }

    private void tryShowCartNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATION_PERMISSION
                );
            } else {
                showCartNotification();
            }
        } else {
            showCartNotification();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            showCartNotification();
        }
    }

    private void showCartNotification() {
        if (cartItemCount > 0) {
            cartNotificationService.showNotification(
                    "Items in Cart",
                    "You have " + countCartItems() + " items waiting in your cart"
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    private void updateCartBadge() {
        
        int count = countCartItems();

        
        if (count > 0) {
            tvCartBadge.setVisibility(View.VISIBLE);
            tvCartBadge.setText(String.valueOf(count));
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    private int countCartItems() {
        return 5;
    }

}
