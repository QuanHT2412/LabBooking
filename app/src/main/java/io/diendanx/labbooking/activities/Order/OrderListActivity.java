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
import io.diendanx.labbooking.models.OrderItem;
import com.google.firebase.firestore.QuerySnapshot;

public class OrderListActivity extends AppCompatActivity {

    private static final String TAG = "OrderListActivity";
    private RecyclerView recyclerViewOrders;
    private LinearLayout emptyStateLayout;
    private OrderListAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private List<OrderItem> orderItemsList = new ArrayList<>();
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

        adapter = new OrderListAdapter(orderList, orderItemsList);
        recyclerViewOrders.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        loadOrders();
    }

    // Loads orders for the current user
    private void loadOrders() {
        if (user == null) {
            showEmptyState();
            return;
        }
        String mail = user.getEmail();

        db.collection("order")
                .whereEqualTo("email", mail)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot querySnapshot = task.getResult();

                        if (!querySnapshot.isEmpty()) {
                            orderList.clear();
                            List<Order> loadedOrders = new ArrayList<>();

                            for (QueryDocumentSnapshot doc : querySnapshot) {
                                Order order = doc.toObject(Order.class);
                                order.setId(doc.getId());
                                loadedOrders.add(order);
                            }

                            orderList.addAll(loadedOrders);
                            loadOrderItemsForOrders(loadedOrders);
                        } else {
                            showEmptyState();
                        }
                    } else {
                        showEmptyState();
                    }
                });
    }

    // Loads order items for the given orders
    private void loadOrderItemsForOrders(List<Order> orders) {
        List<String> allOrderItemIds = new ArrayList<>();

        // Collect all order item IDs from all orders
        for (Order order : orders) {
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                allOrderItemIds.addAll(order.getOrderItems());
            }
        }

        if (allOrderItemIds.isEmpty()) {
            updateUI();
            return;
        }

        // Search for these order items in the order_item collection
        db.collection("order_item")
                .whereIn("id", allOrderItemIds)
                .get()
                .addOnCompleteListener(task -> {
                    orderItemsList.clear();

                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot querySnapshot = task.getResult();

                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            OrderItem item = doc.toObject(OrderItem.class);
                            item.setId(doc.getId());
                            orderItemsList.add(item);
                        }

                        // If still no items found, try using document IDs directly
                        if (querySnapshot.isEmpty()) {
                            loadOrderItemsByDocumentIds(allOrderItemIds);
                        } else {
                            updateUI();
                        }
                    } else {
                        // Try loading by document IDs as fallback
                        loadOrderItemsByDocumentIds(allOrderItemIds);
                    }
                });
    }

    // Fallback method to load order items by their document IDs
    private void loadOrderItemsByDocumentIds(List<String> orderItemIds) {
        orderItemsList.clear();

        // Load each order item by its document ID
        for (String itemId : orderItemIds) {
            db.collection("order_item")
                    .document(itemId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            OrderItem item = documentSnapshot.toObject(OrderItem.class);
                            if (item != null) {
                                item.setId(documentSnapshot.getId());
                                orderItemsList.add(item);
                            }
                        }
                        updateUI();
                    })
                    .addOnFailureListener(e -> {
                        updateUI();
                    });
        }

        // Also update UI immediately in case no items are found
        if (orderItemIds.isEmpty()) {
            updateUI();
        }
    }

    // Updates the UI with current order data
    private void updateUI() {
        adapter.setOrderItems(orderItemsList);
        adapter.notifyDataSetChanged();

        if (orderList.isEmpty()) {
            showEmptyState();
        } else {
            recyclerViewOrders.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    // Shows empty state when no orders are available
    private void showEmptyState() {
        recyclerViewOrders.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }
}
