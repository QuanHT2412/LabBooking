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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
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
    private DocumentSnapshot lastVisible;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private static final int PAGE_SIZE = 20;
    private ListenerRegistration listenerRegistration;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatRoomRepository = new ChatRoomRepository();
    }

    @Override
    public void onStart() {
        super.onStart();
        getOrCreateChatRoom();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        initViews(view);
        setListener();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeRecyclerView();
    }

    private void initializeRecyclerView() {

        chatMessages = new ArrayList<>();

        chatAdapter = new ChatAdapter(getActivity(), chatMessages);

        LinearLayoutManager lm = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, true);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(chatAdapter);

        // Disable animations to prevent position jumps
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            // Override onItemRangeInserted to scroll to the top when new messages are added
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    private void fetchChatMessages() {
        if (chatRoom == null) {
            return;
        }

        isLoading = true;

        chatMessages.clear();

        // Lấy danh sách tin nhắn từ Firestore
        chatRoomRepository.getChatMessagesQuery(chatRoom.getChatRoomId(), "timestamp", "desc", PAGE_SIZE)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1); // Lưu lại document cuối cùng để tải thêm sau này

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            ChatMessage message = doc.toObject(ChatMessage.class);
                            chatMessages.add(message);
                        }
                        chatAdapter.setChatMessages(chatMessages); // Cập nhật danh sách tin nhắn
                        chatAdapter.notifyItemRangeInserted(0, chatMessages.size()); // Thông báo adapter đã thay đổi

                        // Kiểm tra xem có phải là trang cuối cùng không
                        isLastPage = queryDocumentSnapshots.size() < PAGE_SIZE;
                    } else {
                        isLastPage = true;
                    }
                    isLoading = false;

                    startListeningForMessages();
                });

    }

    private void getOrCreateChatRoom() {
        chatRoomRepository.getOrCreateChatRoom(FireBaseUtil.currentUserId(), new BaseRepository.ItemCallback<>() {
            @Override
            public void onSuccess(ChatRoom item) {
                chatRoom = item;
                // Cập nhật giao diện với thông tin chat room
                fetchChatMessages();
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }

    private void setListener() {

        // Set up the RecyclerView to load more messages when scrolled to the top
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (layoutManager != null && layoutManager.findLastVisibleItemPosition() >= chatMessages.size() - 1) {
                    loadMoreMessages();
                }
            }
        });

        sendButton.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString().trim();

            if (message.isEmpty()) {
                return;
            }

            sendMessage(message);
        });
    }

    private void startListeningForMessages() {
        // Listen for new messages in the chat room
        listenerRegistration = chatRoomRepository.getChatMessagesQuery(chatRoom.getChatRoomId(), "timestamp", "desc", PAGE_SIZE)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("Chat", "Listen failed.", error);
                        return;
                    }
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            ChatMessage message = dc.getDocument().toObject(ChatMessage.class);
                            // Check if this is truly a new message
                            if (!messageExists(message)) {
                                chatMessages.add(0, message); // Thêm tin nhắn mới vào đầu danh sách
                                chatAdapter.notifyItemInserted(0); // Cập nhật adapter
                            }
                        } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                            handleRemovedMessage(dc.getDocument().toObject(ChatMessage.class));
                        } else if (dc.getType() == DocumentChange.Type.MODIFIED) {
                            handleModifiedMessage(dc.getDocument().toObject(ChatMessage.class));
                        }
                    }
                });
    }

    private void handleModifiedMessage(ChatMessage modifiedMessage) {
        // Tìm kiếm tin nhắn đã sửa trong danh sách chatMessages
        for (int i = 0; i < chatMessages.size(); i++) {
            ChatMessage msg = chatMessages.get(i);
            if (msg.getTimestamp() != null &&
                    modifiedMessage.getTimestamp() != null &&
                    msg.getTimestamp().equals(modifiedMessage.getTimestamp()) &&
                    msg.getSenderId().equals(modifiedMessage.getSenderId())) {
                chatMessages.set(i, modifiedMessage); // Cập nhật tin nhắn trong danh sách
                chatAdapter.notifyItemChanged(i); // Cập nhật adapter
                break;
            }
        }
    }

    private void handleRemovedMessage(ChatMessage removedMessage) {
        // Tìm kiếm tin nhắn đã xóa trong danh sách chatMessages
        for (int i = 0; i < chatMessages.size(); i++) {
            ChatMessage msg = chatMessages.get(i);
            if (msg.getTimestamp() != null &&
                    removedMessage.getTimestamp() != null &&
                    msg.getTimestamp().equals(removedMessage.getTimestamp()) &&
                    msg.getSenderId().equals(removedMessage.getSenderId())) {
                chatMessages.remove(i); // Xóa tin nhắn khỏi danh sách
                chatAdapter.notifyItemRemoved(i); // Cập nhật adapter
                break;
            }
        }
    }

    private boolean messageExists(ChatMessage message) {
        for (ChatMessage existingMessage : chatMessages) { // Kiểm tra trong danh sách tin nhắn hiện tại
            if (existingMessage.getTimestamp() != null &&
                    message.getTimestamp() != null &&
                    existingMessage.getTimestamp().equals(message.getTimestamp()) &&
                    existingMessage.getSenderId().equals(message.getSenderId())) {
                return true;
            }
        }
        return false;
    }

    private void loadMoreMessages() {
        if (isLoading || isLastPage) return;
        isLoading = true;

        chatRoomRepository.getChatMessagesQuery(chatRoom.getChatRoomId(), "timestamp", "desc", PAGE_SIZE)
                .startAfter(lastVisible)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            ChatMessage message = doc.toObject(ChatMessage.class);
                            chatMessages.add(message);
                        }
                        chatAdapter.notifyDataSetChanged();

                        if (queryDocumentSnapshots.size() < PAGE_SIZE) {
                            isLastPage = true;
                        }
                    } else {
                        isLastPage = true;
                    }
                    isLoading = false;
                });
    }

    private void sendMessage(String message) {
        sendButton.setEnabled(false);

        chatRoomRepository.updateLastMessage(chatRoom, message, chatRoom.getChatRoomId());

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage(message);
        chatMessage.setSenderId(FireBaseUtil.currentUserId());
        chatMessage.setTimestamp(Timestamp.now()); // Set the current timestamp

        chatRoomRepository.sendMessage(chatRoom.getChatRoomId(), chatMessage, new BaseRepository.ItemCallback<Void>() {
            @Override
            public void onSuccess(Void item) {
                editTextMessage.setText("");
                sendButton.setEnabled(true);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("ChatRoomRepository", "Failed to send message: " + e.getMessage());
                // Show error to user
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
                }
                sendButton.setEnabled(true);
            }
        });
    }

    private void initViews(View view) {
        editTextMessage = view.findViewById(R.id.edt_message);
        sendButton = view.findViewById(R.id.message_send_btn);
        recyclerView = view.findViewById(R.id.chatContent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}