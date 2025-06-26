package io.diendanx.labbooking.activities.Cart.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.models.Cart;
import io.diendanx.labbooking.models.Facility;
import io.diendanx.labbooking.models.Package;
import io.diendanx.labbooking.repository.BaseRepository;
import io.diendanx.labbooking.repository.FacilityRepository;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.PackageViewHolder> {
    private List<Cart> cartList;
    private List<Package> packages;
    private List<Facility> facilities;

    private OnRemoveClickListener removeClickListener;

    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }

    public CartAdapter(List<Cart> cartList, List<Package> packages, List<Facility> facilities, OnRemoveClickListener removeClickListener) {
        this.cartList = cartList;
        this.packages = packages;
        this.facilities = facilities;
        this.removeClickListener = removeClickListener;
    }

    private Package findPackageById(String id) {
        if (packages == null) return null;
        for (Package p : packages) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_lab, parent, false);
        return new PackageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
        Cart cart = cartList.get(position);
        Package packageDetails = findPackageById(cart.getPackageId());
        int seatCount = packageDetails != null ? packageDetails.getSeatCount() : 0;
        int tableCount = packageDetails != null ? packageDetails.getTableCount() : 0;
        String packageName = packageDetails != null ? packageDetails.getName() : cart.getPackageId();
        long packagePrice = packageDetails != null ? packageDetails.getPrice() : 0;
        holder.imageLabIcon.setImageResource(R.drawable.ic_package);
        holder.textProductName.setText(packageName);
        holder.textProductDescription.setText(seatCount + " seats - " + tableCount + " tables");
        // Facilities: join names
        List<String> facilityIds = cart.getSelectedFacilities();
        List<String> facilityNames = new ArrayList<>();
        if (facilityIds != null && facilities != null && !facilities.isEmpty()) {
            for (String id : facilityIds) {
                for (Facility f : facilities) {
                    if (id.trim().equals(f.getId().trim())) {
                        facilityNames.add(f.getName());
                        break;
                    }
                }
            }
        }
        holder.textFacilities.setText(
            !facilityNames.isEmpty() ? TextUtils.join(", ", facilityNames) : "N/A"
        );
        // Calculate total price
        long totalPrice = packagePrice;
        if (facilityIds != null && facilities != null && !facilities.isEmpty()) {
            for (String id : facilityIds) {
                for (Facility f : facilities) {
                    if (id.trim().equals(f.getId().trim())) {
                        totalPrice += (long) f.getPrice();
                        break;
                    }
                }
            }
        }
        holder.textPrice.setText(String.format("%,.0fđ", (float) totalPrice));
        // Booking date
        if (cart.getStartDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            holder.textBookingDate.setText(sdf.format(cart.getStartDate().toDate()));
        } else {
            holder.textBookingDate.setText("-");
        }
        // Duration (time)
        if (cart.getStartDate() != null && cart.getDuration() > 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(cart.getStartDate().toDate());
            int startHour = cal.get(Calendar.HOUR_OF_DAY);
            int startMinute = cal.get(Calendar.MINUTE);
            int durationMinutes = cart.getDuration();
            int endTotalMinutes = startHour * 60 + startMinute + durationMinutes;
            int endHour = endTotalMinutes / 60;
            int endMinute = endTotalMinutes % 60;
            String startStr = String.format("%02d:%02d", startHour, startMinute);
            String endStr = String.format("%02d:%02d", endHour, endMinute);
            holder.textBookingTime.setText(startStr + " - " + endStr);
        } else {
            holder.textBookingTime.setText("-");
        }
        holder.buttonRemove.setOnClickListener(v -> {
            if (removeClickListener != null) removeClickListener.onRemoveClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return cartList != null ? cartList.size() : 0;
    }

    public static class PackageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageLabIcon;
        TextView textProductName, textProductDescription, textFacilities, textPrice, textBookingDate, textBookingTime;
        ImageButton buttonRemove;

        public PackageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageLabIcon = itemView.findViewById(R.id.imageLabIcon);
            textProductName = itemView.findViewById(R.id.textProductName);
            textProductDescription = itemView.findViewById(R.id.textProductDescription);
            textFacilities = itemView.findViewById(R.id.textFacilities);
            textPrice = itemView.findViewById(R.id.textPrice);
            textBookingDate = itemView.findViewById(R.id.textBookingDate);
            textBookingTime = itemView.findViewById(R.id.textBookingTime);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
        }
    }
}
