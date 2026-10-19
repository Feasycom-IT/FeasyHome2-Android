package com.feasycom.feasyhome.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.bean.DevGroupBean;
import com.feasycom.feasyhome.callback.ItemClickCallback2;

import java.util.List;

public class IndexGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ItemClickCallback2 itemClickCallback;

    public IndexGroupAdapter(Context context, ItemClickCallback2 itemClickCallback) {
        this.context = context;
        this.itemClickCallback = itemClickCallback;
    }


    private List<DevGroupBean> devGroupBeans;

    public List<DevGroupBean> getLightGroupBeans() {
        return devGroupBeans;
    }

    public void setLightGroupBeans(List<DevGroupBean> devGroupBeans) {
        this.devGroupBeans = devGroupBeans;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.indexgroup_item, parent, false);

        return new GroupViewHold(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof GroupViewHold) {
            DevGroupBean devGroupBean = devGroupBeans.get(holder.getLayoutPosition());
            ((GroupViewHold) holder).txt_name.setText(devGroupBean.getGroup_name());
            ((GroupViewHold) holder).txt_nub.setText(devGroupBean.getDevice_count() + "设备  " + devGroupBean.getAddress() + "C0");

            if (devGroupBean.getDevice_type().equals("01")) {

                ((GroupViewHold) holder).img_dev.setBackgroundResource(R.mipmap.dev_img_light);

            } else if (devGroupBean.getDevice_type().equals("02")) {

                ((GroupViewHold) holder).img_dev.setBackgroundResource(R.mipmap.dev_img_panel);
                ((GroupViewHold) holder).txt_name.setText(devGroupBean.getDevice_name());
                ((GroupViewHold) holder).txt_nub.setText("1设备  " + "XXC0");

            } else if (devGroupBean.getDevice_type().equals("05")) {

                ((GroupViewHold) holder).img_dev.setBackgroundResource(R.mipmap.dev_img_curtain);

            }else if (devGroupBean.getDevice_type().equals("04")){

                ((GroupViewHold) holder).img_dev.setBackgroundResource(R.mipmap.search_img_audio);
                ((GroupViewHold) holder).txt_name.setText(devGroupBean.getDevice_name());
                ((GroupViewHold) holder).txt_nub.setText("1设备  " + "XXC0");

            }
            ((GroupViewHold) holder).line_bg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        itemClickCallback.onItemClick(holder.getLayoutPosition(), devGroupBean);

                    }catch (Exception e){

                    }
                }
            });

        }

    }

    @Override
    public int getItemCount() {

        if (null == devGroupBeans) {
            return 0;
        }
        return devGroupBeans.size();

    }


    private class GroupViewHold extends RecyclerView.ViewHolder {

        private TextView txt_name, txt_nub;
        private LinearLayout line_bg;
        private ImageView img_dev;


        public GroupViewHold(@NonNull View itemView) {
            super(itemView);
            txt_name = itemView.findViewById(R.id.txt_name);
            txt_nub = itemView.findViewById(R.id.txt_nub);
            line_bg = itemView.findViewById(R.id.line_bg);
            img_dev = itemView.findViewById(R.id.img_dev);

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
