package io.diendanx.labbooking.activities.Cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.activities.Cart.adapters.CartAdapter;
import io.diendanx.labbooking.activities.OrderConfirmation.OrderConfirmationActivity;
import io.diendanx.labbooking.models.Cart;
import io.diendanx.labbooking.models.Facility;
import io.diendanx.labbooking.models.Package;
import io.diendanx.labbooking.repository.CartRepository;
import io.diendanx.labbooking.repository.FacilityRepository;
import io.diendanx.labbooking.repository.PackageRepository;

public class CartActivity extends AppCompatActivity {

    private List<Package> packages;
    private List<Facility> facilities;
    private CartAdapter adapter;

    private RecyclerView recyclerView;
    private List<Cart> cartList;
    private LinearLayout emptyStateLayout;
    private MaterialButton btnProceedOrder;


    private CartRepository cartRepository;
    private PackageRepository packageRepository;
    private FacilityRepository facilityRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cart), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupToolbar();
        recyclerView = findViewById(R.id.recyclerViewCartItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        btnProceedOrder = findViewById(R.id.btnProceedOrder);
        cartList = new ArrayList<>();
        packages = new ArrayList<>();
        facilities = new ArrayList<>();
        adapter = new CartAdapter(this, cartList, packages, facilities, position -> removeCartItem(position));
        recyclerView.setAdapter(adapter);
        cartRepository = new CartRepository();
        packageRepository = new PackageRepository();
        facilityRepository = new FacilityRepository();
        loadFacilitiesAndThenPackagesAndCart();
        btnProceedOrder.setOnClickListener(v -> proceedOrder());
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                getOnBackPressedDispatcher().onBackPressed();
            });
        }
    }

    private void updateEmptyState() {
        if (cartList == null || cartList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            btnProceedOrder.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            btnProceedOrder.setVisibility(View.VISIBLE);
            updateProceedButtonText();
        }
    }

    private void updateProceedButtonText() {
        long total = 0;
        if (cartList != null && packages != null) {
            for (Cart cart : cartList) {
                Package pkg = null;
                for (Package p : packages) {
                    if (p.getId().equals(cart.getPackageId())) {
                        pkg = p;
                        break;
                    }
                }
                if (pkg != null) {
                    total += (long)(pkg.getPrice() * (Math.ceil((double) cart.getDuration() / 60)));
                }

                if (cart.getSelectedFacilities() != null && facilities != null) {
                    for (String facilityId : cart.getSelectedFacilities()) {
                        for (Facility facility : facilities) {
                            if (facility.getId().equals(facilityId)) {
                                total += facility.getPrice();
                                break;
                            }
                        }
                    }
                }
            }
        }
        btnProceedOrder.setText(String.format("\uD83C\uDFC1 PROCEED ORDER (%,.0fđ)", (float) total));
    }

    private void removeCartItem(int position) {
        if (cartList != null && position >= 0 && position < cartList.size()) {
            String cartId = cartList.get(position).getId();
            cartList.remove(position);
            cartRepository.delete(cartId, new CartRepository.ItemCallback<Cart>() {
                @Override
                public void onSuccess(Cart item) {
                    Toast.makeText(CartActivity.this, "Cart item removed successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(CartActivity.this, "Failed to remove cart item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            adapter.notifyItemRemoved(position);
            updateEmptyState();
            updateProceedButtonText();
        }
    }

    private void proceedOrder() {
        // TODO: Implement order proceeding logic
        Intent intent = new Intent(CartActivity.this, OrderConfirmationActivity.class);
        startActivity(intent);
        finish();
    }


    private void loadFacilitiesAndThenPackagesAndCart() {
        facilityRepository.getAll(new FacilityRepository.ListCallback<Facility>() {
            @Override
            public void onSuccess(List<Facility> items) {
                facilities.clear();
                facilities.addAll(items);
                loadPackagesAndCart();
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CartActivity.this, "Failed to load facilities", Toast.LENGTH_SHORT).show();
                loadPackagesAndCart(); // fallback: still try to load cart
            }
        });
    }

    private void loadPackagesAndCart() {
        packageRepository.getAll(new PackageRepository.ListCallback<Package>() {
            @Override
            public void onSuccess(List<Package> items) {
                packages.clear();
                packages.addAll(items);
                loadCart();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CartActivity.this, "Failed to load packages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCart() {
        cartRepository.getAll(new CartRepository.ListCallback<Cart>() {
            @Override
            public void onSuccess(List<Cart> items) {
                cartList.clear();
                String currentUserId = getCurrentUserId();
                if (currentUserId != null) {
                    for (Cart cart : items) {
                        if (currentUserId.equals(cart.getUserId())) {
                            cartList.add(cart);
                        }
                    }
                } else {
                    cartList.addAll(items);
                }
                adapter.notifyDataSetChanged();
                updateEmptyState();
                updateProceedButtonText();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CartActivity.this, "Failed to load carts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getCurrentUserId() {
        try {
            com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                return auth.getCurrentUser().getUid();
            }
        } catch (Exception ignored) {}
        return null;
    }

}