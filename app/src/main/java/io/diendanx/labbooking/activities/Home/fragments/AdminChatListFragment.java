package io.diendanx.labbooking.activities.Home.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import io.diendanx.labbooking.R;
import io.diendanx.labbooking.activities.Home.adapters.ChatRoomListAdapter;
import io.diendanx.labbooking.models.ChatRoom;
import io.diendanx.labbooking.repository.ChatRoomRepository;

public class AdminChatListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatRoomListAdapter adapter;
    private ChatRoomRepository chatRoomRepository;
    private final List<ChatRoom> chatRoomList = new ArrayList<>();

    public AdminChatListFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_chat_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_chat_rooms);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ChatRoomListAdapter(chatRoomList, this::onChatRoomClicked);
        recyclerView.setAdapter(adapter);

        chatRoomRepository = new ChatRoomRepository();
        fetchChatRooms();

        return view;
    }

    private void fetchChatRooms() {
        chatRoomRepository.getRef()
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    chatRoomList.clear();
                    for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                        ChatRoom room = doc.toObject(ChatRoom.class);
                        if (room != null && room.getLastMessage() != null && room.getLastMessageSenderId() != null) {
                            chatRoomList.add(room);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void onChatRoomClicked(ChatRoom room) {
        if (room == null || room.getChatRoomId() == null || room.getChatRoomId().isEmpty()) {
            Toast.makeText(getContext(), "Chat room không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Fragment parent = getParentFragment();
        if (parent instanceof AdminChatContainerFragment) {
            ((AdminChatContainerFragment) parent).openChatDetail(room.getChatRoomId());
        } else {
            Toast.makeText(getContext(), "Không tìm thấy container", Toast.LENGTH_SHORT).show();
        }
    }
}
