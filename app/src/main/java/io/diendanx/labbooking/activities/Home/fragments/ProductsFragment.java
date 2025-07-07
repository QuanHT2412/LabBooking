package io.diendanx.labbooking.activities.Home.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.diendanx.labbooking.Helpers.ImageGetterUtils;
import io.diendanx.labbooking.R;
import io.diendanx.labbooking.activities.ProductDetail.ProductDetailActivity;
import io.diendanx.labbooking.activities.Home.adapters.ImageSliderAdapter;
import io.diendanx.labbooking.activities.Home.adapters.ProductAdapter;
import io.diendanx.labbooking.models.Package;

public class ProductsFragment extends Fragment {

    private RecyclerView rvProducts;
    private TabLayout tabLayout;
    private Spinner spinnerPriceRange;
    private Button btnApplyFilter, btnClearFilter;
    private ProgressBar progressBar;
    private View layoutEmptyState;
    private TextView tvSectionTitle, tvPackageCount;
    private ViewPager2 viewPagerSlider;
    private LinearLayout dotsIndicator;

    private ProductAdapter adapter;
    private ImageSliderAdapter sliderAdapter;
    private List<Package> allPackages = new ArrayList<>();
    private List<Package> displayedPackages = new ArrayList<>();
    private List<Package> popularPackages = new ArrayList<>();

    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;

    private String[] priceRanges = {
            "All Prices",
            "Under 50,000 VND",
            "50,000 - 100,000 VND",
            "100,000 - 200,000 VND",
            "200,000 - 300,000 VND",
            "300,000 - 400,000 VND",
            "Above 500,000 VND"
    };

    private int[] sliderImages = {
            R.drawable.slide1,
            R.drawable.slide2,
            R.drawable.slide3,
            R.drawable.slide4
    };

    private int selectedPriceRange = 0;
    private int currentTab = 0; // 0 = Popular, 1 = All

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);

        try {
            initViews(view);
            setupImageSlider();
            setupPriceFilter();
            setupTabs();
            setupRecyclerView();
            fetchPackagesFromFirebase();
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback layout if encountered an error
            return createFallbackView(inflater, container);
        }

        return view;
    }

    private View createFallbackView(LayoutInflater inflater, ViewGroup container) {
        // Fallback to layout
        View view = inflater.inflate(R.layout.fragment_products, container, false);
        RecyclerView rv = view.findViewById(R.id.rv_products);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new ProductAdapter(allPackages);
            rv.setAdapter(adapter);
        }
        fetchPackagesFromFirebase();
        return view;
    }

    private void initViews(View view) {
        rvProducts = view.findViewById(R.id.rv_products);
        tabLayout = view.findViewById(R.id.tab_layout);
        spinnerPriceRange = view.findViewById(R.id.spinner_price_range);
        btnApplyFilter = view.findViewById(R.id.btn_apply_filter);
        btnClearFilter = view.findViewById(R.id.btn_clear_filter);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        tvSectionTitle = view.findViewById(R.id.tv_section_title);
        tvPackageCount = view.findViewById(R.id.tv_package_count);
        viewPagerSlider = view.findViewById(R.id.view_page_slider);
        dotsIndicator = view.findViewById(R.id.dots_indicator);
    }

    private void setupImageSlider() {
        if (viewPagerSlider == null) return;

        sliderAdapter = new ImageSliderAdapter(getContext(), sliderImages);
        viewPagerSlider.setAdapter(sliderAdapter);

        setupCompactDotsIndicator();
        setupSliderCallback();
        startAutoSlide();
    }

    private void setupCompactDotsIndicator() {
        if (dotsIndicator == null) return;

        dotsIndicator.removeAllViews();
        ImageView[] dots = new ImageView[sliderImages.length];

        for (int i = 0; i < sliderImages.length; i++) {
            dots[i] = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(6), dpToPx(6)
            );
            params.setMargins(dpToPx(3), 0, dpToPx(3), 0);
            dots[i].setLayoutParams(params);
            dots[i].setImageResource(R.drawable.dot_inactive);
            dotsIndicator.addView(dots[i]);
        }

        if (dots.length > 0) {
            dots[0].setImageResource(R.drawable.dot_active);
        }
    }

    private void setupSliderCallback() {
        if (viewPagerSlider == null) return;

        viewPagerSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateCompactDotsIndicator(position);
                resetAutoSlide();
            }
        });
    }

    private void updateCompactDotsIndicator(int position) {
        if (dotsIndicator == null) return;

        for (int i = 0; i < dotsIndicator.getChildCount(); i++) {
            ImageView dot = (ImageView) dotsIndicator.getChildAt(i);
            if (i == position) {
                dot.setImageResource(R.drawable.dot_active);
            } else {
                dot.setImageResource(R.drawable.dot_inactive);
            }
        }
    }

    private void startAutoSlide() {
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                if (viewPagerSlider != null && sliderAdapter != null) {
                    int currentItem = viewPagerSlider.getCurrentItem();
                    int nextItem = (currentItem + 1) % sliderAdapter.getItemCount();
                    viewPagerSlider.setCurrentItem(nextItem, true);
                    sliderHandler.postDelayed(this, 5000); // Auto slide every 5 seconds
                }
            }
        };
        sliderHandler.postDelayed(sliderRunnable, 5000);
    }

    private void resetAutoSlide() {
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
            startAutoSlide();
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void setupPriceFilter() {
        if (spinnerPriceRange == null) return;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                priceRanges
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriceRange.setAdapter(adapter);

        if (btnApplyFilter != null) {
            btnApplyFilter.setOnClickListener(v -> applyPriceFilter());
        }
        if (btnClearFilter != null) {
            btnClearFilter.setOnClickListener(v -> clearPriceFilter());
        }
    }

    private void setupTabs() {
        if (tabLayout == null) return;

        tabLayout.addTab(tabLayout.newTab().setText("Popular Packages"));
        tabLayout.addTab(tabLayout.newTab().setText("All Packages"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                updateDisplayedPackages();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        if (rvProducts == null) return;

        rvProducts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        adapter = new ProductAdapter(displayedPackages);
        rvProducts.setAdapter(adapter);
    }

    private void fetchPackagesFromFirebase() {
        showLoading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("package")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allPackages.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Package p = doc.toObject(Package.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            allPackages.add(p);
                        }
                    }

                    processPackages();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showEmptyState(true);
                });
    }

    private void processPackages() {
        // Create popular packages (top 3 by price)
        popularPackages.clear();
        List<Package> sortedPackages = new ArrayList<>(allPackages);

        // Sort packages by price (bubble sort - decreasing)
        for (int i = 0; i < sortedPackages.size() - 1; i++) {
            for (int j = 0; j < sortedPackages.size() - i - 1; j++) {
                if (sortedPackages.get(j).getPrice() < sortedPackages.get(j + 1).getPrice()) {
                    Package temp = sortedPackages.get(j);
                    sortedPackages.set(j, sortedPackages.get(j + 1));
                    sortedPackages.set(j + 1, temp);
                }
            }
        }

        for (int i = 0; i < Math.min(3, sortedPackages.size()); i++) {
            popularPackages.add(sortedPackages.get(i));
        }

        updateDisplayedPackages();
        showEmptyState(allPackages.isEmpty());
    }

    private void applyPriceFilter() {
        selectedPriceRange = spinnerPriceRange.getSelectedItemPosition();
        updateDisplayedPackages();
    }

    private void clearPriceFilter() {
        selectedPriceRange = 0;
        spinnerPriceRange.setSelection(0);
        updateDisplayedPackages();
    }

    private void updateDisplayedPackages() {
        displayedPackages.clear();

        List<Package> sourcePackages = currentTab == 0 ? popularPackages : allPackages;

        // Apply price filter - using loop
        for (Package pkg : sourcePackages) {
            if (matchesPriceFilter(pkg, selectedPriceRange)) {
                displayedPackages.add(pkg);
            }
        }

        updateUI();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private boolean matchesPriceFilter(Package pkg, int priceRangeIndex) {
        if (priceRangeIndex == 0) return true;

        long price = pkg.getPrice();
        switch (priceRangeIndex) {
            case 1: return price < 50000;
            case 2: return price >= 50000 && price <= 100000;
            case 3: return price > 100000 && price <= 200000;
            case 4: return price > 200000 && price <= 300000;
            case 5: return price > 300000 && price <= 400000;
            case 6: return price > 500000;
            default: return true;
        }
    }

    private void updateUI() {
        if (tvSectionTitle != null) {
            tvSectionTitle.setText(currentTab == 0 ? "Popular Packages" : "All Packages");
        }

        if (tvPackageCount != null) {
            tvPackageCount.setText(displayedPackages.size() + " packages");
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmptyState(boolean show) {
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up auto-slide handler
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pause auto-slide when fragment is not visible
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume auto-slide when fragment becomes visible
        if (viewPagerSlider != null && sliderAdapter != null) {
            startAutoSlide();
        }
    }
}