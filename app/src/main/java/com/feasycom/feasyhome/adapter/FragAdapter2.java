package com.feasycom.feasyhome.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.feasycom.feasyhome.ui.RoomFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FragAdapter2 extends FragmentStateAdapter {

    List<RoomFragment> mfragments = new ArrayList<>();
    private FragmentManager fragmentManager;

    public FragAdapter2(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        this.fragmentManager = fragmentManager;
    }


    public void RemoveFragments() {

        if (mfragments.size() > 0) {
            for (int i = 0; i < mfragments.size(); i++) {
                //   Fragment fragmentToDelete = mfragments.remove(i);
                //   fragmentToDelete.getChildFragmentManager().clearBackStack(); // Clear any fragment transactions in the back stack
                fragmentManager.beginTransaction().remove(mfragments.get(i)).commitNow();

            }
            mfragments.clear();
            notifyDataSetChanged();
         //   Log.e("vvv", "=========清除  mfragments=======");
        }

    }

    public void setFragments(List<RoomFragment> fragments) {
        for (int i = 0; i < fragments.size(); i++) {
            try {
                RoomFragment roomFragment = fragments.get(i);
                Optional<RoomFragment> foundAuracastBean = mfragments.parallelStream()
                        .filter(roomFragment1 -> roomFragment1.getRoomBean().getId() == roomFragment.getRoomBean().getId())
                        .findFirst();
                if (!foundAuracastBean.isPresent()) {
                    mfragments.add(roomFragment);
                }
            } catch (Exception e) {

            }
        }
        //   mfragments.addAll(fragments);
        notifyDataSetChanged();

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
