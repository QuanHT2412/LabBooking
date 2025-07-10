package io.diendanx.labbooking.activities.Home.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.models.ChatRoom;

public class ChatRoomListAdapter extends RecyclerView.Adapter<ChatRoomListAdapter.ChatRoomViewHolder> {

    public interface OnChatRoomClickListener {
        void onClick(ChatRoom chatRoom);
    }

    private final List<ChatRoom> chatRooms;
    private final OnChatRoomClickListener listener;

    public ChatRoomListAdapter(List<ChatRoom> chatRooms, OnChatRoomClickListener listener) {
        this.chatRooms = chatRooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom room = chatRooms.get(position);

        // Nếu không có tin nhắn
        if (room.getLastMessage() == null || room.getLastMessageSenderId() == null) {
            holder.tvSenderId.setText("Chưa có người gửi");
            holder.tvLastMessage.setText("Chưa có tin nhắn");
        } else {
            Log.d("ChatRoomAdapter", "Fetching username for userId: " + room.getLastMessageSenderId());

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(room.getLastMessageSenderId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String senderName = documentSnapshot.getString("username");
                            if (senderName == null || senderName.isEmpty()) {
                                senderName = "Unknown";
                            }
                            holder.tvSenderId.setText("From: " + senderName);
                        } else {
                            Log.w("ChatRoomAdapter", "User document not found for ID: " + room.getLastMessageSenderId());
                            holder.tvSenderId.setText("From: Unknown");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ChatRoomAdapter", "Error fetching user document", e);
                        holder.tvSenderId.setText("From: Error");
                    });

            holder.tvLastMessage.setText(room.getLastMessage());
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(room));
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    public static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvSenderId, tvLastMessage;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderId = itemView.findViewById(R.id.tv_sender_id);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
        }
    }
}

