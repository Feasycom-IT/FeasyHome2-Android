package com.feasycom.feasyhome.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.bean.RoomBean;
import com.feasycom.feasyhome.callback.ItemClickCallback;

import java.util.ArrayList;
import java.util.List;

public class RoomListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context context;
    private List<RoomBean> roomBeans = new ArrayList<>();
    private ItemClickCallback itemClickCallback;

    public RoomListAdapter(Context context, ItemClickCallback itemClickCallback) {
        this.context = context;
        this.itemClickCallback = itemClickCallback;

    }


    public List<RoomBean> getRoomBeans() {
        return roomBeans;
    }

    public void setRoomBeans(List<RoomBean> roomBeans) {
        this.roomBeans = roomBeans;
        notifyDataSetChanged();

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.roomlist_item, parent, false);

        return new RoomViewhold(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RoomViewhold) {
            RoomBean roomBean = roomBeans.get(holder.getLayoutPosition());
            ((RoomViewhold) holder).txt_name.setText(roomBean.getRoom_name());

            ((RoomViewhold) holder).rela_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickCallback.onItemClick(holder.getLayoutPosition(),"修改名称");
                }
            });

            ((RoomViewhold) holder).rela_del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickCallback.onItemClick(holder.getLayoutPosition(),"删除房间");

                }
            });


        }

    }

    @Override
    public int getItemCount() {
        if (null == roomBeans) {
            return 0;
        }
        return roomBeans.size();
    }


    private class RoomViewhold extends RecyclerView.ViewHolder {

        private RelativeLayout rela_del, rela_edit;
        private TextView txt_name, txt_dev_nub;

        public RoomViewhold(@NonNull View itemView) {
            super(itemView);
            rela_del = itemView.findViewById(R.id.rela_del);
            txt_name = itemView.findViewById(R.id.txt_name);
            txt_dev_nub = itemView.findViewById(R.id.txt_dev_nub);
            rela_edit = itemView.findViewById(R.id.rela_edit);


        }
    }


}
