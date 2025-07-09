package io.diendanx.labbooking.activities.CartConfirm.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.models.Facility;

public class FacilityAdapter extends RecyclerView.Adapter<FacilityAdapter.FacilityViewHolder> {

    private List<Facility> facilityList;
    private Set<Facility> selectedFacilities = new HashSet<>();

    public FacilityAdapter(List<Facility> facilityList) {
        this.facilityList = facilityList;
    }

    public Set<Facility> getSelectedFacilities() {
        return selectedFacilities;
    }

    @NonNull
    @Override
    public FacilityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_facility, parent, false);
        return new FacilityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FacilityViewHolder holder, int position) {
        Facility facility = facilityList.get(position);
        holder.name.setText(facility.getName());
        holder.description.setText(facility.getDescription());
        holder.price.setText(formatPrice(facility.getPrice()));

        holder.checkBox.setOnCheckedChangeListener(null); // avoid unwanted triggers
        holder.checkBox.setChecked(selectedFacilities.contains(facility));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedFacilities.add(facility);
            } else {
                selectedFacilities.remove(facility);
            }
        });


    }

    @Override
    public int getItemCount() {
        return facilityList.size();
    }

    private String formatPrice(double price) {
        try {
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            return formatter.format(price) + "đ";
        } catch (Exception e) {
            return price + "đ";
        }
    }

    public static class FacilityViewHolder extends RecyclerView.ViewHolder {
        TextView name, description, price;
        CheckBox checkBox;
        public FacilityViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.facilityName);
            description = itemView.findViewById(R.id.facilityDescription);
            price = itemView.findViewById(R.id.facilityPrice);
            checkBox = itemView.findViewById(R.id.facilityCheckbox);

        }

    }
}
