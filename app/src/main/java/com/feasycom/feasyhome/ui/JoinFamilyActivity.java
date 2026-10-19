package com.feasycom.feasyhome.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caverock.androidsvg.SVG;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.logic.FamilyModelFactory;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.feasycom.feasyhome.ui.dialog.TipsDialogOne;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class JoinFamilyActivity extends BaseActivity {

    private ImageView img_head;
    private View view_status;
    private RelativeLayout rela_back;
    private String str_data;
    private TextView txt_tips1, txt_acc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(JoinFamilyActivity.this);
        BarUtils.setStatusBarLightMode(JoinFamilyActivity.this, true);
        setContentView(R.layout.joinfamilyactivity);

        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        str_data = getIntent().getStringExtra("str_data");

        img_head = findViewById(R.id.img_head);
        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);
        txt_tips1 = findViewById(R.id.txt_tips1);
        txt_acc = findViewById(R.id.txt_acc);
        txt_acc.setOnClickListener(listener);


        handler.post(new Runnable() {
            @Override
            public void run() {

                try {

                    JSONObject jsonObject = JSON.parseObject(str_data);
                    JSONObject userInfo = jsonObject.getJSONObject("userInfo");
                    String avatar = userInfo.getString("avatar");

                    String nickname = userInfo.getString("nickname");

                    txt_tips1.setText("来自\"" + nickname + "\"的邀请\n授权后可以使用\"" + nickname + "\"的所有设备");

                    if (avatar.indexOf("svg+xml") != -1) {
                        Glide.with(JoinFamilyActivity.this).asBitmap().load(decodeSvgToBitmap(avatar)).apply(RequestOptions.circleCropTransform()).into(img_head);

                    } else {
                        Glide.with(JoinFamilyActivity.this).load(Api.FeasyHomeServer + avatar).apply(RequestOptions.circleCropTransform()).into(img_head);

                    }

                    LoadingDialog.getInstance().Show(JoinFamilyActivity.this, getString(R.string.requesting));
                    getInvate();


                } catch (Exception e) {

                }

            }
        });

    }


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view == rela_back) {
                finish();
            } else if (view == txt_acc) {

                AccFamily();

            }
        }
    };


    private String family_id = "";
    private String share_id = "";
    private String auth_type = "";

    private void getInvate() {

        JSONObject jsonObject = JSON.parseObject(str_data);
        share_id = jsonObject.getString("share_id");
        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("share_id", share_id);
        RequestOkhttp.getInstance().PostBody(Api.inviteUserPage, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = response.body().string();
                Log.e("vvvv", "========invate=======" + str);
                //{"code":1,"msg":"获取成功","time":1741844738,"data":{"share_user_id":119,"username":"18610510939","avatar":"\/uploads\/20250312\/d1b07f11abb5eee3523acb96be1a006e.jpg","family_name":"01","family_id":396,"auth_type":"member"}}


                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LoadingDialog.getInstance().Dismiss();
                            JSONObject jsonObject1 = JSON.parseObject(str);
                            if (jsonObject1.getIntValue("code") == 1) {
                                txt_acc.setVisibility(View.VISIBLE);
                                JSONObject json_data = jsonObject1.getJSONObject("data");
                                family_id = json_data.getString("family_id");
                                auth_type = json_data.getString("auth_type");
                                share_id = json_data.getString("share_user_id");
                            }
                        } catch (Exception e) {

                        }
                    }
                });


            }
        });

    }

    private void AccFamily() {

        LoadingDialog.getInstance().Show(JoinFamilyActivity.this, getString(R.string.requesting));

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", family_id);
        formbody.add("auth_type", auth_type);
        formbody.add("share_user_id", share_id);

        Log.e("vvvvv", "=====AccFamily======" + family_id + "   " + auth_type + "     " + share_id);
        RequestOkhttp.getInstance().PostBody(Api.joinFamily, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = response.body().string();
                Log.e("vvvv", "======接受邀请==========" + str);

                //  {"code":0,"msg":"管理员才可邀请","time":1741845297,"data":null}
                //  {"code":1,"msg":"加入成功","time":1741845560,"data":null}
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LoadingDialog.getInstance().Dismiss();
                            JSONObject jsonObject = JSON.parseObject(str);
                            ToastUtils.showLong(jsonObject.getString("msg"));
                            if (jsonObject.getIntValue("code") == 1) {

                                TipsDialogOne tipsDialogOne = new TipsDialogOne(JoinFamilyActivity.this);
                                tipsDialogOne.show();
                                tipsDialogOne.txt_title.setText(jsonObject.getString("msg"));
                                tipsDialogOne.txt_cancler.setText("好的");
                                tipsDialogOne.txt_cancler.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        FamilyModelFactory.getInstance().getFamilyModel().getNeedrefresh().postValue(System.currentTimeMillis()+"家庭加入");
                                        tipsDialogOne.dismiss();
                                        finish();
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

    Handler handler = new Handler();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
