package io.diendanx.labbooking.activities.Home.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import io.diendanx.labbooking.models.Package;

public class ProductsFragment extends Fragment {

    private RecyclerView rvProducts;
    private TabLayout tabLayout;
    private Spinner spinnerPriceRange;
    private Button btnApplyFilter, btnClearFilter;
    private ProgressBar progressBar;
    private View layoutEmptyState;
    private TextView tvSectionTitle, tvPackageCount;

    private ProductAdapter adapter;
    private List<Package> allPackages = new ArrayList<>();
    private List<Package> displayedPackages = new ArrayList<>();
    private List<Package> popularPackages = new ArrayList<>();

    private String[] priceRanges = {
            "All Prices",
            "Under 50,000 VND",
            "100,000 - 200,000 VND",
            "200,000 - 300,000 VND",
            "300,000 - 400,000 VND",
            "Above 500,000 VND"
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
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProductAdapter(allPackages);
        rv.setAdapter(adapter);
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
            case 3: return price > 200000 && price <= 300000;
            case 4: return price > 300000 && price <= 400000;
            case 5: return price > 500000;
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

    // Product Adapter Class
    class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
        private final List<Package> packages;

        ProductAdapter(List<Package> packages) {
            this.packages = packages;
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_product, parent, false);
            return new ProductViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            try {
                Package p = packages.get(position);
                holder.tvName.setText(p.getName());
                holder.tvDescription.setText(p.getDescription());
                holder.tvPrice.setText(formatPrice(p.getPrice()));
                // Load image using Glide utility
                ImageGetterUtils.loadImage(holder.itemView.getContext(), p.getImageUrl(), holder.imageProduct);

                holder.itemView.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
                        intent.putExtra("package_id", p.getId());
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return packages != null ? packages.size() : 0;
        }

        class ProductViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDescription, tvPrice;
            ImageView imageProduct;

            ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_product_name);
                tvDescription = itemView.findViewById(R.id.tv_product_description);
                tvPrice = itemView.findViewById(R.id.tv_product_price);
                imageProduct = itemView.findViewById(R.id.image_product);
            }
        }

        private String formatPrice(Long price) {
            if (price == null) return "";
            try {
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                return formatter.format(price) + " VND";
            } catch (Exception e) {
                return price + " VND";
            }
        }
    }
}