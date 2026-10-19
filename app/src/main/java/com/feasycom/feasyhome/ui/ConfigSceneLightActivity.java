package com.feasycom.feasyhome.ui;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.BarUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.adapter.GroupDevAdapter;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.base.DevBean;
import com.feasycom.feasyhome.bean.SceneBean;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.feasycom.feasyhome.ui.dialog.TipsDialogOne;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;

import no.nordicsemi.android.mesh.fsdk.FscMeshSdk;
import no.nordicsemi.android.mesh.fsdk.GroupDataCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class ConfigSceneLightActivity extends BaseActivity {

    private View view_status;
    private SceneBean sceneBean;
    private SmartRefreshLayout refreshLayout;
    private TextView txt_locaName, txt_save;
    private GroupDevAdapter groupDevAdapter;
    private RecyclerView recyclerview_dev;
    private SeekBar seekbar_lightness, seekbar_lightct;
    private RelativeLayout rela_back;
    private TextView txt_pro_ct, txt_pro_ness;
    private String group_name, group_id, group_address, device_type, room_name, room_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(ConfigSceneLightActivity.this);
        BarUtils.setStatusBarLightMode(ConfigSceneLightActivity.this, true);
        setContentView(R.layout.configscenelightactivity);

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

        seekbar_lightness = findViewById(R.id.seekbar_lightness);
        seekbar_lightness.setOnSeekBarChangeListener(barChangeListener_ness);
        seekbar_lightct = findViewById(R.id.seekbar_lightct);
        seekbar_lightct.setOnSeekBarChangeListener(barChangeListener_ct);
        refreshLayout = findViewById(R.id.refreshLayout);
        txt_locaName = findViewById(R.id.txt_locaName);
        txt_pro_ct = findViewById(R.id.txt_pro_ct);
        txt_pro_ness = findViewById(R.id.txt_pro_ness);
        txt_save = findViewById(R.id.txt_save);
        txt_save.setOnClickListener(listener);

        txt_locaName.setText(room_name + " - " + group_name);
        recyclerview_dev = findViewById(R.id.recyclerview_dev);
        recyclerview_dev.setLayoutManager(new GridLayoutManager(ConfigSceneLightActivity.this, 4));
        groupDevAdapter = new GroupDevAdapter(ConfigSceneLightActivity.this);
        recyclerview_dev.setAdapter(groupDevAdapter);
        refreshLayout.setOnRefreshListener(onRefreshListener);
        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);

        refreshLayout.autoRefresh();

    }


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view == rela_back) {
                finish();
            } else if (view == txt_save) {
                LoadingDialog.getInstance().Show(ConfigSceneLightActivity.this, getString(R.string.requesting));
                long runtime = 0;
                if (seek_time == 0) {
                    runtime = 1000;
                } else {
                    runtime = 6000 - (System.currentTimeMillis() - seek_time);
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SaveGroupToScene();
                    }
                }, runtime);

            }
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
        formbody.add("type", "light");

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

                                TipsDialogOne tipsDialogOne = new TipsDialogOne(ConfigSceneLightActivity.this);
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

                                FscMeshSdk.getApi2().SceneBindLightGroup(Integer.parseInt(sceneBean.getScene_code()), group_address, byte_ness, byte_ct);

                            }
                        } catch (Exception e) {

                        }
                        LoadingDialog.getInstance().Dismiss();
                    }
                });

            }
        });

    }


    private byte byte_ct = 0x00;
    private byte byte_ness = 0x00;
    private long seek_time = 0l;

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

            byte_ct = (byte) (seekBar.getProgress() & 0xFF);
            FscMeshSdk.getApi2().ControlLightct(group_address, byte_ct);
            seek_time = System.currentTimeMillis();
            LoadingDialog.getInstance().Show(ConfigSceneLightActivity.this, getString(R.string.requesting));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LoadingDialog.getInstance().Dismiss();
                }
            }, 4000);

        }
    };


    SeekBar.OnSeekBarChangeListener barChangeListener_ness = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            txt_pro_ness.setText(i + "%");

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            byte_ness = (byte) (seekBar.getProgress() & 0xFF);
            FscMeshSdk.getApi2().ControlLightness(group_address, byte_ness);
            seek_time = System.currentTimeMillis();
            LoadingDialog.getInstance().Show(ConfigSceneLightActivity.this, getString(R.string.requesting));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LoadingDialog.getInstance().Dismiss();
                }
            }, 4000);

        }
    };


    private OnRefreshListener onRefreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh(@NonNull RefreshLayout refreshLayout) {
            getDevs();
        }
    };


    private void getDevs() {

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("room_id", room_id);
        formbody.add("address", group_address);
        RequestOkhttp.getInstance().PostBody(Api.getDevlist, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                refreshLayout.finishRefresh();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = response.body().string();

                Log.e("vvv", "=======获取设备列表===========" + str);
                try {
                    JSONObject jsonObject = JSON.parseObject(str);
                    if (jsonObject.getIntValue("code") == 1) {

                        JSONObject jsonData = jsonObject.getJSONObject("data");
                        JSONArray jsonArray = jsonData.getJSONArray("list");
                        if (jsonArray.size() > 0) {
                            ArrayList<DevBean> devBeans = new ArrayList<>();
                            for (int i = 0; i < jsonArray.size(); i++) {
                                DevBean devBean = JSON.parseObject(jsonArray.getString(i), DevBean.class);
                                devBeans.add(devBean);

                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    groupDevAdapter.setDevBeans(devBeans);

                                    FscMeshSdk.getApi2().GetGroupData(group_address, new GroupDataCallback() {
                                        @Override
                                        public void databack(byte[] accessPayload) {
                                            //  f2 11 02 20 15 c0 32 00 01
                                            int ness = Integer.parseInt(String.format("%02X", accessPayload[6]), 16);
                                            int ct = Integer.parseInt(String.format("%02X", accessPayload[7]), 16);
                                            seekbar_lightct.setProgress(ct);
                                            seekbar_lightness.setProgress(ness);
                                            byte_ness = accessPayload[6];
                                            byte_ct = accessPayload[7];
                                        }
                                    });
                                }
                            });
                        }

                    }
                }catch (Exception e){

                }
                refreshLayout.finishRefresh();
            }
        });

    }


    Handler handler = new Handler() {

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
