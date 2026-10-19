package com.feasycom.feasyhome.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.bean.DevTypeBean;
import com.feasycom.feasyhome.callback.ItemSelectCallBack;

import java.util.List;

public class DevTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context context;
    private List<DevTypeBean> devTypeBeans;

    private ItemSelectCallBack itemSelectCallBack;


    public DevTypeAdapter(Context mcontext, ItemSelectCallBack mitemSelectCallBack) {
        context = mcontext;
        itemSelectCallBack = mitemSelectCallBack;
    }


    public List<DevTypeBean> getDevTypeBeans() {
        return devTypeBeans;
    }

    public void setDevTypeBeans(List<DevTypeBean> devTypeBeans) {
        this.devTypeBeans = devTypeBeans;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.devtype_item, parent, false);
        return new TypeViewhold(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof TypeViewhold) {

            DevTypeBean devTypeBean = devTypeBeans.get(holder.getLayoutPosition());
            ((TypeViewhold) holder).txt_name.setText(devTypeBean.getDevName());
            if (devTypeBean.getDevType() == (byte) 0x01) {
                ((TypeViewhold) holder).img_icon.setBackgroundResource(R.mipmap.dev_img_light);
            } else if (devTypeBean.getDevType() == (byte) 0x02) {
                ((TypeViewhold) holder).img_icon.setBackgroundResource(R.mipmap.search_img_panel);
            } else if (devTypeBean.getDevType() == (byte) 0x05) {
                ((TypeViewhold) holder).img_icon.setBackgroundResource(R.mipmap.dev_img_curtain);
            } else if (devTypeBean.getDevType() == (byte) 0x04) {
                ((TypeViewhold) holder).img_icon.setBackgroundResource(R.mipmap.search_img_audio);
            }

            if (devTypeBean.isSelect()) {
                ((TypeViewhold) holder).txt_name.setTextColor(context.getColor(R.color.txt_blue));
                ((TypeViewhold) holder).rela_bg.setBackgroundResource(R.drawable.shap_white_sbk6dp);

            } else {

                ((TypeViewhold) holder).txt_name.setTextColor(context.getColor(R.color.black));
                ((TypeViewhold) holder).rela_bg.setBackgroundResource(R.drawable.shap_white_ss5dp);

            }
            ((TypeViewhold) holder).rela_bg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemSelectCallBack.onSelect(holder.getLayoutPosition(), true);
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        if (null == devTypeBeans) {
            return 0;
        }
        return devTypeBeans.size();
    }


    private class TypeViewhold extends RecyclerView.ViewHolder {


        private ImageView img_icon;
        private TextView txt_name;
        private RelativeLayout rela_bg;

        public TypeViewhold(@NonNull View itemView) {
            super(itemView);

            img_icon = itemView.findViewById(R.id.img_icon);
            txt_name = itemView.findViewById(R.id.txt_name);
            rela_bg = itemView.findViewById(R.id.rela_bg);

        }


    }


}
