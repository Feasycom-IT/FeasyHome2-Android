package com.feasycom.feasyhome.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
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
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.bean.DevGroupBean;
import com.feasycom.feasyhome.bean.RoomBean;
import com.feasycom.feasyhome.logic.FamilyModelFactory;
import com.feasycom.feasyhome.ui.dialog.DelDialog;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.feasycom.feasyhome.ui.dialog.SingleInputDialog;
import com.feasycom.feasyhome.ui.dialog.TipsDialogOne;

import java.io.IOException;

import no.nordicsemi.android.mesh.fsdk.FscMeshSdk;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class PanelDetailsSetActivity extends BaseActivity {


    private View view_status;
    private RelativeLayout rela_back, rela_panel_name;

    private SeekBar seekbar_panel_light;
    private TextView txt_pro_ct, txt_dev_name;
    private DevGroupBean devGroupBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BarUtils.transparentStatusBar(PanelDetailsSetActivity.this);
        BarUtils.setStatusBarLightMode(PanelDetailsSetActivity.this, true);
        setContentView(R.layout.paneldetailssetactivity);

        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        devGroupBean = (DevGroupBean) getIntent().getSerializableExtra("devGroupBean");

        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);
        seekbar_panel_light = findViewById(R.id.seekbar_panel_light);
        seekbar_panel_light.setOnSeekBarChangeListener(barChangeListener_ct);
        txt_pro_ct = findViewById(R.id.txt_pro_ct);
        txt_dev_name = findViewById(R.id.txt_dev_name);
        txt_dev_name.setText(devGroupBean.getDevice_name());

        rela_panel_name = findViewById(R.id.rela_panel_name);
        rela_panel_name.setOnClickListener(listener);


    }


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
            FscMeshSdk.getApi2().SetPanelBtnLight(devGroupBean.getDestination(), ct);
        }
    };


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view == rela_back) {

                finish();

            } else if (view == rela_panel_name) {

                SingleInputDialog singleInputDialog = new SingleInputDialog(PanelDetailsSetActivity.this);
                singleInputDialog.show();
                singleInputDialog.txt_title.setText("重命名面板");
                singleInputDialog.edit_name.setHint(devGroupBean.getDevice_name());
                singleInputDialog.txt_ok.setText("确定");
                singleInputDialog.txt_cancler.setText("取消");
                singleInputDialog.txt_cancler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        singleInputDialog.dismiss();
                    }
                });

                singleInputDialog.txt_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setName = singleInputDialog.edit_name.getText().toString().trim();
                        if (TextUtils.isEmpty(setName)) {
                            ToastUtils.showLong("面板名称不能为空。");
                        } else if (setName.equals(devGroupBean.getDevice_name())) {
                            singleInputDialog.dismiss();
                        } else {
                            singleInputDialog.dismiss();
                            FormBody.Builder formbody = new FormBody.Builder();
                            formbody.add("mac", devGroupBean.getMac());
                            formbody.add("enable", devGroupBean.getEnable() + "");
                            formbody.add("percent", devGroupBean.getPercent() + "");
                            formbody.add("device_name", setName);
                            formbody.add("family_id", devGroupBean.getFamily_id() + "");
                            SetPanel(formbody);
                            LoadingDialog.getInstance().Show(PanelDetailsSetActivity.this, getString(R.string.requesting));
                        }

                    }
                });

            }
        }
    };




    private String setName = "";

    private void SetPanel(FormBody.Builder formbody) {

        RequestOkhttp.getInstance().PostBody(Api.setPanel, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                LoadingDialog.getInstance().Dismiss();
                String str = response.body().string();
                Log.e("vvv", "=======修改按钮名称======" + str);
                //{"code":0,"msg":"名称2-6个字符","time":1735546769,"data":null}
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject jsonObject = JSON.parseObject(str);
                            if (jsonObject.getIntValue("code") == 1) {
                                txt_dev_name.setText(setName);
                            }
                            ToastUtils.showLong(jsonObject.getString("msg"));
                        } catch (Exception e) {

                        }

                    }
                });


            }
        });

    }





}
