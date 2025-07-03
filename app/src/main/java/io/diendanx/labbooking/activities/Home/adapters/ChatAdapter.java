package io.diendanx.labbooking.activities.Home.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.diendanx.labbooking.Helpers.DateTimeFormatter;
import io.diendanx.labbooking.Helpers.FireBaseUtil;
import io.diendanx.labbooking.R;
import io.diendanx.labbooking.models.ChatMessage;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private Context context;

    private List<ChatMessage> chatMessages;
    private DateTimeFormatter dateTimeFormatter;

    public ChatAdapter(Context context, List<ChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.dateTimeFormatter = new DateTimeFormatter();
    }

    public void setChatMessages(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_recycler_row, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.txtSentDateTime.setVisibility(View.GONE); // Initially hide the sent date time text

        ChatMessage chatMessage = chatMessages.get(position);

        // Format the timestamp to a human-readable format
        if (chatMessage.getTimestamp() != null) {
            holder.txtSentDateTime.setText(dateTimeFormatter.format(chatMessage.getTimestamp().toDate().toInstant( )));
        }

        if (chatMessage.getSenderId().equals(FireBaseUtil.currentUserId())) {
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.txtSentMessage.setText(chatMessage.getMessage());
            holder.rightChatLayout.setOnClickListener(v -> {
                // Toggle visibility of the sent date time text
                if (holder.txtSentDateTime.getVisibility() != View.VISIBLE) {
                    holder.txtSentDateTime.setVisibility(View.VISIBLE);
                    holder.txtSentDateTime.animate().alpha(1f).setDuration(200).start(); // Show with animation
                } else {
                    holder.txtSentDateTime.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                        holder.txtSentDateTime.setVisibility(View.GONE);
                    }).start(); // Hide with animation
                }
            });
        } else {
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.txtReceivedMessage.setText(chatMessage.getMessage());
            holder.leftChatLayout.setOnClickListener(v -> {
                // Toggle visibility of the sent date time text
                if (holder.txtSentDateTime.getVisibility() != View.VISIBLE) {
                    holder.txtSentDateTime.setVisibility(View.VISIBLE);
                    holder.txtSentDateTime.animate().alpha(1f).setDuration(200).start(); // Show with animation
                } else {
                    holder.txtSentDateTime.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                        holder.txtSentDateTime.setVisibility(View.GONE);
                    }).start(); // Hide with animation
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftChatLayout, rightChatLayout;
        TextView txtReceivedMessage, txtSentMessage, txtSentDateTime;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            txtReceivedMessage = itemView.findViewById(R.id.text_received_message);
            txtSentMessage = itemView.findViewById(R.id.text_sent_message);
            txtSentDateTime = itemView.findViewById(R.id.text_message_sent_time);
            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
        }
    }
}
