package com.feasycom.feasyhome.ui;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.adapter.PanelBtnAdapter;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.bean.DevGroupBean;
import com.feasycom.feasyhome.bean.PanelBtnBean;
import com.feasycom.feasyhome.bean.RoomBean;
import com.feasycom.feasyhome.callback.ItemClickCallback;
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
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class GroupPanelDetailsActivity extends BaseActivity {


    private View view_status;
    private TextView txt_title, txt_locaName, txt_select_light_group, txt_select_scene, txt_select_curtain_group, txt_del;
    private RelativeLayout rela_back, rela_bg;
    private SmartRefreshLayout refreshLayout;
    private DevGroupBean devGroupBean;
    private RoomBean roombean;
    private RecyclerView recyclerview;
    private PanelBtnAdapter panelBtnAdapter;
    private ArrayList<PanelBtnBean> panelBtnBeans = new ArrayList<>();
    private TextView txt_btn_set, txt_unbind, txt_bind;
    private ImageView img_dev1, img_dev2, img_dev3, img_dev4, img_unbind, img_bind;
    private LinearLayout line_bind, line_unbind;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(GroupPanelDetailsActivity.this);
        BarUtils.setStatusBarLightMode(GroupPanelDetailsActivity.this, true);
        setContentView(R.layout.grouppaneldetailsactivity);


        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        devGroupBean = (DevGroupBean) getIntent().getSerializableExtra("devGroupBean");
        roombean = (RoomBean) getIntent().getSerializableExtra("roombean");
        recyclerview = findViewById(R.id.recyclerview);
        panelBtnAdapter = new PanelBtnAdapter(GroupPanelDetailsActivity.this, itemClickCallback);
        recyclerview.setLayoutManager(new GridLayoutManager(GroupPanelDetailsActivity.this, 2));
        recyclerview.setAdapter(panelBtnAdapter);
        SetSpacing();
        refreshLayout = findViewById(R.id.refreshLayout);
        img_unbind = findViewById(R.id.img_unbind);
        img_bind = findViewById(R.id.img_bind);
        txt_unbind = findViewById(R.id.txt_unbind);
        txt_bind = findViewById(R.id.txt_bind);
        img_dev1 = findViewById(R.id.img_dev1);
        img_dev2 = findViewById(R.id.img_dev2);
        img_dev1.setOnClickListener(listener);
        img_dev2.setOnClickListener(listener);
        txt_btn_set = findViewById(R.id.txt_btn_set);
        txt_btn_set.setOnClickListener(listener);
        txt_title = findViewById(R.id.txt_title);
        txt_locaName = findViewById(R.id.txt_locaName);
        txt_select_curtain_group = findViewById(R.id.txt_select_curtain_group);
        txt_select_curtain_group.setOnClickListener(listener);
        txt_del = findViewById(R.id.txt_del);
        txt_del.setOnClickListener(listener);

        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);
        img_dev3 = findViewById(R.id.img_dev3);
        img_dev3.setOnClickListener(listener);
        img_dev4 = findViewById(R.id.img_dev4);
        img_dev4.setOnClickListener(listener);

        txt_title.setText("面板");
        rela_bg = findViewById(R.id.rela_bg);
        txt_select_light_group = findViewById(R.id.txt_select_light_group);
        txt_select_light_group.setOnClickListener(listener);
        txt_select_scene = findViewById(R.id.txt_select_scene);
        txt_select_scene.setOnClickListener(listener);
        txt_locaName.setText(devGroupBean.getDevice_name());
        line_unbind = findViewById(R.id.line_unbind);
        line_bind = findViewById(R.id.line_bind);
        line_bind.setOnClickListener(listener);
        line_unbind.setOnClickListener(listener);


        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                line_bind.setEnabled(false);
                line_unbind.setEnabled(false);
                img_bind.setBackgroundResource(R.mipmap.panel_btn_icon_bind_inactive);
                img_unbind.setBackgroundResource(R.mipmap.panel_btn_icon_unbind_inactive);
                txt_bind.setTextColor(getResources().getColor(R.color.txt_none));
                txt_unbind.setTextColor(getResources().getColor(R.color.txt_none));
                getPaneBtnList();
                getPanelGrouplist();
            }
        });


    }


    private void SetSpacing() {

        // 设置横向间距
        int spanCount = 2; // 列数，根据需求设置
        int spacing = ConvertUtils.dp2px(15); // 间距大小
        boolean includeEdge = false; // 是否包括边缘

        recyclerview.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                int column = position % spanCount;

                if (includeEdge) {
                    outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((100 - spacing) / spanCount)
                    outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((100 - spacing) / spanCount)

                    if (position < spanCount) { // top edge
                        outRect.top = spacing;
                    }
                    outRect.bottom = spacing; // item bottom
                } else {
                    outRect.left = column * spacing / spanCount; // column * ((100 - spacing) / spanCount)
                    outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((100 - spacing) / spanCount)
                    if (position >= spanCount) {
                        outRect.top = spacing; // item top
                    }
                }
            }
        });

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
                if (null != pop_bind_light_group && pop_bind_light_group.isShowing()) {
                    pop_bind_light_group.dismiss();
                    return;
                }

                if (null != pop_bind_curtain_group && pop_bind_curtain_group.isShowing()) {
                    pop_bind_curtain_group.dismiss();
                    return;
                }

                if (null != pop_bind_scene && pop_bind_scene.isShowing()) {
                    pop_bind_scene.dismiss();
                    return;
                }

                finish();
            } else if (view == txt_btn_set) {

                Intent intent = new Intent(GroupPanelDetailsActivity.this, PanelDetailsSetActivity.class);
                intent.putExtra("devGroupBean", devGroupBean);
                startActivity(intent);

            } else if (view == txt_select_light_group) {

                if (null == lightGroupList || lightGroupList.size() == 0) {
                    refreshLayout.autoRefresh();
                    ToastUtils.showShort("请重试...");
                } else if (null != pop_bind_light_group && pop_bind_light_group.isShowing()) {
                    pop_bind_light_group.dismiss();
                } else {
                    ShowBindLightGroupPOP(rela_bg);
                    if (null != pop_bind_curtain_group && pop_bind_curtain_group.isShowing()) {
                        pop_bind_curtain_group.dismiss();
                    }
                    if (null != pop_bind_scene && pop_bind_scene.isShowing()) {
                        pop_bind_scene.dismiss();
                    }
                }

            } else if (view == txt_select_curtain_group) {

                if (null == curtainGroupList || curtainGroupList.size() == 0) {
                    refreshLayout.autoRefresh();
                    ToastUtils.showShort("请重试...");
                } else {
                    ShowBindCurtainGroupPOP(rela_bg);
                }

            } else if (view == txt_select_scene) {

                if (null == sceneList || sceneList.size() == 0) {
                    ToastUtils.showShort("请重试...");
                } else {
                    ShowBindScenePOP(rela_bg);
                }

            } else if (view == img_dev1) {
                //   绑定类别，灯组则为灯具类别如01（普通等）,06（色温灯）,窗帘组05，场景则1A，色温则1C
                select_type = "01";
                img_dev1.setBackgroundResource(R.mipmap.primary_icon_selected);
                img_dev2.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev3.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev4.setBackgroundResource(R.mipmap.primary_icon_unselected);
            } else if (view == img_dev2) {
                select_type = "05";
                img_dev1.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev2.setBackgroundResource(R.mipmap.primary_icon_selected);
                img_dev3.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev4.setBackgroundResource(R.mipmap.primary_icon_unselected);
            } else if (view == img_dev3) {
                select_type = "1A";
                img_dev1.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev2.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev3.setBackgroundResource(R.mipmap.primary_icon_selected);
                img_dev4.setBackgroundResource(R.mipmap.primary_icon_unselected);
            } else if (view == img_dev4) {
                select_type = "1C";
                img_dev1.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev2.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev3.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev4.setBackgroundResource(R.mipmap.primary_icon_selected);
            } else if (view == line_unbind) {

                if (select_type.equals("-1")) {
                    ToastUtils.showLong("请选择解绑内容");
                    return;
                }

                if (select_type.equals("01")) {

                    if (null == panelBtnBeans || panelBtnBeans.size() == 0) {
                        ToastUtils.showShort("请重试...");
                    } else {
                        LoadingDialog.getInstance().Show(GroupPanelDetailsActivity.this, getString(R.string.requesting));
                        unBindGroup();
                    }

                } else if (select_type.equals("05")) {

                    if (null == panelBtnBeans || panelBtnBeans.size() == 0) {
                        ToastUtils.showShort("请重试...");
                    } else {
                        LoadingDialog.getInstance().Show(GroupPanelDetailsActivity.this, getString(R.string.requesting));
                        unBindGroup();
                    }

                } else if (select_type.equals("1A")) {

                    if (null == panelBtnBeans || panelBtnBeans.size() == 0) {
                        ToastUtils.showShort("请重试...");
                    } else {
                        LoadingDialog.getInstance().Show(GroupPanelDetailsActivity.this, getString(R.string.requesting));
                        unBindScene();
                    }

                } else if (select_type.equals("1C")) {

                    if (null == panelBtnBeans || panelBtnBeans.size() == 0) {
                        ToastUtils.showShort("请重试...");
                    } else {
                        LoadingDialog.getInstance().Show(GroupPanelDetailsActivity.this, getString(R.string.requesting));
                        unBindGroup();
                    }

                }


            } else if (view == line_bind) {
                ////01普通灯，06RGB灯，05窗帘，1A场景，1B调光，1C色温

                if (select_type.equals("-1")) {
                    ToastUtils.showLong("请选择绑定内容");
                    return;
                }

                if (select_type.equals("01") && TextUtils.isEmpty(txt_select_light_group.getText().toString())) {
                    ToastUtils.showLong("请选择绑定内容");
                    return;
                }
                if (select_type.equals("05") && TextUtils.isEmpty(txt_select_curtain_group.getText().toString())) {
                    ToastUtils.showLong("请选择绑定内容");
                    return;
                }
                if (select_type.equals("1A") && TextUtils.isEmpty(txt_select_scene.getText().toString())) {
                    ToastUtils.showLong("请选择绑定内容");
                    return;
                }

                if (select_type.equals("01")) {
                    LoadingDialog.getInstance().Show(GroupPanelDetailsActivity.this, getString(R.string.requesting));
                    BindGroup(select_type);
                }

                if (select_type.equals("05")) {
                    LoadingDialog.getInstance().Show(GroupPanelDetailsActivity.this, getString(R.string.requesting));
                    BindGroup(select_type);
                }

                if (select_type.equals("1A")) {
                    LoadingDialog.getInstance().Show(GroupPanelDetailsActivity.this, getString(R.string.requesting));
                    BindScene();
                }
                if (select_type.equals("1C")) {
                    LoadingDialog.getInstance().Show(GroupPanelDetailsActivity.this, getString(R.string.requesting));
                    BindGroup(select_type);
                }
            } else if (view == txt_del) {

                DelDialog delDialog = new DelDialog(GroupPanelDetailsActivity.this);
                delDialog.show();
                delDialog.txt_title.setText("确认删除该面板！");
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
                        LoadingDialog.getInstance().Show(GroupPanelDetailsActivity.this, getString(R.string.requesting));
                        deleteGroup(devGroupBean);
                    }
                });

            }

        }
    };


    private boolean needDisconnet = false;

    private void deleteGroup(DevGroupBean devGroupBean) {
        needDisconnet = false;
        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", devGroupBean.getFamily_id() + "");
        formbody.add("room_id", devGroupBean.getRoom_id() + "");
        formbody.add("mac", devGroupBean.getMac());
        Log.e("vvv", "=======删除面板=======" + devGroupBean.getDevice_name() + "  " + devGroupBean.getRoom_id() + "    " + devGroupBean.getMac());

        RequestOkhttp.getInstance().PostBody(Api.deletePanel, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
                ToastUtils.showLong("删除失败!");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                //  {"code":1,"msg":"操作成功","time":1729839308,"data":null}
                String str = response.body().string();
                Log.e("vvvv", "========删除群组========" + str + "    " + devGroupBean.getDestination());

                try {
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

                                TipsDialogOne tipsDialogOne = new TipsDialogOne(GroupPanelDetailsActivity.this);
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
                                        ActivityUtils.finishActivity(GroupPanelDetailsActivity.class);
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

    String btn_id = "0";

    private void BindGroup(String select_type) {

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("mac", devGroupBean.getMac());
        formbody.add("family_id", devGroupBean.getFamily_id() + "");
        formbody.add("room_id", devGroupBean.getRoom_id() + "");
        formbody.add("second_type", devGroupBean.getSecond_type());

        for (int i = 0; i < panelBtnAdapter.getPanelBtnBeans().size(); i++) {
            if (panelBtnAdapter.getPanelBtnBeans().get(i).getSelect().equals("true")) {
                btn_id = i + "";
            }
        }
        formbody.add("btn_id", btn_id);
        if (select_type.equals("01")) {
            formbody.add("bind_id", lightGroupList.getJSONObject(light_name_nub).getString("address"));
            formbody.add("bind_type", lightGroupList.getJSONObject(light_name_nub).getString("device_type"));
        } else if (select_type.equals("05")) {
            formbody.add("bind_id", curtainGroupList.getJSONObject(curtain_name_nub).getString("address"));
            formbody.add("bind_type", curtainGroupList.getJSONObject(curtain_name_nub).getString("device_type"));
        } else if (select_type.equals("1C")) {
            formbody.add("bind_id", "00");
            formbody.add("bind_type", select_type);
        }
        formbody.add("do_type", "1");
        formbody.add("device_type", devGroupBean.getDevice_type());

        RequestOkhttp.getInstance().PostBody(Api.updateLayout, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String str = response.body().string();
                Log.e("vvvv", "=======绑定============" + str);
                //{"code":1,"msg":"操作成功","time":1735787561,"data":null}
                try {
                    JSONObject jsonObject = JSON.parseObject(str);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (jsonObject.getIntValue("code") == 1) {
                                if (select_type.equals("01")) {
                                    FscMeshSdk.getApi2().AT_GroupBindPanelBtn(devGroupBean.getDestination(), lightGroupList.getJSONObject(light_name_nub).getString("address"), (Integer.parseInt(btn_id) + 1) + "", "1");

                                } else if (select_type.equals("05")) {
                                    FscMeshSdk.getApi2().AT_GroupBindPanelBtn(devGroupBean.getDestination(), curtainGroupList.getJSONObject(curtain_name_nub).getString("address"), (Integer.parseInt(btn_id) + 1) + "", "1");

                                } else if (select_type.equals("1C")) {
                                    FscMeshSdk.getApi2().AT_GroupBindColorBtn(devGroupBean.getDestination(), (Integer.parseInt(btn_id) + 1) + "", "1");
                                }
                                refreshLayout.autoRefresh();
                            }
                        }
                    });
                } catch (Exception e) {

                }
                LoadingDialog.getInstance().Dismiss();
            }
        });

    }


    private void BindScene() {

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("mac", devGroupBean.getMac());
        formbody.add("family_id", devGroupBean.getFamily_id() + "");
        formbody.add("room_id", devGroupBean.getRoom_id() + "");
        formbody.add("second_type", devGroupBean.getSecond_type());

        for (int i = 0; i < panelBtnAdapter.getPanelBtnBeans().size(); i++) {
            if (panelBtnAdapter.getPanelBtnBeans().get(i).getSelect().equals("true")) {
                btn_id = i + "";
            }
        }

        formbody.add("btn_id", btn_id);
        formbody.add("bind_id", sceneList.getJSONObject(scene_name_nub).getString("scene_code"));
        formbody.add("bind_type", "1A");
        formbody.add("do_type", "1");
        formbody.add("device_type", devGroupBean.getDevice_type());

        RequestOkhttp.getInstance().PostBody(Api.updateLayout, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String str = response.body().string();
                Log.e("vvvv", "=======绑定============" + str);
                //{"code":1,"msg":"操作成功","time":1735787561,"data":null}
                try {
                    JSONObject jsonObject = JSON.parseObject(str);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (jsonObject.getIntValue("code") == 1) {
                                FscMeshSdk.getApi2().AT_SceneBindPanelBen(devGroupBean.getDestination(), Integer.parseInt(sceneList.getJSONObject(scene_name_nub).getString("scene_code")), (Integer.parseInt(btn_id) + 1) + "", "1");
                                refreshLayout.autoRefresh();
                            }
                        }
                    });
                } catch (Exception e) {

                }
                LoadingDialog.getInstance().Dismiss();
            }
        });

    }


    private void unBindGroup() {

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("mac", devGroupBean.getMac());
        formbody.add("family_id", devGroupBean.getFamily_id() + "");
        formbody.add("room_id", devGroupBean.getRoom_id() + "");
        formbody.add("second_type", devGroupBean.getSecond_type());

        for (int i = 0; i < panelBtnAdapter.getPanelBtnBeans().size(); i++) {
            if (panelBtnAdapter.getPanelBtnBeans().get(i).getSelect().equals("true")) {
                btn_id = i + "";
            }
        }
        formbody.add("btn_id", btn_id);
        formbody.add("bind_id", panelBtnAdapter.getPanelBtnBeans().get(Integer.parseInt(btn_id)).getBind_id());
        formbody.add("bind_type", panelBtnAdapter.getPanelBtnBeans().get(Integer.parseInt(btn_id)).getBind_type());
        formbody.add("do_type", "0");
        formbody.add("device_type", devGroupBean.getDevice_type());

        RequestOkhttp.getInstance().PostBody(Api.updateLayout, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String str = response.body().string();
                Log.e("vvvv", "=======解绑定============" + str);

                JSONObject jsonObject = JSON.parseObject(str);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            if (jsonObject.getIntValue("code") == 1) {
                                if (select_type.equals("01")) {
                                    FscMeshSdk.getApi2().AT_GroupBindPanelBtn(devGroupBean.getDestination(), panelBtnAdapter.getPanelBtnBeans().get(Integer.parseInt(btn_id)).getBind_id(), (Integer.parseInt(btn_id) + 1) + "", "0");

                                } else if (select_type.equals("05")) {
                                    FscMeshSdk.getApi2().AT_GroupBindPanelBtn(devGroupBean.getDestination(), panelBtnAdapter.getPanelBtnBeans().get(Integer.parseInt(btn_id)).getBind_id(), (Integer.parseInt(btn_id) + 1) + "", "0");

                                } else if (select_type.equals("1C")) {

                                    FscMeshSdk.getApi2().AT_GroupBindColorBtn(devGroupBean.getDestination(), (Integer.parseInt(btn_id) + 1) + "", "0");

                                }
                            }

                        } catch (Exception e) {

                        }
                        refreshLayout.autoRefresh();
                    }
                });

                LoadingDialog.getInstance().Dismiss();
            }
        });

    }


    private void unBindScene() {

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("mac", devGroupBean.getMac());
        formbody.add("family_id", devGroupBean.getFamily_id() + "");
        formbody.add("room_id", devGroupBean.getRoom_id() + "");
        formbody.add("second_type", devGroupBean.getSecond_type());

        for (int i = 0; i < panelBtnAdapter.getPanelBtnBeans().size(); i++) {
            if (panelBtnAdapter.getPanelBtnBeans().get(i).getSelect().equals("true")) {
                btn_id = i + "";
            }
        }

        formbody.add("btn_id", btn_id);
        formbody.add("bind_id", panelBtnAdapter.getPanelBtnBeans().get(Integer.parseInt(btn_id)).getBind_id());
        formbody.add("bind_type", "1A");
        formbody.add("do_type", "0");
        formbody.add("device_type", devGroupBean.getDevice_type());

        RequestOkhttp.getInstance().PostBody(Api.updateLayout, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String str = response.body().string();
                Log.e("vvvv", "=======绑定============" + str);
                //{"code":1,"msg":"操作成功","time":1735787561,"data":null}

                JSONObject jsonObject = JSON.parseObject(str);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            if (jsonObject.getIntValue("code") == 1) {
                                FscMeshSdk.getApi2().AT_SceneBindPanelBen(devGroupBean.getDestination(), Integer.parseInt(sceneList.getJSONObject(scene_name_nub).getString("scene_code")), (Integer.parseInt(btn_id) + 1) + "", "0");

                            }
                        } catch (Exception e) {

                        }

                        refreshLayout.autoRefresh();

                    }
                });


                LoadingDialog.getInstance().Dismiss();
            }
        });

    }


    private String select_type = "-1";


    ItemClickCallback itemClickCallback = new ItemClickCallback() {
        @Override
        public void onItemClick(int p, String str) {

            for (int i = 0; i < panelBtnAdapter.getPanelBtnBeans().size(); i++) {
                panelBtnAdapter.getPanelBtnBeans().get(i).setSelect("false");
            }
            panelBtnAdapter.getPanelBtnBeans().get(p).setSelect("true");
            panelBtnAdapter.notifyDataSetChanged();

            if (TextUtils.isEmpty(panelBtnAdapter.getPanelBtnBeans().get(p).getBind_id())) {

                img_unbind.setBackgroundResource(R.mipmap.panel_btn_icon_unbind_inactive);
                txt_unbind.setTextColor(getResources().getColor(R.color.txt_none));
                img_bind.setBackgroundResource(R.mipmap.panel_btn_icon_bind_active);
                txt_bind.setTextColor(getResources().getColor(R.color.txt_blue));
                line_bind.setEnabled(true);
                line_unbind.setEnabled(false);

            } else {

                img_unbind.setBackgroundResource(R.mipmap.panel_btn_icon_unbind_active);
                txt_unbind.setTextColor(getResources().getColor(R.color.txt_blue));
                img_bind.setBackgroundResource(R.mipmap.panel_btn_icon_bind_inactive);
                txt_bind.setTextColor(getResources().getColor(R.color.txt_none));
                line_bind.setEnabled(false);
                line_unbind.setEnabled(true);

            }


            if (panelBtnAdapter.getPanelBtnBeans().get(p).getBind_type().equals("01")) {
                select_type = "01";
                img_dev1.setBackgroundResource(R.mipmap.primary_icon_selected);
                img_dev2.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev3.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev4.setBackgroundResource(R.mipmap.primary_icon_unselected);
                if (null != lightGroupList) {

                    for (int i = 0; i < lightGroupList.size(); i++) {
                        if (panelBtnAdapter.getPanelBtnBeans().get(p).getTitle().equals(lightGroupList.getJSONObject(i).getString("group_name"))) {
                            txt_select_light_group.setText(lightGroupList.getJSONObject(i).getJSONObject("room").getString("room_name") + "  " + panelBtnAdapter.getPanelBtnBeans().get(p).getTitle());
                            light_name_nub = i;
                        }
                    }

                }

            } else if (panelBtnAdapter.getPanelBtnBeans().get(p).getBind_type().equals("05")) {
                select_type = "05";
                img_dev1.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev2.setBackgroundResource(R.mipmap.primary_icon_selected);
                img_dev3.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev4.setBackgroundResource(R.mipmap.primary_icon_unselected);
                if (null != curtainGroupList) {

                    for (int i = 0; i < curtainGroupList.size(); i++) {
                        if (panelBtnAdapter.getPanelBtnBeans().get(p).getTitle().equals(curtainGroupList.getJSONObject(i).getString("group_name"))) {
                            txt_select_curtain_group.setText(curtainGroupList.getJSONObject(i).getJSONObject("room").getString("room_name") + "  " + panelBtnAdapter.getPanelBtnBeans().get(p).getTitle());
                            curtain_name_nub = i;
                        }
                    }

                }

            } else if (panelBtnAdapter.getPanelBtnBeans().get(p).getBind_type().equals("1A")) {
                select_type = "1A";

                img_dev1.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev2.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev3.setBackgroundResource(R.mipmap.primary_icon_selected);
                img_dev4.setBackgroundResource(R.mipmap.primary_icon_unselected);
                if (null != sceneList) {

                    for (int i = 0; i < sceneList.size(); i++) {
                        if (panelBtnAdapter.getPanelBtnBeans().get(p).getTitle().equals(sceneList.getJSONObject(i).getString("scene_name"))) {
                            txt_select_scene.setText(panelBtnAdapter.getPanelBtnBeans().get(p).getTitle());
                            scene_name_nub = i;
                        }
                    }

                }

            } else if (panelBtnAdapter.getPanelBtnBeans().get(p).getBind_type().equals("1C")) {
                select_type = "1C";
                img_dev1.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev2.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev3.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev4.setBackgroundResource(R.mipmap.primary_icon_selected);

            } else {
                select_type = "-1";
                img_dev1.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev2.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev3.setBackgroundResource(R.mipmap.primary_icon_unselected);
                img_dev4.setBackgroundResource(R.mipmap.primary_icon_unselected);
            }


        }
    };


    private void getPaneBtnList() {

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("mac", devGroupBean.getMac());

        RequestOkhttp.getInstance().PostBody(Api.getPanelinfo, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                refreshLayout.finishRefresh();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                panelBtnBeans.clear();
                refreshLayout.finishRefresh();
                String str_res = response.body().string();
                Log.e("vvv", "=======getPaneBtnList=====" + str_res);
                try {
                    JSONObject jsonObject = JSON.parseObject(str_res);
                    if (jsonObject.getIntValue("code") == 1) {
                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                        JSONArray jsonArray = jsonObject1.getJSONArray("layoutList");
                        if (null != jsonArray) {
                            for (int i = 0; i < jsonArray.size(); i++) {
                                PanelBtnBean panelBtnBean = JSON.parseObject(jsonArray.getString(i), PanelBtnBean.class);
                                panelBtnBeans.add(panelBtnBean);
                            }
                        }


                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                txt_locaName.setText(jsonObject.getJSONObject("data").getJSONObject("panel").getString("device_name"));
                                devGroupBean.setDevice_name(txt_locaName.getText().toString());
                                panelBtnAdapter.setPanelBtnBeans(panelBtnBeans);
                                if (panelBtnBeans.size() > 0) {
                                    txt_btn_set.setVisibility(View.VISIBLE);
                                } else {
                                    txt_btn_set.setVisibility(View.GONE);
                                }
                            }
                        });

                    }


                } catch (Exception e) {

                }

            }
        });

    }


    private JSONArray lightGroupList;
    private JSONArray curtainGroupList;
    private JSONArray sceneList;

    private void getPanelGrouplist() {

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", devGroupBean.getFamily_id() + "");
        formbody.add("room_id", devGroupBean.getRoom_id() + "");
        RequestOkhttp.getInstance().PostBody(Api.panelGroupList, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = response.body().string();
                Log.e("vvv", "====获取可绑定组======" + str);
                try {
                    JSONObject jsonObject = JSON.parseObject(str);
                    if (jsonObject.getIntValue("code") == 1) {

                        JSONObject json_data = jsonObject.getJSONObject("data");
                        lightGroupList = json_data.getJSONArray("groupList");
                        curtainGroupList = json_data.getJSONArray("curtainGroupList");
                        sceneList = json_data.getJSONArray("sceneList");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (light_name_nub < lightGroupList.size()) {
                                    txt_select_light_group.setText(lightGroupList.getJSONObject(light_name_nub).getJSONObject("room").getString("room_name") + "  " + lightGroupList.getJSONObject(light_name_nub).getString("group_name"));
                                }
                                if (curtain_name_nub < curtainGroupList.size()) {
                                    txt_select_curtain_group.setText(curtainGroupList.getJSONObject(curtain_name_nub).getJSONObject("room").getString("room_name") + "  " + curtainGroupList.getJSONObject(curtain_name_nub).getString("group_name"));
                                }
                                if (scene_name_nub < sceneList.size()) {
                                    txt_select_scene.setText(sceneList.getJSONObject(scene_name_nub).getString("scene_name"));
                                }
                            }
                        });

                    }
                } catch (Exception e) {

                }

            }
        });


    }


    private PopupWindow pop_bind_light_group = null;
    private int light_name_nub = 0;


    @SuppressLint("NewApi")
    private void ShowBindLightGroupPOP(View parent) {


        View pop_view = LayoutInflater.from(GroupPanelDetailsActivity.this).inflate(R.layout.pop_bind_group, null);
        RelativeLayout rela_pop_back = pop_view.findViewById(R.id.rela_pop_back);
        NumberPicker numberpicker1 = pop_view.findViewById(R.id.numberpicker1);

        String[] gn = new String[lightGroupList.size()];

        for (int i = 0; i < lightGroupList.size(); i++) {
            gn[i] = lightGroupList.getJSONObject(i).getJSONObject("room").getString("room_name") + "  " + lightGroupList.getJSONObject(i).getString("group_name");
        }

        numberpicker1.setDisplayedValues(gn);
        numberpicker1.setMinValue(0);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            numberpicker1.setTextColor(getResources().getColor(R.color.black));
            numberpicker1.setSelectionDividerHeight(1);
        }
        numberpicker1.setMaxValue(lightGroupList.size() - 1);

        numberpicker1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // 关闭编辑模式
        numberpicker1.setWrapSelectorWheel(false);
        numberpicker1.setValue(light_name_nub);
        numberpicker1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                light_name_nub = i1;
            }
        });


        rela_pop_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (null != pop_bind_light_group && pop_bind_light_group.isShowing()) {
                    pop_bind_light_group.dismiss();
                }

            }
        });
        TextView txt_pop_create = pop_view.findViewById(R.id.txt_pop_create);
        txt_pop_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop_bind_light_group.dismiss();
                txt_select_light_group.setText(gn[light_name_nub]);

            }
        });


        pop_bind_light_group = new PopupWindow(pop_view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pop_bind_light_group.setElevation(3);
        pop_bind_light_group.setAnimationStyle(R.style.PopupAnimation);
        pop_bind_light_group.setOutsideTouchable(false);
        pop_bind_light_group.showAtLocation(parent, Gravity.BOTTOM, 0, 0);

    }


    private PopupWindow pop_bind_curtain_group = null;
    private int curtain_name_nub = 0;


    @SuppressLint("NewApi")
    private void ShowBindCurtainGroupPOP(View parent) {


        View pop_view = LayoutInflater.from(GroupPanelDetailsActivity.this).inflate(R.layout.pop_bind_group, null);
        RelativeLayout rela_pop_back = pop_view.findViewById(R.id.rela_pop_back);
        NumberPicker numberpicker1 = pop_view.findViewById(R.id.numberpicker1);

        String[] gn = new String[curtainGroupList.size()];

        for (int i = 0; i < curtainGroupList.size(); i++) {
            gn[i] = curtainGroupList.getJSONObject(i).getJSONObject("room").getString("room_name") + "  " + curtainGroupList.getJSONObject(i).getString("group_name");
        }

        numberpicker1.setDisplayedValues(gn);
        numberpicker1.setMinValue(0);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            numberpicker1.setTextColor(getResources().getColor(R.color.black));
            numberpicker1.setSelectionDividerHeight(1);
        }
        numberpicker1.setMaxValue(curtainGroupList.size() - 1);

        numberpicker1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // 关闭编辑模式
        numberpicker1.setWrapSelectorWheel(false);
        numberpicker1.setValue(curtain_name_nub);
        numberpicker1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                curtain_name_nub = i1;
            }
        });


        rela_pop_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop_bind_curtain_group.dismiss();
            }
        });
        TextView txt_pop_create = pop_view.findViewById(R.id.txt_pop_create);
        txt_pop_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop_bind_curtain_group.dismiss();
                txt_select_curtain_group.setText(gn[curtain_name_nub]);

            }
        });


        pop_bind_curtain_group = new PopupWindow(pop_view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pop_bind_curtain_group.setElevation(3);
        pop_bind_curtain_group.setAnimationStyle(R.style.PopupAnimation);
        pop_bind_curtain_group.setOutsideTouchable(false);
        pop_bind_curtain_group.showAtLocation(parent, Gravity.BOTTOM, 0, 0);

    }

    private PopupWindow pop_bind_scene = null;
    private int scene_name_nub = 0;

    @SuppressLint("NewApi")
    private void ShowBindScenePOP(View parent) {


        View pop_view = LayoutInflater.from(GroupPanelDetailsActivity.this).inflate(R.layout.pop_bind_scene, null);
        RelativeLayout rela_pop_back = pop_view.findViewById(R.id.rela_pop_back);
        NumberPicker numberpicker1 = pop_view.findViewById(R.id.numberpicker1);

        String[] gn = new String[sceneList.size()];

        for (int i = 0; i < sceneList.size(); i++) {
            gn[i] = sceneList.getJSONObject(i).getString("scene_name");
        }

        numberpicker1.setDisplayedValues(gn);
        numberpicker1.setMinValue(0);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            numberpicker1.setTextColor(getResources().getColor(R.color.black));
            numberpicker1.setSelectionDividerHeight(1);
        }
        numberpicker1.setMaxValue(sceneList.size() - 1);

        numberpicker1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // 关闭编辑模式
        numberpicker1.setWrapSelectorWheel(false);
        numberpicker1.setValue(scene_name_nub);
        numberpicker1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                scene_name_nub = i1;
            }
        });


        rela_pop_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop_bind_scene.dismiss();
            }
        });
        TextView txt_pop_create = pop_view.findViewById(R.id.txt_pop_create);
        txt_pop_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop_bind_scene.dismiss();
                txt_select_scene.setText(gn[scene_name_nub]);
            }
        });


        pop_bind_scene = new PopupWindow(pop_view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pop_bind_scene.setElevation(3);
        pop_bind_scene.setAnimationStyle(R.style.PopupAnimation);
        pop_bind_scene.setOutsideTouchable(false);
        pop_bind_scene.showAtLocation(parent, Gravity.BOTTOM, 0, 0);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (null != pop_bind_light_group && pop_bind_light_group.isShowing()) {
            pop_bind_light_group.dismiss();

        } else if (null != pop_bind_curtain_group && pop_bind_curtain_group.isShowing()) {
            pop_bind_curtain_group.dismiss();

        } else if (null != pop_bind_scene && pop_bind_scene.isShowing()) {
            pop_bind_scene.dismiss();

        } else {
            finish();

        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    Handler handler = new Handler() {

    };


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
