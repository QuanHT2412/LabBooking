package io.diendanx.labbooking.activities.Order;

import android.os.Bundle;
import android.util.Log;
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

    private void loadOrders() {
        if (user == null) {
            Log.d(TAG, "User is null");
            showEmptyState();
            return;
        }
        String mail = user.getEmail();
        Log.d(TAG, "Loading orders for email: " + mail);

        db.collection("order")
                .whereEqualTo("email", mail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot querySnapshot = task.getResult();
                        Log.d(TAG, "Orders found: " + querySnapshot.size());

                        if (!querySnapshot.isEmpty()) {
                            orderList.clear();
                            List<Order> loadedOrders = new ArrayList<>();

                            for (QueryDocumentSnapshot doc : querySnapshot) {
                                Order order = doc.toObject(Order.class);
                                order.setId(doc.getId());
                                loadedOrders.add(order);
                                Log.d(TAG, "Order loaded: " + order.getId() + ", User: " + order.getUsername());
                                Log.d(TAG, "Order items IDs: " + order.getOrderItems());
                            }

                            orderList.addAll(loadedOrders);
                            loadOrderItemsForOrders(loadedOrders);
                        } else {
                            Log.d(TAG, "No orders found");
                            showEmptyState();
                        }
                    } else {
                        Log.e(TAG, "Error loading orders", task.getException());
                        showEmptyState();
                    }
                });
    }

    private void loadOrderItemsForOrders(List<Order> orders) {
        List<String> allOrderItemIds = new ArrayList<>();

        // Collect all order item IDs from all orders
        for (Order order : orders) {
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                allOrderItemIds.addAll(order.getOrderItems());
                Log.d(TAG, "Added order item IDs from order " + order.getId() + ": " + order.getOrderItems());
            }
        }

        if (allOrderItemIds.isEmpty()) {
            Log.d(TAG, "No order item IDs found in orders");
            updateUI();
            return;
        }

        Log.d(TAG, "Looking for order items with IDs: " + allOrderItemIds);

        // Now search for these order items in the order_item collection
        db.collection("order_item")
                .whereIn("id", allOrderItemIds)
                .get()
                .addOnCompleteListener(task -> {
                    orderItemsList.clear();

                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot querySnapshot = task.getResult();
                        Log.d(TAG, "Order items found: " + querySnapshot.size());

                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            OrderItem item = doc.toObject(OrderItem.class);
                            item.setId(doc.getId());
                            orderItemsList.add(item);
                            Log.d(TAG, "Order item loaded: " + item.getId() +
                                    ", Package: " + item.getPackageName());
                        }

                        // If still no items found, try using document IDs directly
                        if (querySnapshot.isEmpty()) {
                            Log.d(TAG, "No items found with 'id' field, trying document IDs...");
                            loadOrderItemsByDocumentIds(allOrderItemIds);
                        } else {
                            updateUI();
                        }
                    } else {
                        Log.e(TAG, "Error loading order items", task.getException());
                        // Try loading by document IDs as fallback
                        loadOrderItemsByDocumentIds(allOrderItemIds);
                    }
                });
    }

    private void loadOrderItemsByDocumentIds(List<String> orderItemIds) {
        Log.d(TAG, "Trying to load order items by document IDs");
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
                                Log.d(TAG, "Order item loaded by document ID: " + item.getId() +
                                        ", Package: " + item.getPackageName());
                            }
                        } else {
                            Log.d(TAG, "No document found with ID: " + itemId);
                        }

                        updateUI();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading order item by ID: " + itemId, e);
                        updateUI();
                    });
        }

        // Also update UI immediately in case no items are found
        if (orderItemIds.isEmpty()) {
            updateUI();
        }
    }

    private void updateUI() {
        Log.d(TAG, "Updating UI - Orders: " + orderList.size() + ", Items: " + orderItemsList.size());

        adapter.setOrderItems(orderItemsList);
        adapter.notifyDataSetChanged();

        if (orderList.isEmpty()) {
            showEmptyState();
        } else {
            recyclerViewOrders.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        recyclerViewOrders.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }
}