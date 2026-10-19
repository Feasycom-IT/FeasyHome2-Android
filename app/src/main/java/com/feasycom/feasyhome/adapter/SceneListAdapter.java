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

import com.blankj.utilcode.util.ConvertUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.bean.SceneBean;
import com.feasycom.feasyhome.callback.ItemClickCallback3;

import java.util.ArrayList;

public class SceneListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<SceneBean> sceneBeans = new ArrayList<>();
    private ItemClickCallback3 itemClickCallback3;


    public SceneListAdapter(Context context, ItemClickCallback3 itemClickCallback3) {
        this.context = context;
        this.itemClickCallback3 = itemClickCallback3;
    }


    public ArrayList<SceneBean> getSceneBeans() {
        return sceneBeans;
    }

    public void setSceneBeans(ArrayList<SceneBean> sceneBeans) {
        this.sceneBeans = sceneBeans;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.scenelist_item, parent, false);

        return new SceneViewHold(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof SceneViewHold) {

            SceneBean sceneBean = sceneBeans.get(holder.getLayoutPosition());
            ((SceneViewHold) holder).txt_scene_name.setText(sceneBean.getScene_name());
            if (sceneBean.isSelect()) {
                ((SceneViewHold) holder).txt_scene_name.setBackgroundResource(R.drawable.shap_blue_bottom_circular);
            } else {
                ((SceneViewHold) holder).txt_scene_name.setBackgroundResource(R.drawable.shap_black_bottom_circular);

            }
            ((SceneViewHold) holder).txt_scene_name.getBackground().setAlpha(150);
            RequestOptions options = new RequestOptions()
                    .bitmapTransform(new RoundedCorners(ConvertUtils.dp2px(10)));
            Glide.with(context).asBitmap().load(Api.FeasyHomeServer + sceneBean.getScene_image()).apply(options).into(((SceneViewHold) holder).img_scene);
            ((SceneViewHold) holder).rela_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickCallback3.onItemClick(holder.getLayoutPosition(), 2, ((SceneViewHold) holder).rela_edit);
                }
            });
            ((SceneViewHold) holder).rela_bg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastClickTime > MIN_CLICK_INTERVAL) {
                        lastClickTime = currentTime;
                        itemClickCallback3.onItemClick(holder.getLayoutPosition(), 1, null);
                    }

                }
            });

        }
    }

    private final long MIN_CLICK_INTERVAL = 600; // 最小点击间隔时间，单位毫秒
    private long lastClickTime = 0;

    @Override
    public int getItemCount() {
        if (null == sceneBeans) {
            return 0;
        }
        return sceneBeans.size();
    }


    private class SceneViewHold extends RecyclerView.ViewHolder {


        private TextView txt_scene_name;
        private ImageView img_scene, img_scene_edit;
        private RelativeLayout rela_bg, rela_edit;

        public SceneViewHold(@NonNull View itemView) {
            super(itemView);

            txt_scene_name = itemView.findViewById(R.id.txt_scene_name);
            img_scene = itemView.findViewById(R.id.img_scene);
            img_scene_edit = itemView.findViewById(R.id.img_scene_edit);
            rela_bg = itemView.findViewById(R.id.rela_bg);
            rela_edit = itemView.findViewById(R.id.rela_edit);

        }
    }


}
