package io.diendanx.labbooking.activities.Home.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.diendanx.labbooking.R;

public class ProductsFragment extends Fragment {

    private RecyclerView rvProducts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);
        rvProducts = view.findViewById(R.id.rv_products);
        setupRecyclerView();
        return view;
    }

    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(new ProductAdapter(getSampleProducts()));
    }

    // Placeholder model
    static class Product {
        String name;
        String description;
        String price;

        Product(String name, String description, String price) {
            this.name = name;
            this.description = description;
            this.price = price;
        }
    }

    // Placeholder data
    private List<Product> getSampleProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Pack 1", "Set of 3 chairs and 1 table", "6.000.000 VND"));
        products.add(new Product("Pack 2", "Set of 4 table and 1 chair", "12.000.000 VND"));
        products.add(new Product("Pack 3", "Set of 1 chair on top of 1 table", "1.000.000 VND"));
        return products;
    }

    // Minimal adapter for demonstration
    class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
        private final List<Product> products;

        ProductAdapter(List<Product> products) {
            this.products = products;
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
            Product p = products.get(position);
            holder.tvName.setText(p.name);
            holder.tvDescription.setText(p.description);
            holder.tvPrice.setText(p.price);
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        class ProductViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDescription, tvPrice;

            ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_product_name);
                tvDescription = itemView.findViewById(R.id.tv_product_description);
                tvPrice = itemView.findViewById(R.id.tv_product_price);
            }
        }
    }
}
