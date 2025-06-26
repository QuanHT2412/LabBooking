package io.diendanx.labbooking.activities.Cart.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.activities.Cart.CartActivity;
import io.diendanx.labbooking.models.Cart;
import io.diendanx.labbooking.models.Facility;
import io.diendanx.labbooking.models.Package;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.PackageViewHolder> {
    private List<Cart> cartList;
    private List<Package> packages;
    private CartActivity cartActivity;
    private OnRemoveClickListener removeClickListener;

    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }

    public CartAdapter(List<Cart> cartList, List<Package> packages, CartActivity cartActivity, OnRemoveClickListener removeClickListener) {
        this.cartList = cartList;
        this.packages = packages;
        this.cartActivity = cartActivity;
        this.removeClickListener = removeClickListener;
    }

    @NonNull
    @Override
    public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(io.diendanx.labbooking.R.layout.item_cart_lab, parent, false);
        return new PackageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
        Cart cart = cartList.get(position);
        Package packageDetails = null;
        if (cartActivity != null) {
            packageDetails = cartActivity.findPackageById(cart.getPackageId());
        } else if (packages != null) {
            for (Package p : packages) {
                if (p.getId().equals(cart.getPackageId())) {
                    packageDetails = p;
                    break;
                }
            }
        }
        int seatCount = packageDetails != null ? packageDetails.getSeatCount() : 0;
        int tableCount = packageDetails != null ? packageDetails.getTableCount() : 0;
        String packageName = packageDetails != null ? packageDetails.getName() : cart.getPackageId();
        long packagePrice = packageDetails != null ? packageDetails.getPrice() : 0;
        holder.imageLabIcon.setImageResource(io.diendanx.labbooking.R.drawable.ic_package);
        holder.textProductName.setText(packageName);
        holder.textProductDescription.setText(seatCount + " seats - " + tableCount + " tables");
        // Facilities: join names
        List<Facility> facilities = cart.getSelectedFacilities();
        if (facilities != null && !facilities.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < facilities.size(); i++) {
                sb.append(facilities.get(i).getName());
                if (i < facilities.size() - 1) sb.append(", ");
            }
            holder.textFacilities.setText(sb.toString());
        } else {
            holder.textFacilities.setText("N/A");
        }
        holder.textPrice.setText(String.format("%,.0fđ", (float)packagePrice));
        // Booking date
        if (cart.getStartDate() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            holder.textBookingDate.setText(sdf.format(cart.getStartDate().toDate()));
        } else {
            holder.textBookingDate.setText("-");
        }
        // Duration (time)
        if (cart.getStartDate() != null && cart.getDuration() > 0) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(cart.getStartDate().toDate());
            int startHour = cal.get(java.util.Calendar.HOUR_OF_DAY);
            int startMinute = cal.get(java.util.Calendar.MINUTE);
            int endHour = startHour + cart.getDuration();
            String startStr = String.format("%02d:%02d", startHour, startMinute);
            String endStr = String.format("%02d:%02d", endHour, startMinute);
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
            imageLabIcon = itemView.findViewById(io.diendanx.labbooking.R.id.imageLabIcon);
            textProductName = itemView.findViewById(io.diendanx.labbooking.R.id.textProductName);
            textProductDescription = itemView.findViewById(io.diendanx.labbooking.R.id.textProductDescription);
            textFacilities = itemView.findViewById(io.diendanx.labbooking.R.id.textFacilities);
            textPrice = itemView.findViewById(io.diendanx.labbooking.R.id.textPrice);
            textBookingDate = itemView.findViewById(io.diendanx.labbooking.R.id.textBookingDate);
            textBookingTime = itemView.findViewById(io.diendanx.labbooking.R.id.textBookingTime);
            buttonRemove = itemView.findViewById(io.diendanx.labbooking.R.id.buttonRemove);
        }
    }
}
