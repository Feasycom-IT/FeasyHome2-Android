package com.feasycom.feasyhome.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.caverock.androidsvg.SVG;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.bean.MemberBean;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class MemberListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context context;
    private List<MemberBean> memberBeans;

    public MemberListAdapter(Context context) {
        this.context = context;
    }


    public List<MemberBean> getMemberBeans() {
        return memberBeans;
    }

    public void setMemberBeans(List<MemberBean> memberBeans) {
        this.memberBeans = memberBeans;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.member_list_item, parent, false);

        return new MemberViewhold(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MemberViewhold) {

            MemberBean memberBean = memberBeans.get(position);
            ((MemberViewhold) holder).txt_name.setText(memberBean.getUsername());
          //  ((MemberViewhold) holder).img_head.setImageBitmap(base64SvgToBitmap(memberBean.getAvatar()));
            if (memberBean.getAuth_type().equals("manage")) {
                ((MemberViewhold) holder).img_del.setVisibility(View.INVISIBLE);
                ((MemberViewhold) holder).txt_manage.setVisibility(View.VISIBLE);

            } else {
                ((MemberViewhold) holder).img_del.setVisibility(View.VISIBLE);
                ((MemberViewhold) holder).txt_manage.setVisibility(View.INVISIBLE);


            }

        }

    }

    @Override
    public int getItemCount() {
        if (null == memberBeans) {
            return 0;
        }
        return memberBeans.size();
    }


    private class MemberViewhold extends RecyclerView.ViewHolder {

        private ImageView img_head, img_del;
        private TextView txt_name, txt_manage;

        public MemberViewhold(@NonNull View itemView) {
            super(itemView);

            img_head = itemView.findViewById(R.id.img_head);
            txt_name = itemView.findViewById(R.id.txt_name);
            txt_manage = itemView.findViewById(R.id.txt_manage);
            img_del = itemView.findViewById(R.id.img_del);

        }


    }


    /**
     * 将Base64编码的SVG字符串转换为Bitmap对象。
     *
     * @param base64Svg SVG字符串（Base64编码）
     * @return 生成的Bitmap对象
     */
    private Bitmap base64SvgToBitmap(String base64Svg) {
        try {
            // 解码Base64字符串
            byte[] decodedBytes = Base64.decode(base64Svg, Base64.DEFAULT);
            InputStream inputStream = new ByteArrayInputStream(decodedBytes);

            // 解析SVG
            SVG svg = SVG.getFromInputStream(inputStream);
            Picture picture = svg.renderToPicture();

            // 创建Bitmap
            Bitmap bitmap = Bitmap.createBitmap(picture.getWidth(), picture.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawPicture(picture);

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



}
