package io.diendanx.labbooking.activities.ProductDetail;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.NumberFormat;
import java.util.Locale;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.models.Package;
import io.diendanx.labbooking.repository.PackageRepository;
import io.diendanx.labbooking.repository.BaseRepository;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView tvName, tvInfo, tvPrice, tvDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.product_detail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup toolbar back navigation
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Initialize views
        tvName = findViewById(R.id.tv_package_name);
        tvInfo = findViewById(R.id.tv_package_info);
        tvPrice = findViewById(R.id.tv_package_price);
        tvDescription = findViewById(R.id.package_description_text);

        // Get package id from intent
        String packageId = getIntent().getStringExtra("package_id");
        if (packageId != null) {
            fetchPackageDetail(packageId);
        } else {
            Toast.makeText(this, "Package not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchPackageDetail(String packageId) {
        PackageRepository repo = new PackageRepository();
        repo.getById(packageId, new BaseRepository.ItemCallback<Package>() {
            @Override
            public void onSuccess(Package pkg) {
                if (pkg != null) {
                    showPackageDetail(pkg);
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Package not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ProductDetailActivity.this, "Failed to load package", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showPackageDetail(Package pkg) {
        tvName.setText(pkg.getName());
        tvInfo.setText(pkg.getTableCount() + " tables • " + pkg.getSeatCount() + " chairs");
        tvPrice.setText(formatPrice(pkg.getPrice()));
        if (tvDescription != null) {
            tvDescription.setText(pkg.getDescription());
        }
    }

    private String formatPrice(Long price) {
        if (price == null) return "";
        try {
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            return formatter.format(price) + "đ";
        } catch (Exception e) {
            return price + "đ";
        }
    }
}