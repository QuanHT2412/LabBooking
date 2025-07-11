package io.diendanx.labbooking.activities.Order;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.models.Order;

public class OrderListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private LinearLayout emptyStateLayout;
    private OrderListAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);

        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OrderListAdapter(orderList);
        recyclerViewOrders.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        loadOrders();
    }

    private void loadOrders() {
        if (user == null) {
            showEmptyState();
            return;
        }
        String name = user.getDisplayName();
        String mail = user.getEmail();

        db.collection("order")
                .whereEqualTo("username", name)
                .whereEqualTo("email", mail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        orderList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Order order = doc.toObject(Order.class);
                            orderList.add(order);
                        }
                        adapter.notifyDataSetChanged();
                        recyclerViewOrders.setVisibility(View.VISIBLE);
                        emptyStateLayout.setVisibility(View.GONE);
                    } else {
                        showEmptyState();
                    }
                });
    }

    private void showEmptyState() {
        recyclerViewOrders.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }
}
