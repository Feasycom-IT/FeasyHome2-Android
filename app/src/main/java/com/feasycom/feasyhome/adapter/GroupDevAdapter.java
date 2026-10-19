package com.feasycom.feasyhome.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.base.DevBean;

import java.util.ArrayList;

public class GroupDevAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context context;

    public GroupDevAdapter(Context context) {
        this.context = context;
    }

    private ArrayList<DevBean> devBeans = new ArrayList<>();

    public ArrayList<DevBean> getDevBeans() {
        return devBeans;
    }

    public void setDevBeans(ArrayList<DevBean> devBeans) {
        this.devBeans = devBeans;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dev_item, parent, false);
        return new DevViewhold(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DevViewhold) {

            DevBean devBean = devBeans.get(holder.getAdapterPosition());
            if (devBean.getDevice_type().equals("1")) {
                ((DevViewhold) holder).txt_devName.setText("灯" + (position + 1));
            }


        }

    }

    @Override
    public int getItemCount() {

        if (null == devBeans) {
            return 0;
        }
        return devBeans.size();

    }


    private class DevViewhold extends RecyclerView.ViewHolder {

        TextView txt_devName;

        public DevViewhold(@NonNull View itemView) {
            super(itemView);
            txt_devName = itemView.findViewById(R.id.txt_devName);

        }
    }

}
