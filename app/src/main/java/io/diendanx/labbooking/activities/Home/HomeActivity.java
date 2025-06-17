package io.diendanx.labbooking.activities.Home;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Stack;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.activities.Home.adapters.HomePagerAdapter;

public class HomeActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;
    private final Stack<Integer> fragmentBackStack = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setListeners();

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

    private void initViews() {
        viewPager = findViewById(R.id.fragment_container);
        bottomNav = findViewById(R.id.bottom_navigation);

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
    }

}