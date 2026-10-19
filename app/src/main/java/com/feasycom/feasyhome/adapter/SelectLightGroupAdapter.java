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
import com.feasycom.feasyhome.bean.DevGroupBean;
import com.feasycom.feasyhome.callback.ItemSelectCallBack;

import java.util.ArrayList;


public class SelectLightGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context context;
    private ItemSelectCallBack itemSelectCallBack;

    public SelectLightGroupAdapter(Context context, ItemSelectCallBack itemSelectCallBack) {
        this.context = context;
        this.itemSelectCallBack = itemSelectCallBack;
    }

    private ArrayList<DevGroupBean> devGroupBeans;

    public ArrayList<DevGroupBean> getLightGroupBeans() {
        return devGroupBeans;
    }

    public void setLightGroupBeans(ArrayList<DevGroupBean> devGroupBeans) {
        this.devGroupBeans = devGroupBeans;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.selectlightgroup_item, parent, false);

        return new LightGroupViewhold(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LightGroupViewhold) {
            DevGroupBean devGroupBean = devGroupBeans.get(position);
            ((LightGroupViewhold) holder).txt_name.setText(devGroupBean.getGroup_name()+"  "+devGroupBean.getAddress()+"C0");
            ((LightGroupViewhold) holder).txt_dev_count.setText(devGroupBean.getDevice_count() + "设备");
            ((LightGroupViewhold) holder).rela_bg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemSelectCallBack.onSelect(holder.getLayoutPosition(), true);
                }
            });

            if (devGroupBean.isSelect()){
                ((LightGroupViewhold) holder).rela_bg.setBackgroundResource(R.drawable.shap_white_sbk6dp);
            }else {
                ((LightGroupViewhold) holder).rela_bg.setBackgroundResource(R.drawable.shap_white_ss5dp);

            }


        }

    }

    @Override
    public int getItemCount() {
        if (null == devGroupBeans) {
            return 0;
        }
        return devGroupBeans.size();
    }


    private class LightGroupViewhold extends RecyclerView.ViewHolder {

        private TextView txt_name, txt_dev_count;
        private RelativeLayout rela_bg;

        public LightGroupViewhold(@NonNull View itemView) {
            super(itemView);
            txt_name = itemView.findViewById(R.id.txt_name);
            txt_dev_count = itemView.findViewById(R.id.txt_dev_count);
            rela_bg = itemView.findViewById(R.id.rela_bg);

        }

    }


    private byte[] intToTwoByteArray(int value) {
        if (value < 0 || value > 0xFFFF) {
            throw new IllegalArgumentException("Value must be between 0 and 65535");
        }

        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) ((value >> 8) & 0xFF); // 高字节
        byteArray[1] = (byte) (value & 0xFF);       // 低字节

        return byteArray;
    }


    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex + " ");
        }
        return hexString.toString();
    }


}
