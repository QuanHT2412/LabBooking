package io.diendanx.labbooking.activities.Home.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.activities.ProductDetail.ProductDetailActivity;
import io.diendanx.labbooking.models.Package;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private final List<Package> packages;

    public ProductAdapter(List<Package> packages) {
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

            int[] packageImages = {
                R.drawable.package_image_1,
                R.drawable.package_image_2,
                R.drawable.package_image_3
            };
            int imageResId = packageImages[position % packageImages.length];
            holder.imageProduct.setImageResource(imageResId);

            holder.itemView.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(holder.itemView.getContext(), ProductDetailActivity.class);
                    intent.putExtra("package_id", p.getId());
                    holder.itemView.getContext().startActivity(intent);
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

    static class ProductViewHolder extends RecyclerView.ViewHolder {
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

