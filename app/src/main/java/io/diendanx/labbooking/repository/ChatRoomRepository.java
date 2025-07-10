package io.diendanx.labbooking.repository;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.diendanx.labbooking.Helpers.FireBaseUtil;
import io.diendanx.labbooking.models.ChatMessage;
import io.diendanx.labbooking.models.ChatRoom;

public class ChatRoomRepository extends BaseRepository<ChatRoom> {
    public ChatRoomRepository() {
        super("chat_room");
    }

    public void getOrCreateChatRoom(String chatRoomId, ItemCallback<ChatRoom> callback) {
        getById(chatRoomId, new ItemCallback<ChatRoom>() {
            @Override
            public void onSuccess(ChatRoom chatRoom) {
                callback.onSuccess(chatRoom);
            }

            @Override
            public void onFailure(Exception e) {
                // If the chat room does not exist, create a new one
                ChatRoom newChatRoom = new ChatRoom(
                        chatRoomId,
                        Timestamp.now(),
                        "");
                add(chatRoomId, newChatRoom, callback);
            }
        });
    }

    public void updateLastMessage(ChatRoom chatRoom, String message, String senderId) {
        chatRoom.setLastMessage(message);
        chatRoom.setLastMessageSenderId(senderId);
        chatRoom.setLastMessageTimestamp(Timestamp.now());

        update(chatRoom.getChatRoomId(), chatRoom, new ItemCallback<>() {
            @Override
            public void onSuccess(ChatRoom item) {
                Log.d("ChatRoomRepository", "Last message updated successfully");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("ChatRoomRepository", "Failed to update last message: " + e.getMessage());
            }
        });
    }

    public Query getChatMessagesQuery(String chatRoomId, String orderByField, String orderByDirection, int limit) {
        return ref.document(chatRoomId)
                .collection("chat_messages")
                .orderBy(orderByField, "asc".equals(orderByDirection) ? Query.Direction.ASCENDING : Query.Direction.DESCENDING)
                .limit(limit);
    }

    public void sendMessage(String chatRoomId, ChatMessage msg, ItemCallback<Void> callback) {

        ref.document(chatRoomId).collection("chat_messages")
                .add(msg)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException() : new Exception("Unknown error"));
                    }
                });
    }

    public CollectionReference getRef() {
        return ref;
    }
}
