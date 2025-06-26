package io.diendanx.labbooking.activities.Cart;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.activities.Cart.adapters.CartAdapter;
import io.diendanx.labbooking.models.Cart;
import io.diendanx.labbooking.models.Package;

public class CartActivity extends AppCompatActivity {

    private List<Package> packages;
    private CartAdapter adapter;

    private RecyclerView recyclerView;
    private List<Cart> cartList;
    private LinearLayout emptyStateLayout;
    private MaterialButton btnProceedOrder;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

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
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        btnProceedOrder = findViewById(R.id.btnProceedOrder);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        cartList = new ArrayList<>();
        packages = new ArrayList<>();
        adapter = new CartAdapter(cartList, packages, this, position -> removeCartItem(position));
        recyclerView.setAdapter(adapter);
        loadPackagesAndCart();
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
                Package pkg = findPackageById(cart.getPackageId());
                if (pkg != null) {
                    total += pkg.getPrice();
                }
            }
        }
        btnProceedOrder.setText(String.format("Proceed (%,.0fđ)", (float) total));
    }

    private void removeCartItem(int position) {
        if (cartList != null && position >= 0 && position < cartList.size()) {
            cartList.remove(position);
            adapter.notifyItemRemoved(position);
            updateEmptyState();
            updateProceedButtonText();
        }
    }

    private void proceedOrder() {
        // TODO: Implement order proceeding logic
    }

    public void setPackages(List<Package> packages) {
        this.packages = packages;
    }

    public Package findPackageById(String id) {
        if (packages == null) return null;
        for (Package p : packages) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    private void loadPackagesAndCart() {
        db.collection("package").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                packages.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Package pkg = doc.toObject(Package.class);
                    pkg.setId(doc.getId()); // Ensure ID is set
                    packages.add(pkg);
                }
                loadCart();
            }
        });
    }

    private void loadCart() {
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid == null) return;
        db.collection("cart").document(uid).collection("items").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                cartList.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Cart cart = doc.toObject(Cart.class);
                    cartList.add(cart);
                }
                adapter.notifyDataSetChanged();
                updateEmptyState();
                updateProceedButtonText();
            }
        });
    }

}