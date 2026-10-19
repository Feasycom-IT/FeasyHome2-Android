package com.feasycom.feasyhome.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class FragAdapter extends FragmentStateAdapter {

    List<Fragment> mfragments = new ArrayList<>();
    private FragmentManager fragmentManager;

    public FragAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        this.fragmentManager = fragmentManager;
    }


    public void RemoveFragments() {

        if (mfragments.size() > 0) {
            for (int i = 0; i < mfragments.size(); i++) {
             //   Fragment fragmentToDelete = mfragments.remove(i);
             //   fragmentToDelete.getChildFragmentManager().clearBackStack(); // Clear any fragment transactions in the back stack
                fragmentManager.beginTransaction().remove(mfragments.remove(i)).commitNow();
            }
            mfragments.clear();
            notifyDataSetChanged();
        }

    }

    public void setFragments(List<Fragment> fragments) {

        mfragments.addAll(fragments);

    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mfragments.get(position);
    }

    @Override
    public int getItemCount() {
        return mfragments.size();
    }


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }
}
