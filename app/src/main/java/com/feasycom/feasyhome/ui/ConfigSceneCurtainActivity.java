package com.feasycom.feasyhome.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.BarUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.bean.SceneBean;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.feasycom.feasyhome.ui.dialog.TipsDialogOne;

import java.io.IOException;

import no.nordicsemi.android.mesh.fsdk.FscMeshSdk;
import no.nordicsemi.android.mesh.fsdk.GroupDataCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class ConfigSceneCurtainActivity extends BaseActivity {

    private View view_status;
    private SceneBean sceneBean;
    private RelativeLayout rela_back;
    private TextView txt_locaName, txt_save, txt_pro_ct;
    private SeekBar seekbar_lightct;

    private String group_name, group_id, group_address, device_type, room_name, room_id;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BarUtils.transparentStatusBar(ConfigSceneCurtainActivity.this);
        BarUtils.setStatusBarLightMode(ConfigSceneCurtainActivity.this, true);
        setContentView(R.layout.configscenecurtainactivity);

        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        sceneBean = (SceneBean) getIntent().getSerializableExtra("sceneBean");
        group_name = getIntent().getStringExtra("group_name");
        group_id = getIntent().getStringExtra("group_id");
        group_address = getIntent().getStringExtra("group_address");
        device_type = getIntent().getStringExtra("device_type");
        room_name = getIntent().getStringExtra("room_name");
        room_id = getIntent().getStringExtra("room_id");

        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);
        txt_locaName = findViewById(R.id.txt_locaName);
        txt_save = findViewById(R.id.txt_save);
        txt_save.setOnClickListener(listener);
        txt_locaName.setText(room_name + " - " + group_name);

        seekbar_lightct = findViewById(R.id.seekbar_lightct);
        txt_pro_ct = findViewById(R.id.txt_pro_ct);
        seekbar_lightct.setOnSeekBarChangeListener(barChangeListener_ct);

        handler.post(new Runnable() {
            @Override
            public void run() {
                FscMeshSdk.getApi2().GetGroupData(group_address, new GroupDataCallback() {
                    @Override
                    public void databack(byte[] accessPayload) {
                        //  f2 11 02 20 15 c0 32 00 01
                        //  f2 11 02 50 60 c0 00 00
                        int openPercent = Integer.parseInt(String.format("%02X", accessPayload[6]), 16);
                        seekbar_lightct.setProgress(openPercent);

                    }
                });
            }
        });
    }


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view == rela_back) {
                finish();
            } else if (view == txt_save) {

                LoadingDialog.getInstance().Show(ConfigSceneCurtainActivity.this, getString(R.string.requesting));
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SaveGroupToScene();
                    }
                }, 3000);

            }

        }
    };


    SeekBar.OnSeekBarChangeListener barChangeListener_ct = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            txt_pro_ct.setText(i + "%");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            byte ct = (byte) (seekBar.getProgress() & 0xFF);
            FscMeshSdk.getApi2().ControlCurtainOpenPercent(group_address, ct);
        }
    };


    private void SaveGroupToScene() {

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("scene_code", sceneBean.getScene_code());
        formbody.add("family_id", sceneBean.getFamily_id() + "");
        formbody.add("room_id", room_id);
        formbody.add("address", group_address);
        formbody.add("device_type", device_type);
        formbody.add("group_id", group_id);
        formbody.add("type", "curtain");

        RequestOkhttp.getInstance().PostBody(Api.joinSceneGroup, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String str = response.body().string();
                Log.e("vvv", "=========保存配置=======" + str);
                //{"code":1,"msg":"加入成功","time":1739350546,"data":null}

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = JSON.parseObject(str);
                            if (jsonObject.getIntValue("code") == 1) {
                                TipsDialogOne tipsDialogOne = new TipsDialogOne(ConfigSceneCurtainActivity.this);
                                tipsDialogOne.show();
                                tipsDialogOne.txt_title.setText("保存成功！");
                                tipsDialogOne.txt_cancler.setText("确认");
                                tipsDialogOne.txt_cancler.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        tipsDialogOne.dismiss();
                                        finish();
                                    }
                                });

                                FscMeshSdk.getApi2().SceneBindCurtainGroup(Integer.parseInt(sceneBean.getScene_code()), group_address, (byte) seekbar_lightct.getProgress());

                            }
                        } catch (Exception e) {

                        }
                        LoadingDialog.getInstance().Dismiss();
                    }
                });

            }
        });

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
