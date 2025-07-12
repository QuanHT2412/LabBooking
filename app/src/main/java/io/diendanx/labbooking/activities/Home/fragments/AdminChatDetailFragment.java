package io.diendanx.labbooking.activities.Home.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import io.diendanx.labbooking.Helpers.FireBaseUtil;
import io.diendanx.labbooking.R;
import io.diendanx.labbooking.activities.Home.adapters.ChatAdapter;
import io.diendanx.labbooking.models.ChatMessage;
import io.diendanx.labbooking.repository.BaseRepository;
import io.diendanx.labbooking.repository.ChatRoomRepository;

public class AdminChatDetailFragment extends Fragment {

    private String chatRoomId;
    private RecyclerView recyclerView;
    private EditText edtMessage;
    private ImageButton btnSend;
    private MaterialToolbar topAppBar;
    private ChatRoomRepository chatRoomRepo;
    private ChatAdapter adapter;
    private final List<ChatMessage> messageList = new ArrayList<>();
    private DocumentSnapshot lastVisible;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private static final int PAGE_SIZE = 20;
    private ListenerRegistration listenerRegistration;

    public AdminChatDetailFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_chat, container, false);

        recyclerView = view.findViewById(R.id.chatContent);
        edtMessage = view.findViewById(R.id.edt_message);
        btnSend = view.findViewById(R.id.message_send_btn);
        topAppBar = view.findViewById(R.id.topAppBar);

        chatRoomRepo = new ChatRoomRepository();

        if (getArguments() != null) {
            chatRoomId = getArguments().getString("chatRoomId");
        }

        if (chatRoomId == null || chatRoomId.isEmpty()) {
            Toast.makeText(getContext(), "Chat room ID không hợp lệ", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Lấy senderId từ tin nhắn cuối cùng để truy username
        FirebaseFirestore.getInstance()
                .collection("chat_room")
                .document(chatRoomId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String senderId = doc.getString("chatRoomId");
                        if (senderId != null && !senderId.isEmpty()) {
                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(senderId)
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists()) {
                                            String username = userDoc.getString("username");
                                            if (username != null) {
                                                topAppBar.setTitle(username);
                                            } else {
                                                topAppBar.setTitle("Unknown");
                                            }
                                        }
                                    });
                        }
                    }
                });

        topAppBar.setNavigationOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        setupRecyclerView();
        setSendListener();
        fetchInitialMessages();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter(requireContext(), messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(0);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastVisibleItemPosition() >= messageList.size() - 1) {
                    loadMoreMessages();
                }
            }
        });
    }

    private void fetchInitialMessages() {
        isLoading = true;
        messageList.clear();

        chatRoomRepo.getChatMessagesQuery(chatRoomId, "timestamp", "desc", PAGE_SIZE)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (!querySnapshots.isEmpty()) {
                        lastVisible = querySnapshots.getDocuments().get(querySnapshots.size() - 1);
                        for (DocumentSnapshot doc : querySnapshots) {
                            ChatMessage msg = doc.toObject(ChatMessage.class);
                            messageList.add(msg);
                        }
                        adapter.notifyItemRangeInserted(0, messageList.size());
                        isLastPage = querySnapshots.size() < PAGE_SIZE;
                    } else {
                        isLastPage = true;
                    }
                    isLoading = false;
                    startListeningForMessages();
                });
    }

    private void startListeningForMessages() {
        listenerRegistration = chatRoomRepo.getChatMessagesQuery(chatRoomId, "timestamp", "desc", PAGE_SIZE)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        ChatMessage msg = dc.getDocument().toObject(ChatMessage.class);
                        switch (dc.getType()) {
                            case ADDED:
                                if (!messageExists(msg)) {
                                    messageList.add(0, msg);
                                    adapter.notifyItemInserted(0);
                                }
                                break;
                            case MODIFIED:
                                handleModifiedMessage(msg);
                                break;
                            case REMOVED:
                                handleRemovedMessage(msg);
                                break;
                        }
                    }
                });
    }

    private void handleModifiedMessage(ChatMessage modifiedMsg) {
        for (int i = 0; i < messageList.size(); i++) {
            ChatMessage current = messageList.get(i);
            if (equalsMessage(current, modifiedMsg)) {
                messageList.set(i, modifiedMsg);
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void handleRemovedMessage(ChatMessage removedMsg) {
        for (int i = 0; i < messageList.size(); i++) {
            ChatMessage current = messageList.get(i);
            if (equalsMessage(current, removedMsg)) {
                messageList.remove(i);
                adapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    private boolean messageExists(ChatMessage message) {
        for (ChatMessage m : messageList) {
            if (equalsMessage(m, message)) return true;
        }
        return false;
    }

    private boolean equalsMessage(ChatMessage a, ChatMessage b) {
        return a.getTimestamp() != null && b.getTimestamp() != null &&
                a.getTimestamp().equals(b.getTimestamp()) &&
                a.getSenderId().equals(b.getSenderId());
    }

    private void loadMoreMessages() {
        if (isLoading || isLastPage) return;
        isLoading = true;

        chatRoomRepo.getChatMessagesQuery(chatRoomId, "timestamp", "desc", PAGE_SIZE)
                .startAfter(lastVisible)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (!querySnapshots.isEmpty()) {
                        lastVisible = querySnapshots.getDocuments().get(querySnapshots.size() - 1);
                        for (DocumentSnapshot doc : querySnapshots) {
                            ChatMessage msg = doc.toObject(ChatMessage.class);
                            messageList.add(msg);
                        }
                        adapter.notifyDataSetChanged();
                        isLastPage = querySnapshots.size() < PAGE_SIZE;
                    } else {
                        isLastPage = true;
                    }
                    isLoading = false;
                });
    }

    private void setSendListener() {
        btnSend.setOnClickListener(v -> {
            String content = edtMessage.getText().toString().trim();
            if (TextUtils.isEmpty(content)) return;

            btnSend.setEnabled(false);
            ChatMessage msg = new ChatMessage(content, FireBaseUtil.currentUserId());
            msg.setTimestamp(Timestamp.now());

            chatRoomRepo.sendMessage(chatRoomId, msg, new BaseRepository.ItemCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    edtMessage.setText("");
                    btnSend.setEnabled(true);

                    chatRoomRepo.getRef().document(chatRoomId).update(
                            "lastMessage", content,
                            "lastMessageSenderId", msg.getSenderId(),
                            "lastMessageTimestamp", Timestamp.now()
                    );
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Gửi tin nhắn thất bại", Toast.LENGTH_SHORT).show();
                    btnSend.setEnabled(true);
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
