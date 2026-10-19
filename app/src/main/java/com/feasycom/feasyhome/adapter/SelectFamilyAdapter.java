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

import com.blankj.utilcode.util.SPUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.bean.FamilyBean;
import com.feasycom.feasyhome.callback.ItemClickCallback;

import java.util.List;

public class SelectFamilyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<FamilyBean> familyBeans;

    private ItemClickCallback itemClickCallback;


    public SelectFamilyAdapter(Context mcontext, ItemClickCallback mitemClickCallback) {
        context = mcontext;
        itemClickCallback = mitemClickCallback;

    }

    public List<FamilyBean> getFamilyBeans() {
        return familyBeans;
    }

    public void setFamilyBeans(List<FamilyBean> familyBeans) {
        this.familyBeans = familyBeans;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.selectfamily_item, parent, false);

        return new FamilyViewHold(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof FamilyViewHold) {

            FamilyBean familyBean = familyBeans.get(holder.getLayoutPosition());
            ((FamilyViewHold) holder).txt_name.setText(familyBean.getFamily_name());
            if (holder.getLayoutPosition() == SPUtils.getInstance().getInt("select_family", 0)) {
                ((FamilyViewHold) holder).img_gou.setVisibility(View.VISIBLE);
            } else {
                ((FamilyViewHold) holder).img_gou.setVisibility(View.INVISIBLE);
            }

            ((FamilyViewHold) holder).rela_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    itemClickCallback.onItemClick(holder.getLayoutPosition(), "");

                }
            });

        }

    }

    @Override
    public int getItemCount() {
        if (null == familyBeans) {
            return 0;
        }
        return familyBeans.size();
    }


    private class FamilyViewHold extends RecyclerView.ViewHolder {

        private TextView txt_name;
        private ImageView img_gou;
        private RelativeLayout rela_btn;

        public FamilyViewHold(@NonNull View itemView) {
            super(itemView);
            txt_name = itemView.findViewById(R.id.txt_name);
            img_gou = itemView.findViewById(R.id.img_gou);
            rela_btn = itemView.findViewById(R.id.rela_btn);

        }

    }


}
