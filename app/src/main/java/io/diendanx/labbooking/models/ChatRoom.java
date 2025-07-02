package io.diendanx.labbooking.models;

import com.google.firebase.Timestamp;

import java.util.List;

public class ChatRoom {
    String chatRoomId;
    Timestamp lastMessageTimestamp;
    String lastMessageSenderId;
    String lastMessage;

    public ChatRoom() {
    }

    public ChatRoom(String chatRoomId, Timestamp lastMessageTimestamp, String lastMessageSenderId) {
        this.chatRoomId = chatRoomId;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public Timestamp getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Timestamp lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
