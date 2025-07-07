package io.diendanx.labbooking.activities.Home.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.diendanx.labbooking.Helpers.ImageGetterUtils;
import io.diendanx.labbooking.R;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder> {
    private final Context context;
    private String[] imageUrls = null;
    private int[] imageResIds = null;

    // Constructor for URLs
    public ImageSliderAdapter(Context context, String[] imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    // Constructor for drawable resource IDs
    public ImageSliderAdapter(Context context, int[] imageResIds) {
        this.context = context;
        this.imageResIds = imageResIds;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        try {
            if (imageResIds != null) {
                holder.imageView.setImageResource(imageResIds[position]);
            } else if (imageUrls != null) {
                String imageUrl = imageUrls[position];
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    ImageGetterUtils.loadImage(
                            context,
                            imageUrl,
                            holder.imageView
                    );
                } else {
                    holder.imageView.setImageResource(R.drawable.image_not_found);
                }
            } else {
                holder.imageView.setImageResource(R.drawable.image_not_found);
            }
        } catch (Exception e) {
            e.printStackTrace();
            holder.imageView.setImageResource(R.drawable.image_not_found);
        }
    }

    @Override
    public int getItemCount() {
        if (imageResIds != null) return imageResIds.length;
        if (imageUrls != null) return imageUrls.length;
        return 0;
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.slider_image);
        }
    }
}