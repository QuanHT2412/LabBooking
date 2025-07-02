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

import io.diendanx.labbooking.Helpers.FireBaseUtil;
import io.diendanx.labbooking.R;
import io.diendanx.labbooking.models.ChatMessage;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private Context context;

    private List<ChatMessage> chatMessages;

    public ChatAdapter(Context context, List<ChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
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

        holder.txtMessageStatus.setVisibility(View.GONE);
        holder.txtSentDateTime.setVisibility(View.GONE);

        ChatMessage chatMessage = chatMessages.get(position);

        if (chatMessage.getSenderId().equals(FireBaseUtil.currentUserId())) {
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.txtSentMessage.setText(chatMessage.getMessage());
        } else {
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.txtReceivedMessage.setText(chatMessage.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftChatLayout, rightChatLayout;
        TextView txtReceivedMessage, txtSentMessage, txtSentDateTime, txtMessageStatus;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            txtReceivedMessage = itemView.findViewById(R.id.text_received_message);
            txtSentMessage = itemView.findViewById(R.id.text_sent_message);
            txtSentDateTime = itemView.findViewById(R.id.text_message_sent_time);
            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            txtMessageStatus = itemView.findViewById(R.id.text_message_status);
        }
    }
}
