package io.diendanx.labbooking.activities.Home.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.diendanx.labbooking.R;

public class AdminChatContainerFragment extends Fragment {

    public AdminChatContainerFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_chat_container, container, false);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.chat_inner_container, new AdminChatListFragment())
                    .commit();
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
                            getChildFragmentManager().popBackStack();
                        } else {
                            setEnabled(false);
                            requireActivity().onBackPressed();
                        }
                    }
                });

        return view;
    }

    public void openChatDetail(String chatRoomId) {
        AdminChatDetailFragment detailFragment = new AdminChatDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("chatRoomId", chatRoomId);
        detailFragment.setArguments(bundle);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.chat_inner_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }
}
