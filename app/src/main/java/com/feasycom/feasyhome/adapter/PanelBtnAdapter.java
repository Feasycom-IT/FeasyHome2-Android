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
import com.feasycom.feasyhome.bean.PanelBtnBean;
import com.feasycom.feasyhome.callback.ItemClickCallback;

import java.util.ArrayList;


public class PanelBtnAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<PanelBtnBean> panelBtnBeans = new ArrayList<>();
    private ItemClickCallback itemClickCallback;

    public PanelBtnAdapter(Context context, ItemClickCallback itemClickCallback) {
        this.context = context;
        this.itemClickCallback = itemClickCallback;
    }

    public void setPanelBtnBeans(ArrayList<PanelBtnBean> panelBtnBeans) {
        this.panelBtnBeans = panelBtnBeans;
        notifyDataSetChanged();
    }


    public ArrayList<PanelBtnBean> getPanelBtnBeans() {
        return panelBtnBeans;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.panelbtn_item, parent, false);
        return new PanelBtnView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof PanelBtnView) {
            PanelBtnBean panelBtnBean = panelBtnBeans.get(holder.getLayoutPosition());
            ((PanelBtnView) holder).txt_name.setText(panelBtnBean.getTitle());
            ((PanelBtnView) holder).line_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickCallback.onItemClick(holder.getLayoutPosition(), "");
                }
            });

            if (panelBtnBean.getSelect().equals("true")) {

                ((PanelBtnView) holder).line_btn.setBackgroundResource(R.drawable.shap_white_sbk6dp);
                ((PanelBtnView) holder).txt_name.setTextColor(context.getColor(R.color.txt_blue));

                if (TextUtils.isEmpty(panelBtnBean.getBind_id())){
                    ((PanelBtnView) holder).img_icon.setBackgroundResource(R.mipmap.panel_btn_icon_unbind_active);
                }else {
                    ((PanelBtnView) holder).img_icon.setBackgroundResource(R.mipmap.panel_btn_icon_bind_active);
                }

            } else {

                ((PanelBtnView) holder).line_btn.setBackgroundResource(R.drawable.shap_white_ss6dp);
                ((PanelBtnView) holder).txt_name.setTextColor(context.getColor(R.color.txt_none));

                if (TextUtils.isEmpty(panelBtnBean.getBind_id())){
                    ((PanelBtnView) holder).img_icon.setBackgroundResource(R.mipmap.panel_btn_icon_unbind_inactive);
                }else {
                    ((PanelBtnView) holder).img_icon.setBackgroundResource(R.mipmap.panel_btn_icon_bind_inactive);
                }

            }

        }


    }

    @Override
    public int getItemCount() {
        if (null == panelBtnBeans) {
            return 0;
        }
        return panelBtnBeans.size();
    }


    private class PanelBtnView extends RecyclerView.ViewHolder {

        private TextView txt_name;
        private LinearLayout line_btn;
        private ImageView img_icon;

        public PanelBtnView(@NonNull View itemView) {
            super(itemView);

            txt_name = itemView.findViewById(R.id.txt_name);
            line_btn = itemView.findViewById(R.id.line_btn);
            img_icon = itemView.findViewById(R.id.img_icon);

        }

    }


}
