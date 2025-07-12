package io.diendanx.labbooking.activities.Order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.models.Order;
import io.diendanx.labbooking.models.OrderItem;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.OrderViewHolder> {

    private static final String TAG = "OrderListAdapter";
    private final List<Order> orderList;
    private List<OrderItem> orderItems;
    private Map<String, String> facilityNameCache = new HashMap<>();
    private FirebaseFirestore db;

    public OrderListAdapter(List<Order> orderList, List<OrderItem> orderItems) {
        this.orderList = orderList;
        this.orderItems = orderItems != null ? orderItems : new ArrayList<>();
        this.db = FirebaseFirestore.getInstance();
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems != null ? orderItems : new ArrayList<>();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(v);
    }

    // Binds order data to the view holder
    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvUserName.setText("User: " + (order.getUsername() != null ? order.getUsername() : "N/A"));
        holder.tvOrderMail.setText("Email: " + (order.getEmail() != null ? order.getEmail() : "N/A"));
        holder.tvOrderDate.setText("Date: " + formatDate(order.getCreated_at()));
        holder.tvOrderPrice.setText("Total: " + formatPrice(order.getTotalPrice()));

        if (order.getOrderItems() != null) {
            for (String orderItemId : order.getOrderItems()) {
                for (OrderItem item : orderItems) {
                    if (item.getId() != null && item.getId().equals(orderItemId)) {
                        StringBuilder itemInfo = new StringBuilder();
                        itemInfo.append("📦 Package: ").append(item.getPackageName() != null ? item.getPackageName() : "N/A").append("\n");
                        itemInfo.append("⏱️ Duration: ").append(formatDuration(item.getDuration())).append("\n");
                        itemInfo.append("📅 Start: ").append(formatDate(item.getStartDate())).append("\n");
                        itemInfo.append("🔧 Add-ons: ");

                        holder.tvOrderItems.setText(itemInfo.toString() + "Loading...");
                        loadFacilityNames(item.getSelectedFacilities(), holder.tvOrderItems, itemInfo.toString());
                        break;
                    }
                }
            }
        } else {
            holder.tvOrderItems.setText("No items found");
        }
    }

    // Loads facility names from IDs and updates the target view
    private void loadFacilityNames(List<String> facilityIds, TextView targetView, String baseText) {
        if (facilityIds == null || facilityIds.isEmpty()) {
            targetView.setText(baseText + "None");
            return;
        }

        // Check if facilities are already names (contain spaces or are short)
        boolean areAlreadyNames = facilityIds.stream().allMatch(f ->
                f != null && (f.contains(" ") || f.length() < 15));

        if (areAlreadyNames) {
            targetView.setText(baseText + String.join(", ", facilityIds));
            return;
        }

        List<String> facilityNames = new ArrayList<>();
        AtomicInteger completedRequests = new AtomicInteger(0);

        for (String facilityId : facilityIds) {
            if ("YjIgSE2hfyxYflcGlnPs".equals(facilityId) || "Yjig5E2hfyxYfIcGInPs".equals(facilityId.trim())) {
                facilityNames.add("Whiteboard");
                if (completedRequests.incrementAndGet() == facilityIds.size()) {
                    targetView.setText(baseText + String.join(", ", facilityNames));
                }
                continue;
            }

            if (isWhiteboardId(facilityId)) {
                facilityNames.add("Whiteboard");
                if (completedRequests.incrementAndGet() == facilityIds.size()) {
                    targetView.setText(baseText + String.join(", ", facilityNames));
                }
                continue;
            }

            if (facilityNameCache.containsKey(facilityId)) {
                String cachedName = facilityNameCache.get(facilityId);
                facilityNames.add(cachedName);

                if (completedRequests.incrementAndGet() == facilityIds.size()) {
                    targetView.setText(baseText + String.join(", ", facilityNames));
                }
            } else {
                db.collection("facility").document(facilityId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            String facilityName;
                            if (documentSnapshot.exists()) {
                                String name = documentSnapshot.getString("name");
                                if (name != null && !name.trim().isEmpty()) {
                                    facilityName = name;
                                } else {
                                    facilityName = "Unnamed Facility";
                                }
                            } else {
                                facilityName = "Unknown Facility";
                            }

                            facilityNameCache.put(facilityId, facilityName);
                            facilityNames.add(facilityName);

                            if (completedRequests.incrementAndGet() == facilityIds.size()) {
                                targetView.setText(baseText + String.join(", ", facilityNames));
                            }
                        })
                        .addOnFailureListener(e -> {
                            facilityNames.add("Error Loading Facility");

                            if (completedRequests.incrementAndGet() == facilityIds.size()) {
                                targetView.setText(baseText + String.join(", ", facilityNames));
                            }
                        });
            }
        }
    }

    // Checks if the given ID is a known whiteboard ID
    private boolean isWhiteboardId(String facilityId) {
        if (facilityId == null) return false;

        String cleanId = facilityId.trim().replaceAll("\\s+", "");

        String[] knownWhiteboardIds = {
                "YjIgSE2hfyxYflcGlnPs",
                "Yjig5E2hfyxYfIcGInPs"
        };

        for (String knownId : knownWhiteboardIds) {
            if (cleanId.equals(knownId)) {
                return true;
            }
        }

        return false;
    }

    // Formats duration from minutes to a human-readable string (hours/days)
    private String formatDuration(int duration) {
        if (duration <= 0) {
            return "Invalid duration";
        }

        // Convert from stored value to actual hours
        int actualHours;

        if (duration >= 1440) {
            // 1 day = 1440 minutes
            actualHours = duration / 60; // Convert minutes to hours
        } else if (duration == 60) {
            actualHours = 1;
        } else if (duration > 60 && duration < 1440) {
            actualHours = duration / 60;
        } else {
            // For small values, assume it's already in hours
            actualHours = duration;
        }

        if (actualHours < 24) {
            // Show as hours
            return actualHours + " hour" + (actualHours != 1 ? "s" : "");
        } else if (actualHours % 24 == 0) {
            // Show as days
            int days = actualHours / 24;
            return days + " day" + (days != 1 ? "s" : "");
        } else {
            // Show as days and hours
            int days = actualHours / 24;
            int hours = actualHours % 24;
            return days + " day" + (days != 1 ? "s" : "") +
                    " " + hours + " hour" + (hours != 1 ? "s" : "");
        }
    }

    // Formats a Firestore timestamp to a readable date string
    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        try {
            Date date = timestamp.toDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            return dateFormat.format(date);
        } catch (Exception e) {
            return "Invalid Date";
        }
    }

    // Formats a price with thousand separators
    private String formatPrice(long price) {
        return String.format(Locale.getDefault(), "%,d", price);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvOrderMail, tvOrderDate, tvOrderPrice, tvOrderItems;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvOrderMail = itemView.findViewById(R.id.tvOrderMail);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderPrice = itemView.findViewById(R.id.tvOrderPrice);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
        }
    }
}
