package com.feasycom.feasyhome.ui;


import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caverock.androidsvg.SVG;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.bean.FamilyBean;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.feasycom.feasyhome.ui.dialog.TipsDialogOne;
import com.mylhyl.zxing.scanner.encode.QREncode;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class ShareQrcodeActivity extends BaseActivity {


    private View view_status;
    private RelativeLayout rela_back, rela_share;
    private String auth_type = "member";
    private FamilyBean familyBean;
    private ImageView img_head, img_qr;
    private LinearLayout line_refresh, line_bg;
    private TextView txt_tips1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(ShareQrcodeActivity.this);
        BarUtils.setStatusBarLightMode(ShareQrcodeActivity.this, true);
        setContentView(R.layout.shareqrcodeactiviy);

        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);


        familyBean = (FamilyBean) getIntent().getSerializableExtra("familyBean");
        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);
        img_head = findViewById(R.id.img_head);
        img_qr = findViewById(R.id.img_qr);
        line_refresh = findViewById(R.id.line_refresh);
        line_refresh.setOnClickListener(listener);
        txt_tips1 = findViewById(R.id.txt_tips1);
        line_bg = findViewById(R.id.line_bg);
        rela_share = findViewById(R.id.rela_share);
        rela_share.setOnClickListener(listener);


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LoadingDialog.getInstance().Show(ShareQrcodeActivity.this, getString(R.string.requesting));
                getQr(auth_type);
                //  来自“XX”的邀请授权后可以使用“XX”的所有设备

            }
        }, 200);
    }


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view == rela_back) {
                if (null != share_pop) {
                    share_pop.dismiss();
                }
                finish();
            } else if (view == line_refresh) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        LoadingDialog.getInstance().Show(ShareQrcodeActivity.this, getString(R.string.requesting));
                        getQr(auth_type);
                    }
                });

            } else if (view == rela_share) {

                ShowSharePop();

            }


        }
    };


    private void getQr(String type) {

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("auth_type", type);
        formbody.add("family_id", familyBean.getId() + "");
        RequestOkhttp.getInstance().PostBody(Api.shareQrcode, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = response.body().string();
                Log.e("vvv", "=======获取二维码内容==========" + str);
//{"code":0,"msg":"管理员才可邀请","time":1741846671,"data":null}
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        LoadingDialog.getInstance().Dismiss();
                        try {
                            JSONObject jsonObject = JSON.parseObject(str);
                            if (jsonObject.getIntValue("code") == 1) {
                                JSONObject json_data = jsonObject.getJSONObject("data");
                                JSONObject json_userInfo = json_data.getJSONObject("userInfo");
                                String avatar = json_userInfo.getString("avatar");

                                String nickname = json_userInfo.getString("nickname");

                                txt_tips1.setText("来自\"" + nickname + "\"的邀请\n授权后可以使用\"" + nickname + "\"的所有设备");

                                if (avatar.indexOf("svg+xml") != -1) {
                                    Glide.with(ShareQrcodeActivity.this).asBitmap().load(decodeSvgToBitmap(avatar)).apply(RequestOptions.circleCropTransform()).into(img_head);

                                } else {
                                    Glide.with(ShareQrcodeActivity.this).load(Api.FeasyHomeServer + avatar).apply(RequestOptions.circleCropTransform()).into(img_head);

                                }

                                //文本类型
                                Bitmap bitmap = new QREncode.Builder(ShareQrcodeActivity.this)
                                        //.setParsedResultType(ParsedResultType.TEXT)//默认是TEXT类型
                                        .setContents(json_data.toJSONString())//二维码内容
                                        .build().encodeAsBitmap();

                                Glide.with(ShareQrcodeActivity.this).asBitmap().load(bitmap).into(img_qr);

                            }else {

                                TipsDialogOne tipsDialogOne = new TipsDialogOne(ShareQrcodeActivity.this);
                                tipsDialogOne.show();
                                tipsDialogOne.txt_title.setText(jsonObject.getString("msg"));
                                tipsDialogOne.txt_cancler.setText("好的");
                                tipsDialogOne.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        finish();
                                    }
                                });
                                tipsDialogOne.txt_cancler.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        tipsDialogOne.dismiss();
                                    }
                                });


                            }

                        } catch (Exception e) {

                        }


                    }
                });


            }
        });

    }


    private PopupWindow share_pop;

    private void ShowSharePop() {


        View pop_view = LayoutInflater.from(ShareQrcodeActivity.this).inflate(R.layout.share_pop, null);
        // 设置PopupWindow的背景
        TextView txt_shiyong = pop_view.findViewById(R.id.txt_shiyong);
        TextView txt_guanli = pop_view.findViewById(R.id.txt_guanli);
        TextView txt_cancler = pop_view.findViewById(R.id.txt_cancler);
        RelativeLayout rela_pop_bg = pop_view.findViewById(R.id.rela_pop_bg);
        rela_pop_bg.getBackground().setAlpha(100);
        txt_shiyong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth_type = "member";
                share_pop.dismiss();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        LoadingDialog.getInstance().Show(ShareQrcodeActivity.this, getString(R.string.requesting));
                        getQr(auth_type);
                    }
                });

            }
        });
        txt_guanli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth_type = "manage";
                share_pop.dismiss();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        LoadingDialog.getInstance().Show(ShareQrcodeActivity.this, getString(R.string.requesting));
                        getQr(auth_type);
                    }
                });

            }
        });

        txt_cancler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                share_pop.dismiss();

            }
        });


        share_pop = new PopupWindow(pop_view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        share_pop.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 设置PopupWindow的动画
        share_pop.setAnimationStyle(R.style.PopupAnimation);
        // 设置PopupWindow是否可以获取焦点
        share_pop.setFocusable(false);
        // 设置PopupWindow是否可以触摸外部消失
        share_pop.setOutsideTouchable(false);
        share_pop.setElevation(3);
        share_pop.showAtLocation(line_bg, Gravity.BOTTOM, 0, 0);


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (null != share_pop) {
            share_pop.dismiss();
        }
        finish();
    }

    private Bitmap decodeSvgToBitmap(String base64Svg) {
        base64Svg = base64Svg.split(",")[1];
        // Decode the Base64 string
        byte[] decodedBytes = Base64.decode(base64Svg, Base64.DEFAULT);

        // Convert byte array to InputStream
        //   ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes);

        try {
            // Parse the SVG from the InputStream
            SVG svg = SVG.getFromString(new String(decodedBytes));

            // Create a Bitmap with the same dimensions as the SVG
            int width = (int) svg.getDocumentWidth();
            int height = (int) svg.getDocumentHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            // Draw the SVG onto the Bitmap
            Canvas canvas = new Canvas(bitmap);
            //  Paint paint = new Paint();
            svg.renderToCanvas(canvas);

            return bitmap;
        } catch (Exception e) {
            Log.e("SvgUtils", "Error parsing SVG", e);
            return null;
        }
    }


    private Bitmap base64ToBitmap(String base64Str) {
        // 检查Base64字符串是否包含前缀（如"data:image/jpeg;base64,"）
        if (base64Str.contains(",")) {
            base64Str = base64Str.split(",")[1];
        }

        // 将Base64字符串解码为字节数组
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);

        // 将字节数组转换为Bitmap对象
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }


    Handler handler = new Handler();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
