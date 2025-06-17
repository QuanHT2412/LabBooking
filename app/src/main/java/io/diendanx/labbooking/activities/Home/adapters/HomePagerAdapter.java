package io.diendanx.labbooking.activities.Home.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import io.diendanx.labbooking.activities.Home.fragments.ChatFragment;
import io.diendanx.labbooking.activities.Home.fragments.ProductsFragment;
import io.diendanx.labbooking.activities.Home.fragments.SettingsFragment;

public class HomePagerAdapter extends FragmentStateAdapter {
    public HomePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new ProductsFragment();
            case 1: return new ChatFragment();
            case 2: return new SettingsFragment();
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
