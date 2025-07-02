package io.diendanx.labbooking.activities.Home.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import io.diendanx.labbooking.Helpers.FireBaseUtil;
import io.diendanx.labbooking.R;
import io.diendanx.labbooking.activities.Home.adapters.ChatAdapter;
import io.diendanx.labbooking.models.ChatMessage;
import io.diendanx.labbooking.models.ChatRoom;
import io.diendanx.labbooking.repository.BaseRepository;
import io.diendanx.labbooking.repository.ChatRoomRepository;

public class ChatFragment extends Fragment {

    private EditText editTextMessage;
    private ImageButton sendButton;
    private ChatRoomRepository chatRoomRepository;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private ChatRoom chatRoom;
    private List<ChatMessage> chatMessages;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatRoomRepository = new ChatRoomRepository();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        initViews(view);
        setListener();
        getOrCreateChatRoom();
        setUpChatRecyclerView();
        return view;
    }

    private void setUpChatRecyclerView() {

        chatMessages = new ArrayList<>();

        chatAdapter = new ChatAdapter(getActivity(), chatMessages);


        // Lấy danh sách tin nhắn từ Firestore
        chatRoomRepository.getChatRoomMessagesRef(FireBaseUtil.currentUserId())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50) // Giới hạn số lượng tin nhắn tải xuống
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (var document : task.getResult()) {
                            ChatMessage chatMessage = document.toObject(ChatMessage.class);
                            chatMessages.add(chatMessage);
                        }
                        chatAdapter.setChatMessages(chatMessages); // Cập nhật dữ liệu cho adapter
                        chatAdapter.notifyDataSetChanged(); // Cập nhật adapter với dữ liệu mới
                    } else {
                        Log.e("ChatFragment", "Error getting messages: ", task.getException());
                    }
                });

        // LayoutManager stackFromEnd
        LinearLayoutManager lm = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, true);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(chatAdapter);

        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    private void getOrCreateChatRoom() {
        chatRoomRepository.getOrCreateChatRoom(FireBaseUtil.currentUserId(), new BaseRepository.ItemCallback<>() {
            @Override
            public void onSuccess(ChatRoom item) {
                chatRoom = item;
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }

    private void setListener() {
        sendButton.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString().trim();

            if (message.isEmpty()) {
                return;
            }

            sendMessage(message);
        });
    }

    private void sendMessage(String message) {
        chatRoomRepository.updateLastMessage(chatRoom, message, chatRoom.getChatRoomId());

        ChatMessage chatMessage = new ChatMessage(message, FireBaseUtil.currentUserId(), null);

        chatRoomRepository.sendMessage(chatRoom.getChatRoomId(), chatMessage, new BaseRepository.ItemCallback<Void>() {
            @Override
            public void onSuccess(Void item) {
                editTextMessage.setText("");
                chatMessages.add(0, chatMessage); // Thêm tin nhắn mới vào danh sách
                chatAdapter.notifyItemInserted(0);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("ChatRoomRepository", "Failed to send message: " + e.getMessage());
            }
        });
    }

    private void initViews(View view) {
        editTextMessage = view.findViewById(R.id.edt_message);
        sendButton = view.findViewById(R.id.message_send_btn);
        recyclerView = view.findViewById(R.id.chatContent);
    }
}