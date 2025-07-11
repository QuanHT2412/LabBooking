package io.diendanx.labbooking.activities.Order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.models.Order;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.OrderViewHolder> {

    private final List<Order> orderList;

    public OrderListAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvUserName.setText("User: " + order.getUsername());
        holder.tvOrderMail.setText("Email: " + order.getEmail());
       holder.tvOrderDate.setText("Date: " + formatDate(new Timestamp(order.getCreated_at().getSeconds() * 1000)));
        holder.tvOrderPrice.setText("Total: " + order.getTotalPrice());

    }

    private String formatDate(Timestamp rawDate) {
        try {
            Date date = new Date(rawDate.getTime());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            return dateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "Invalid Date";
        }

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvOrderMail, tvOrderDate, tvOrderPrice;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvOrderMail = itemView.findViewById(R.id.tvOrderMail);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderPrice = itemView.findViewById(R.id.tvOrderPrice);
        }
    }
}
