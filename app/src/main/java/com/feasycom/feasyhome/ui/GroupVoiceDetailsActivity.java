package com.feasycom.feasyhome.ui;


import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.provider.FontsContractCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.base.DevBean;
import com.feasycom.feasyhome.bean.DevGroupBean;
import com.feasycom.feasyhome.bean.LightTableBean;
import com.feasycom.feasyhome.bean.RoomBean;
import com.feasycom.feasyhome.logic.FamilyModelFactory;
import com.feasycom.feasyhome.ui.dialog.DelDialog;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.feasycom.feasyhome.ui.dialog.TipsDialogOne;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import no.nordicsemi.android.mesh.fsdk.FscMeshSdk;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class GroupVoiceDetailsActivity extends BaseActivity {


    private View view_status;
    private TextView txt_title, txt_devname;
    private RelativeLayout rela_back;
    private DevGroupBean devGroupBean;
    private LinearLayout line_del_voice, line_sync;

    private SmartRefreshLayout refreshLayout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(GroupVoiceDetailsActivity.this);
        BarUtils.setStatusBarLightMode(GroupVoiceDetailsActivity.this, true);
        setContentView(R.layout.groupvoicedetailsactivity);

        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        refreshLayout = findViewById(R.id.refreshLayout);
        txt_devname = findViewById(R.id.txt_devname);
        devGroupBean = (DevGroupBean) getIntent().getSerializableExtra("devGroupBean");
        txt_devname.setText(devGroupBean.getDevice_name());

        line_del_voice = findViewById(R.id.line_del_voice);
        line_del_voice.setOnClickListener(listener);
        line_sync = findViewById(R.id.line_sync);
        line_sync.setOnClickListener(listener);

        txt_title = findViewById(R.id.txt_title);
        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);
        txt_title.setText("语音");

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

                getLightList();

            }
        });
        refreshLayout.autoRefresh();
    }


    ArrayList<LightTableBean> lightTableBeans = new ArrayList<>();
    ArrayList<LightTableBean> curtainTableBeans = new ArrayList<>();
    private byte[][] light_tables = new byte[33][];

    private final long MIN_CLICK_INTERVAL = 600; // 最小点击间隔时间，单位毫秒
    private long mLastClickTime;

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view == rela_back) {
                finish();
            } else if (view == line_del_voice) {
                DelDialog delDialog = new DelDialog(GroupVoiceDetailsActivity.this);
                delDialog.show();
                delDialog.txt_title.setText("确认删除该语音！");
                delDialog.txt_cancler.setText("取消");
                delDialog.txt_cancler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        delDialog.dismiss();
                    }
                });
                delDialog.txt_del.setText("确认");
                delDialog.txt_del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        delDialog.dismiss();
                        LoadingDialog.getInstance().Show(GroupVoiceDetailsActivity.this, getString(R.string.requesting));
                        DelVoice();
                    }
                });

            } else if (view == line_sync) {


                long currentClickTime = SystemClock.uptimeMillis();
                long elapsedTime = currentClickTime - mLastClickTime;
                mLastClickTime = currentClickTime;

                if (elapsedTime <= MIN_CLICK_INTERVAL) {

                } else if (TextUtils.isEmpty(light_tableData) && TextUtils.isEmpty(curtain_tableData)) {
                    ToastUtils.showShort("暂无数据同步！");
                    refreshLayout.autoRefresh();
                } else if (light_tb_nub == -1 || curtain_tb_nub == -1) {
                    ToastUtils.showShort("网络异常，请重试！");
                    refreshLayout.autoRefresh();
                } else {


                    lightTableBeans.clear();
                    curtainTableBeans.clear();


                    if (!TextUtils.isEmpty(light_tableData)) {
                        String[] light_tb = light_tableData.split(",");

                        for (int i = 0; i < light_tb.length; i++) {
                            String[] light_adr = light_tb[i].split(":");
                            LightTableBean lightTableBean = new LightTableBean();
                            lightTableBean.setHang(Integer.parseInt(light_adr[0]));
                            lightTableBean.setLie(Integer.parseInt(light_adr[1]));
                            lightTableBean.setAdr(Integer.parseInt(light_adr[2], 16));
                            lightTableBeans.add(lightTableBean);
                        }

                    }

                    if (!TextUtils.isEmpty(curtain_tableData)) {
                        String[] curtain_tb = curtain_tableData.split(",");
                        for (int i = 0; i < curtain_tb.length; i++) {
                            String[] light_adr = curtain_tb[i].split(":");
                            LightTableBean lightTableBean = new LightTableBean();
                            lightTableBean.setHang(Integer.parseInt(light_adr[0]));
                            lightTableBean.setLie(Integer.parseInt(light_adr[1]));
                            lightTableBean.setAdr(Integer.parseInt(light_adr[2], 16));
                            curtainTableBeans.add(lightTableBean);
                        }
                    }
                    Log.e("vvvvv", "=======light_tables.length============" + lightTableBeans.size() + "     " + curtainTableBeans.size() + "    " + light_tables.length);
                    if (lightTableBeans.size() > 0 || curtainTableBeans.size() > 0) {
                        for (int i = 0; i < light_tables.length; i++) {
                            if (i == 32) {
                                light_tables[i] = new byte[]{(byte) 64, (byte) room_code, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

                            } else {
                                light_tables[i] = new byte[]{(byte) i, (byte) room_code, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

                            }
                        }
                        light_tb_nub = 0;
                        curtain_tb_nub = 0;

                        if (lightTableBeans.size() > 0) {
                            for (int i = 0; i < lightTableBeans.size(); i++) {
                                light_tables[lightTableBeans.get(i).getHang()][lightTableBeans.get(i).getLie() + 2] = (byte) lightTableBeans.get(i).getAdr();
                            }
                        }
                        if (curtainTableBeans.size() > 0) {
                            for (int i = 0; i < curtainTableBeans.size(); i++) {
                                light_tables[32][curtainTableBeans.get(i).getLie() + 2] = (byte) curtainTableBeans.get(i).getAdr();
                            }
                        }


                        DelDialog tipsDialogOne = new DelDialog(GroupVoiceDetailsActivity.this);
                        tipsDialogOne.show();
                        tipsDialogOne.txt_title.setText("确认同步数据");
                        tipsDialogOne.txt_cancler.setText("取消");
                        tipsDialogOne.txt_del.setText("确认");
                        tipsDialogOne.txt_cancler.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                tipsDialogOne.dismiss();
                            }
                        });
                        tipsDialogOne.txt_del.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                handler.sendEmptyMessage(1001);
                                tipsDialogOne.dismiss();
                                LoadingDialog.getInstance().Show(GroupVoiceDetailsActivity.this, getString(R.string.requesting));
                            }
                        });


                    }


                }

            }

        }
    };


    private boolean needDisconnet = false;

    private void DelVoice() {
        needDisconnet = false;
        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", devGroupBean.getFamily_id() + "");
        formbody.add("mac", devGroupBean.getMac());
        RequestOkhttp.getInstance().PostBody(Api.deleteVoice, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();


            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String str = response.body().string();
                Log.e("vvv", "========语音退网=========" + str + "    " + devGroupBean.getDestination());
                JSONObject jsonObject = JSON.parseObject(str);
                if (jsonObject.getIntValue("code") == 1) {

                    FscMeshSdk.getApi2().ResetNode(devGroupBean.getDestination());
                }

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        LoadingDialog.getInstance().Dismiss();
                        if (jsonObject.getIntValue("code") == 1) {

                            ProvisionedMeshNode meshNode = FscMeshSdk.getApi2().getNode(devGroupBean.getDestination());
                            if (meshNode.getNodeName().equals(FscMeshSdk.getApi2().getConnectedAdr())) {
                                needDisconnet = true;
                            }
                            FscMeshSdk.getApi2().deleteNode(meshNode);

                            TipsDialogOne tipsDialogOne = new TipsDialogOne(GroupVoiceDetailsActivity.this);
                            tipsDialogOne.show();
                            tipsDialogOne.txt_title.setText("删除成功！");
                            tipsDialogOne.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    finish();
                                }
                            });
                            tipsDialogOne.txt_cancler.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    FamilyModelFactory.getInstance().getFamilyModel().getRoomid().setValue(devGroupBean.getRoom_id());

                                    if (needDisconnet) {
                                        FscMeshSdk.getApi2().Disconnect();
                                    }
                                    tipsDialogOne.dismiss();

                                }
                            });

                            RequestOkhttp.getInstance().SyncMeshData();


                        }

                    }
                }, 2000);


            }
        });

    }


    private String curtain_tableData = "";

    private void getCurtainList() {

        Map<String, Object> params10 = new HashMap<>();
        params10.put("mac", devGroupBean.getMac());
        RequestOkhttp.getInstance().Get(RequestOkhttp.getInstance().getUrl(Api.FeasyHomeServer + "/api/voice/curtainTableList", params10), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                refreshLayout.finishRefresh();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String str = response.body().string();
                Log.e("vvv", "=======语音   窗帘组列表======" + str);
                try {
                    JSONObject jsonObject = JSON.parseObject(str);
                    if (jsonObject.getIntValue("code") == 1) {
                        JSONObject json_data = jsonObject.getJSONObject("data");
                        curtain_tableData = json_data.getString("tableData");
                        curtain_tb_nub = json_data.getIntValue("first_index");

                    }
                    refreshLayout.finishRefresh();
                } catch (Exception e) {
                    refreshLayout.finishRefresh();
                }
            }
        });

    }

    private String light_tableData = "";
    private int room_code = 1;

    private void getLightList() {

        Map<String, Object> params10 = new HashMap<>();
        params10.put("mac", devGroupBean.getMac());
        RequestOkhttp.getInstance().Get(RequestOkhttp.getInstance().getUrl(Api.FeasyHomeServer + "/api/voice/lightTableList", params10), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                refreshLayout.finishRefresh();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String str = response.body().string();
                Log.e("vvv", "=======语音   灯光组列表======" + str);

                try {
                    JSONObject jsonObject = JSON.parseObject(str);
                    if (jsonObject.getIntValue("code") == 1) {
                        JSONObject json_data = jsonObject.getJSONObject("data");
                        light_tableData = json_data.getString("tableData");
                        light_tb_nub = json_data.getIntValue("first_index");
                        room_code = json_data.getIntValue("room_code");
                    }
                    getCurtainList();
                } catch (Exception e) {

                }


            }
        });

    }


    private void getVoiceDetail() {

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("mac", devGroupBean.getMac());
        RequestOkhttp.getInstance().PostBody(Api.voiceDetail, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                refreshLayout.finishRefresh();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                refreshLayout.finishRefresh();
                String str = response.body().string();
                Log.e("vvv", "=======语音详情======" + str);


            }
        });


    }


    private int light_tb_nub = -1;
    private int curtain_tb_nub = -1;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    //0-15    0-31
                    try {
                        if (light_tb_nub < light_tables.length) {

                        //    Log.e("vvvv", "=======数据大小========" + light_tb_nub + "   " + light_tables[light_tb_nub].length + "   " + bytesToHex(light_tables[light_tb_nub]));
                            FscMeshSdk.getApi2().SyncVoice(devGroupBean.getDestination(), light_tables[light_tb_nub]);

                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (light_tb_nub == 32) {
                                        FscMeshSdk.getApi2().SynconeB(devGroupBean.getDestination(), room_code, (byte) 64);

                                    } else {
                                        FscMeshSdk.getApi2().SynconeB(devGroupBean.getDestination(), room_code, (byte) light_tb_nub);
                                    }
                                    light_tb_nub++;
                                    handler.sendEmptyMessageDelayed(1001, 400);
                                }
                            }, 300);


                        } else {
                            Log.e("vvv", "========灯光同步完毕==========");
                            LoadingDialog.getInstance().Dismiss();
                            TipsDialogOne tipsDialogOne = new TipsDialogOne(GroupVoiceDetailsActivity.this);
                            tipsDialogOne.show();
                            tipsDialogOne.txt_title.setText("同步完成！");
                            tipsDialogOne.txt_cancler.setText("确定");
                            tipsDialogOne.txt_cancler.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    tipsDialogOne.dismiss();
                                    finish();
                                }
                            });

                        }
                    } catch (Exception e) {
                        Log.e("vvvv", "==========同步数据异常=======" + e.toString());
                    }


                    break;
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
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
