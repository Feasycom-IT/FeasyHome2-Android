package com.feasycom.feasyhome.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.adapter.FamilyListAdapter;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.bean.FamilyBean;
import com.feasycom.feasyhome.callback.ItemClickCallback;

import com.feasycom.feasyhome.logic.FamilyModelFactory;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.feasycom.feasyhome.ui.dialog.SingleInputDialog;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;
import java.util.Optional;

import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.fsdk.FscMeshNetworkCallback;
import no.nordicsemi.android.mesh.fsdk.FscMeshNetworksCallback;
import no.nordicsemi.android.mesh.fsdk.FscMeshSdk;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class FamilyListActivity extends BaseActivity implements ItemClickCallback {


    private View view_status;
    private RelativeLayout rela_back;
    private TextView txt_creat_family;
    private SmartRefreshLayout refreshLayout;

    private RecyclerView recyclerview;

    private ArrayList<FamilyBean> familyBeans = new ArrayList<>();
    private FamilyListAdapter familyListAdapter;

    private SingleInputDialog singleInputDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(FamilyListActivity.this);
        BarUtils.setStatusBarLightMode(FamilyListActivity.this, true);

        setContentView(R.layout.familylistactivity);

        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);
        txt_creat_family = findViewById(R.id.txt_creat_family);
        txt_creat_family.setOnClickListener(listener);

        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                GetFamilylist();
            }
        });
        recyclerview = findViewById(R.id.recyclerview);
        familyListAdapter = new FamilyListAdapter(FamilyListActivity.this, FamilyListActivity.this);
        recyclerview.setLayoutManager(new LinearLayoutManager(FamilyListActivity.this));
        recyclerview.setAdapter(familyListAdapter);


    }


    @Override
    protected void onResume() {
        super.onResume();
        refreshLayout.autoRefresh();

    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view == rela_back) {
                finish();
            } else if (view == txt_creat_family) {

                singleInputDialog = new SingleInputDialog(FamilyListActivity.this);
                singleInputDialog.show();
                singleInputDialog.txt_title.setText(R.string.createafamily);
                singleInputDialog.edit_name.setHint(R.string.tips18);
                singleInputDialog.edit_name.setMaxEms(10);
                singleInputDialog.txt_cancler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        singleInputDialog.dismiss();
                    }
                });
                singleInputDialog.txt_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        boolean exists = familyBeans.stream()
                                .anyMatch(bean -> bean.getFamily_name().equals(singleInputDialog.edit_name.getText().toString()));

                        if (TextUtils.isEmpty(singleInputDialog.edit_name.getText().toString().trim())) {
                            ToastUtils.showShort(R.string.tips18);
                        } else if (singleInputDialog.edit_name.getText().toString().trim().length() < 2) {
                            ToastUtils.showShort(R.string.tips19);
                        } else if (singleInputDialog.edit_name.getText().toString().trim().length() > 12) {
                            ToastUtils.showShort(R.string.tips20);
                        } else if (exists) {
                            ToastUtils.showShort(R.string.tips21);
                        } else {
                            CreateFamilyRequest(singleInputDialog.edit_name.getText().toString().trim());

                        }

                    }
                });


            }

        }
    };


    private void GetFamilylist() {


        FormBody.Builder formbody = new FormBody.Builder();
        RequestOkhttp.getInstance().PostBody(Api.FamilyList, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                familyBeans.clear();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        familyListAdapter.setFamilyBeans(familyBeans);

                    }
                });
                refreshLayout.finishRefresh();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                familyBeans.clear();
                String str_res = response.body().string();
                //{"code":1,"msg":"返回成功","time":1723190757,"data":[]}
                //Log.e("vvv", "=======家庭列表==========" + str_res);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            refreshLayout.finishRefresh();
                            JSONObject jsonObject = JSON.parseObject(str_res);
                            if (jsonObject.getIntValue("code") == 1) {

                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                if (jsonArray.size() <= 0) {
                                    //没有家庭,清空本地多余的Mesh网络数据
                                    FscMeshSdk.getApi2().QueryMeshNetworks(new FscMeshNetworksCallback() {
                                        @Override
                                        public void onMeshNetworks(List<MeshNetwork> meshNetworks) {
                                            if (null != meshNetworks) {
                                                for (int i = 0; i < meshNetworks.size(); i++) {
                                                    FscMeshSdk.getApi2().deleteMeshNetworkFromDb(meshNetworks.get(i));
                                                }
                                            }
                                        }
                                    });

                                } else {
                                    //有家庭

                                    for (int i = 0; i < jsonArray.size(); i++) {

                                        FamilyBean familyBean = JSON.parseObject(jsonArray.getString(i), FamilyBean.class);
                                        if (null != familyBean) {
                                            familyBeans.add(familyBean);
                                        }

                                    }
                                }

                                familyListAdapter.setFamilyBeans(familyBeans);
                                if (alreadyAddFamily) {
                                    FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().setValue(familyBeans);
                                }

                            }

                        } catch (Exception e) {

                        }

                    }
                });
            }
        });


    }


    private boolean alreadyAddFamily = false;

    private void CreateFamilyRequest(String familyname) {

        if (TextUtils.isEmpty(familyname)){

            if (null != singleInputDialog && singleInputDialog.isShowing()) {
                singleInputDialog.dismiss();
            }
            ToastUtils.showLong(R.string.tips22);
            return;
        }

        LoadingDialog.getInstance().Show(FamilyListActivity.this, getString(R.string.requesting));

        FscMeshSdk.getApi2().CreateMeshNetwork(familyname, new FscMeshNetworkCallback() {
            @Override
            public void onMeshNetwork(MeshNetwork meshNetwork) {

                if (null != meshNetwork) {
                    Log.e("vvv", "======CreateFamilyRequest=======" + meshNetwork.getMeshName());
                }

                try {

                    FormBody.Builder formbody = new FormBody.Builder();
                    formbody.add("familyName", meshNetwork.getMeshName());
                    formbody.add("networkData", EncodeUtils.base64Encode2String(FscMeshSdk.getApi2().exportMeshNetwork().getBytes("utf-8")));
                    formbody.add("networkId", meshNetwork.getMeshUUID());
                    formbody.add("familyStr", meshNetwork.getMeshUUID());

                    RequestOkhttp.getInstance().PostBody(Api.CreateFamily, formbody, new okhttp3.Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {

                            FscMeshSdk.getApi2().deleteMeshNetworkFromDb(meshNetwork);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    LoadingDialog.getInstance().Dismiss();
                                    ToastUtils.showShort("创建失败1");
                                    if (null != singleInputDialog && singleInputDialog.isShowing()) {
                                        singleInputDialog.dismiss();
                                    }
                                }
                            });

                            Log.e("vvv", "=======创建家庭=======" + e.toString());
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                            String str_res = response.body().string();
                            //  {"code":1,"msg":"创建成功","time":1723883117,"data":null}

                            Log.e("vvv", "=======创建家庭=2======" + str_res);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {

                                        JSONObject jsonObject = JSON.parseObject(str_res);
                                        LoadingDialog.getInstance().Dismiss();
                                        if (null != singleInputDialog && singleInputDialog.isShowing()) {
                                            singleInputDialog.dismiss();
                                        }
                                        if (jsonObject.getIntValue("code") == 1) {
                                            alreadyAddFamily = true;
                                            refreshLayout.autoRefresh();
                                        } else {
                                            FscMeshSdk.getApi2().deleteMeshNetworkFromDb(meshNetwork);
                                        }
                                        ToastUtils.showLong(jsonObject.getString("msg"));

                                    } catch (Exception e) {
                                        LoadingDialog.getInstance().Dismiss();
                                        ToastUtils.showLong("网络异常，请重试。");

                                    }


                                }
                            });

                        }
                    });

                } catch (Exception e) {
                    LoadingDialog.getInstance().Dismiss();
                }

            }
        });


    }


    @Override
    public void onItemClick(int p, String str) {

        Intent intent = new Intent(FamilyListActivity.this, FamilyDetailsActivity.class);
        intent.putExtra("familyBean", familyBeans.get(p));
        startActivity(intent);

    }

    Handler handler = new Handler();


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
