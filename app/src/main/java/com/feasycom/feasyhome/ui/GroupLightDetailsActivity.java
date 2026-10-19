package com.feasycom.feasyhome.ui;


import android.content.DialogInterface;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.adapter.GroupDevAdapter;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.base.DevBean;
import com.feasycom.feasyhome.bean.DevGroupBean;
import com.feasycom.feasyhome.logic.FamilyModelFactory;
import com.feasycom.feasyhome.ui.dialog.DelDialog;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.feasycom.feasyhome.ui.dialog.TipsDialogOne;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;

import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.fsdk.FscMeshNetworkCallback;
import no.nordicsemi.android.mesh.fsdk.FscMeshSdk;
import no.nordicsemi.android.mesh.fsdk.GroupDataCallback;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class GroupLightDetailsActivity extends BaseActivity {


    private View view_status;
    private TextView txt_title, txt_locaName, txt_del, txt_pro_ct, txt_pro_ness;
    private RelativeLayout rela_back, rela_all_close, rela_all_open, rela_refresh;

    private DevGroupBean devGroupBean;
    private RecyclerView recyclerview_dev;
    private SmartRefreshLayout refreshLayout;
    private GroupDevAdapter groupDevAdapter;

    private SeekBar seekbar_lightct, seekbar_lightness;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(GroupLightDetailsActivity.this);
        BarUtils.setStatusBarLightMode(GroupLightDetailsActivity.this, true);

        setContentView(R.layout.grouplightdetailsactivity);

        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        seekbar_lightct = findViewById(R.id.seekbar_lightct);
        seekbar_lightness = findViewById(R.id.seekbar_lightness);
        seekbar_lightct.setOnSeekBarChangeListener(barChangeListener_ct);
        seekbar_lightness.setOnSeekBarChangeListener(barChangeListener_ness);

        txt_pro_ct = findViewById(R.id.txt_pro_ct);
        txt_pro_ness = findViewById(R.id.txt_pro_ness);
        rela_refresh = findViewById(R.id.rela_refresh);
        rela_refresh.setOnClickListener(listener);
        rela_all_close = findViewById(R.id.rela_all_close);
        rela_all_close.setOnClickListener(listener);
        rela_all_open = findViewById(R.id.rela_all_open);
        rela_all_open.setOnClickListener(listener);
        recyclerview_dev = findViewById(R.id.recyclerview_dev);
        recyclerview_dev.setLayoutManager(new GridLayoutManager(GroupLightDetailsActivity.this, 4));
        groupDevAdapter = new GroupDevAdapter(GroupLightDetailsActivity.this);
        recyclerview_dev.setAdapter(groupDevAdapter);
        refreshLayout = findViewById(R.id.refreshLayout);


        devGroupBean = (DevGroupBean) getIntent().getSerializableExtra("devGroupBean");

        txt_title = findViewById(R.id.txt_title);
        txt_locaName = findViewById(R.id.txt_locaName);
        txt_del = findViewById(R.id.txt_del);
        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);
        txt_del.setOnClickListener(listener);
        txt_title.setText("照明");
        txt_locaName.setText(devGroupBean.getGroup_name());

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                getDevList();
            }
        });
        refreshLayout.autoRefresh();
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

            if (FscMeshSdk.getApi2().isConnect()) {
                LoadingDialog.getInstance().Show(GroupLightDetailsActivity.this, getString(R.string.requesting));
                byte ct = (byte) (seekBar.getProgress() & 0xFF);
                FscMeshSdk.getApi2().ControlLightct(devGroupBean.getAddress(), ct);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LoadingDialog.getInstance().Dismiss();
                    }
                }, 300);

            } else {
                ToastUtils.showShort("网络未连接！");
            }
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

            if (FscMeshSdk.getApi2().isConnect()) {
                LoadingDialog.getInstance().Show(GroupLightDetailsActivity.this, getString(R.string.requesting));
                byte ness = (byte) (seekBar.getProgress() & 0xFF);
                FscMeshSdk.getApi2().ControlLightness(devGroupBean.getAddress(), ness);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LoadingDialog.getInstance().Dismiss();
                    }
                }, 300);

            } else {
                ToastUtils.showShort("网络未连接！");
            }

        }
    };


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view == rela_refresh) {
                if (FscMeshSdk.getApi2().isConnect()) {
                    handler.removeMessages(1001);
                    handler.sendEmptyMessage(1001);
                } else {
                    ToastUtils.showShort("网络未连接！");
                }


            } else if (view == rela_back) {
                finish();
            } else if (view == txt_del) {

                DelDialog delDialog = new DelDialog(GroupLightDetailsActivity.this);
                delDialog.show();
                if (FscMeshSdk.getApi2().isConnect()) {
                    delDialog.txt_title.setText("确认删除该群组！");
                } else {
                    delDialog.txt_title.setText("未连接网络，仅删除本地数据！");
                }

                delDialog.txt_cancler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        delDialog.dismiss();
                    }
                });
                delDialog.txt_del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        delDialog.dismiss();
                        LoadingDialog.getInstance().Show(GroupLightDetailsActivity.this, getString(R.string.requesting));
                        deleteGroup(devGroupBean);
                    }
                });

            } else if (view == rela_all_open) {
                if (FscMeshSdk.getApi2().isConnect()) {
                    LoadingDialog.getInstance().Show(GroupLightDetailsActivity.this, getString(R.string.requesting));
                    handler.removeMessages(1001);
                    FscMeshSdk.getApi2().ControlDevOnOff(devGroupBean.getAddress(), (byte) 0x01);
                    handler.sendEmptyMessageDelayed(1001, 3000);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LoadingDialog.getInstance().Dismiss();
                        }
                    }, 200);
                } else {
                    ToastUtils.showShort("网络未连接！");
                }
                //    Log.e("vvvvv", "====ffffff========" + FscMeshSdk.getApi2().getMeshNetwork().getIvIndex().toString()+"     "+FscMeshSdk.getApi2().getMeshNetwork().getSequenceNumbers().toString());


            } else if (view == rela_all_close) {

                if (FscMeshSdk.getApi2().isConnect()) {
                    LoadingDialog.getInstance().Show(GroupLightDetailsActivity.this, getString(R.string.requesting));
                    handler.removeMessages(1001);
                    FscMeshSdk.getApi2().ControlDevOnOff(devGroupBean.getAddress(), (byte) 0x00);
                    seekbar_lightness.setProgress(0);
                    handler.sendEmptyMessageDelayed(1001, 3000);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LoadingDialog.getInstance().Dismiss();
                        }
                    }, 200);
                } else {
                    ToastUtils.showShort("网络未连接！");
                }


            }


        }
    };

    private boolean needDisconnet = false;

    private void deleteGroup(DevGroupBean devGroupBean) {
        needDisconnet = false;
        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", devGroupBean.getFamily_id() + "");
        formbody.add("room_id", devGroupBean.getRoom_id() + "");
        formbody.add("address", devGroupBean.getAddress());
        Log.e("vvv", "=======删除群组=======" + devGroupBean.getGroup_name() + "  " + devGroupBean.getRoom_id() + "    " + devGroupBean.getAddress());

        RequestOkhttp.getInstance().PostBody(Api.deleteGroup, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
                ToastUtils.showLong("删除失败!");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                //  {"code":1,"msg":"操作成功","time":1729839308,"data":null}
                String str = response.body().string();
                Log.e("vvvv", "========删除群组========" + str);

                try {
                    JSONObject jsonObject = JSON.parseObject(str);
                    if (jsonObject.getIntValue("code") == 1) {
                        FscMeshSdk.getApi2().DeleteGroup(devGroupBean.getAddress());
                    }

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LoadingDialog.getInstance().Dismiss();
                            if (jsonObject.getIntValue("code") == 1) {

                                for (int i = 0; i < groupDevAdapter.getDevBeans().size(); i++) {
                                    ProvisionedMeshNode meshNode = FscMeshSdk.getApi2().getNode(Integer.parseInt(groupDevAdapter.getDevBeans().get(i).getDestination()));
                                    if (null != meshNode) {
                                        if (meshNode.getNodeName().equals(FscMeshSdk.getApi2().getConnectedAdr())) {
                                            needDisconnet = true;
                                        }
                                        FscMeshSdk.getApi2().deleteNode(meshNode);

                                    }


                                }


                                TipsDialogOne tipsDialogOne = new TipsDialogOne(GroupLightDetailsActivity.this);
                                tipsDialogOne.show();
                                tipsDialogOne.txt_title.setText("删除成功！");
                                tipsDialogOne.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        FamilyModelFactory.getInstance().getFamilyModel().getRoomid().setValue(devGroupBean.getRoom_id());

                                        if (needDisconnet) {
                                            FscMeshSdk.getApi2().Disconnect();
                                        }
                                        finish();
                                    }
                                });
                                tipsDialogOne.txt_cancler.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        tipsDialogOne.dismiss();

                                    }
                                });

                                RequestOkhttp.getInstance().SyncMeshData();


                            }
                        }
                    }, 2000);
                } catch (Exception e) {

                }
            }


        });


    }


    private void getDevList() {

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("room_id", devGroupBean.getRoom_id() + "");
        formbody.add("address", devGroupBean.getAddress());
        RequestOkhttp.getInstance().PostBody(Api.getDevlist, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                refreshLayout.finishRefresh();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = response.body().string();
                refreshLayout.finishRefresh();
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
                                }
                            });
                            handler.sendEmptyMessage(1001);
                        }

                    }
                } catch (Exception e) {

                }
            }
        });


    }


    private void DelDevfromGroup(DevBean devBean) {

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", devBean.getFamily_id() + "");
        formbody.add("room_id", devBean.getRoom_id() + "");
        formbody.add("address", devBean.getAddress());
        formbody.add("mac", devBean.getMac());


        RequestOkhttp.getInstance().PostBody(Api.delDevfromGroup, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.e("vvv", "=====DelDevfromGroup=========" + devBean.getAddress() + "    " + devBean.getDevice_id() + "    " + devBean.getId());

                FscMeshSdk.getApi2().ResetNode(Integer.parseInt(devBean.getDevice_id()));
                FscMeshSdk.getApi2().deleteNode(FscMeshSdk.getApi2().getNode(Integer.parseInt(devBean.getDevice_id())));

            }
        });


    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    try {
                        FscMeshSdk.getApi2().GetGroupData(devGroupBean.getAddress(), new GroupDataCallback() {
                            @Override
                            public void databack(byte[] accessPayload) {
                                //  f2 11 02 20 15 c0 32 00 01
                                int ness = Integer.parseInt(String.format("%02X", accessPayload[6]), 16);
                                int ct = Integer.parseInt(String.format("%02X", accessPayload[7]), 16);
                                seekbar_lightct.setProgress(ct);
                                seekbar_lightness.setProgress(ness);
                                Log.e("vvvv", "========亮度  " + ness + "   色温  " + ct);
                            }
                        });

                    } catch (Exception e) {
                        ToastUtils.showLong("数据异常！" + e.toString());
                        Log.e("vvvv","====="+e.toString());
                    }

                    break;
            }
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
        handler.removeMessages(1001);
        handler.removeMessages(1001);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
