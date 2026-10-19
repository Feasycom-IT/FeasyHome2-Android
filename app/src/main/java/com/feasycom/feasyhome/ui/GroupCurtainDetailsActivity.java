package com.feasycom.feasyhome.ui;


import android.content.DialogInterface;
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

import no.nordicsemi.android.mesh.fsdk.FscMeshSdk;
import no.nordicsemi.android.mesh.fsdk.GroupDataCallback;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class GroupCurtainDetailsActivity extends BaseActivity {


    private View view_status;
    private TextView txt_title, txt_locaName, txt_del, txt_pro_ct;
    private RelativeLayout rela_back, rela_all_close, rela_all_open, rela_all_clear, rela_all_reverse, rela_all_refresh;
    private DevGroupBean devGroupBean;
    private SmartRefreshLayout refreshLayout;

    private SeekBar seekbar_lightct;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(GroupCurtainDetailsActivity.this);
        BarUtils.setStatusBarLightMode(GroupCurtainDetailsActivity.this, true);
        setContentView(R.layout.groupcurtaindetailsactivity);


        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        rela_all_clear = findViewById(R.id.rela_all_clear);
        rela_all_clear.setOnClickListener(listener);
        rela_all_reverse = findViewById(R.id.rela_all_reverse);
        rela_all_reverse.setOnClickListener(listener);
        rela_all_refresh = findViewById(R.id.rela_all_refresh);

        seekbar_lightct = findViewById(R.id.seekbar_lightct);
        seekbar_lightct.setOnSeekBarChangeListener(barChangeListener_ct);

        txt_pro_ct = findViewById(R.id.txt_pro_ct);


        rela_all_close = findViewById(R.id.rela_all_close);
        rela_all_close.setOnClickListener(listener);
        rela_all_open = findViewById(R.id.rela_all_open);
        rela_all_open.setOnClickListener(listener);

        refreshLayout = findViewById(R.id.refreshLayout);


        devGroupBean = (DevGroupBean) getIntent().getSerializableExtra("devGroupBean");

        txt_title = findViewById(R.id.txt_title);
        txt_locaName = findViewById(R.id.txt_locaName);
        txt_del = findViewById(R.id.txt_del);
        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);
        txt_del.setOnClickListener(listener);
        txt_title.setText("窗帘");
        txt_locaName.setText(devGroupBean.getGroup_name());

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                devBeans.clear();
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

            byte ct = (byte) (seekBar.getProgress() & 0xFF);
            FscMeshSdk.getApi2().ControlCurtainOpenPercent(devGroupBean.getAddress(), ct);
        }
    };


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view == rela_back) {
                finish();
            } else if (view == txt_del) {

                DelDialog delDialog = new DelDialog(GroupCurtainDetailsActivity.this);
                delDialog.show();
                delDialog.txt_title.setText("确认删除该群组！");
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
                        LoadingDialog.getInstance().Show(GroupCurtainDetailsActivity.this, getString(R.string.requesting));
                        deleteGroup(devGroupBean);
                    }
                });

            } else if (view == rela_all_open) {

                FscMeshSdk.getApi2().ControlDevOnOff(devGroupBean.getAddress(), (byte) 0x01);
                seekbar_lightct.setProgress(100);

            } else if (view == rela_all_close) {

                FscMeshSdk.getApi2().ControlDevOnOff(devGroupBean.getAddress(), (byte) 0x00);
                seekbar_lightct.setProgress(0);

            } else if (view == rela_all_clear) {
                FscMeshSdk.getApi2().ControlCurtainCleanMil(devGroupBean.getAddress());
            } else if (view == rela_all_reverse) {

                FscMeshSdk.getApi2().ControlCurtainDirection(devGroupBean.getAddress(), direction);
                if (direction == (byte) 0x01) {
                    direction = (byte) 0x00;
                } else {
                    direction = (byte) 0x01;
                }

            }

        }
    };


    private byte direction = (byte) 0x01;

    private ArrayList<DevBean> devBeans = new ArrayList<>();

    private boolean needDisconnet = false;

    private void deleteGroup(DevGroupBean devGroupBean) {
        needDisconnet = false;
        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", devGroupBean.getFamily_id() + "");
        formbody.add("room_id", devGroupBean.getRoom_id() + "");
        formbody.add("address", devGroupBean.getAddress());
        //    formbody.add("mac",devGroupBean.getMac());
        Log.e("vvv", "=======删除群组=======" + devGroupBean.getGroup_name() + "  " + devGroupBean.getRoom_id() + "    " + devGroupBean.getAddress());

        RequestOkhttp.getInstance().PostBody(Api.deletecurtainGroup, formbody, new Callback() {
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


                                for (int i = 0; i < devBeans.size(); i++) {
                                    ProvisionedMeshNode meshNode = FscMeshSdk.getApi2().getNode(Integer.parseInt(devBeans.get(i).getDestination()));
                                    if (meshNode.getNodeName().equals(FscMeshSdk.getApi2().getConnectedAdr())) {
                                        needDisconnet = true;
                                    }
                                    FscMeshSdk.getApi2().deleteNode(meshNode);
                                }

                                TipsDialogOne tipsDialogOne = new TipsDialogOne(GroupCurtainDetailsActivity.this);
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
        formbody.add("family_id", devGroupBean.getFamily_id() + "");
        formbody.add("room_id", devGroupBean.getRoom_id() + "");
        formbody.add("address", devGroupBean.getAddress());
        RequestOkhttp.getInstance().PostBody(Api.FeasyHomeServer + "/api/curtain/curtainList", formbody, new Callback() {
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

                            for (int i = 0; i < jsonArray.size(); i++) {
                                DevBean devBean = JSON.parseObject(jsonArray.getString(i), DevBean.class);
                                devBeans.add(devBean);

                            }

                        }

                    }
                } catch (Exception e) {

                }
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
