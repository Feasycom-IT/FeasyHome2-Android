package com.feasycom.feasyhome.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.bean.SceneGroupBean;
import com.feasycom.feasyhome.callback.ItemClickCallback;

import java.util.ArrayList;
import java.util.HashMap;

public class SceneGroupsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ItemClickCallback itemClickCallback;
    private HashMap<Integer, String> hashMap_room = new HashMap<>();

    public SceneGroupsAdapter(Context context, ItemClickCallback itemClickCallback) {
        this.context = context;
        this.itemClickCallback = itemClickCallback;
    }

    private ArrayList<SceneGroupBean> sceneGroupBeans;

    public void setDevGroupBeans(ArrayList<SceneGroupBean> sceneGroupBeans) {
        this.sceneGroupBeans = sceneGroupBeans;
        notifyDataSetChanged();
    }

    public void setHashMap_room(HashMap<Integer, String> hashMap_room) {
        this.hashMap_room = hashMap_room;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.scene_group_item, parent, false);
        return new GroupItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GroupItem) {
            SceneGroupBean sceneGroupBean = sceneGroupBeans.get(holder.getLayoutPosition());

            if (TextUtils.isEmpty(hashMap_room.get(sceneGroupBean.getRoom_id()))) {
                ((GroupItem) holder).txt_name.setText(sceneGroupBean.getGroup_name());
            } else {
                ((GroupItem) holder).txt_name.setText(hashMap_room.get(sceneGroupBean.getRoom_id()) + " " + sceneGroupBean.getGroup_name());
            }

            ((GroupItem) holder).txt_nub.setText(sceneGroupBean.getDevice_count() + "设备");
            ((GroupItem) holder).rela_del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickCallback.onItemClick(holder.getLayoutPosition(), "del");
                }
            });

            ((GroupItem) holder).rela_bg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickCallback.onItemClick(holder.getLayoutPosition(), "edit");
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        if (null == sceneGroupBeans) {
            return 0;
        }
        return sceneGroupBeans.size();
    }

    private class GroupItem extends RecyclerView.ViewHolder {

        private RelativeLayout rela_del, rela_bg;
        private TextView txt_name, txt_nub;

        public GroupItem(@NonNull View itemView) {
            super(itemView);

            rela_del = itemView.findViewById(R.id.rela_del);
            txt_name = itemView.findViewById(R.id.txt_name);
            txt_nub = itemView.findViewById(R.id.txt_nub);
            rela_bg = itemView.findViewById(R.id.rela_bg);

        }
    }

}
