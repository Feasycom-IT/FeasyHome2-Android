package com.feasycom.feasyhome.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.adapter.SceneGroupsAdapter;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.bean.DevGroupBean;
import com.feasycom.feasyhome.bean.RoomBean;
import com.feasycom.feasyhome.bean.SceneBean;
import com.feasycom.feasyhome.bean.SceneGroupBean;
import com.feasycom.feasyhome.callback.ItemClickCallback;
import com.feasycom.feasyhome.logic.FamilyModelFactory;
import com.feasycom.feasyhome.ui.dialog.DelDialog;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import no.nordicsemi.android.mesh.fsdk.FscMeshSdk;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class EditSceneActivity extends BaseActivity {


    private View view_status;
    private RelativeLayout rela_back;
    private SceneBean select_sceneBean;
    private TextView txt_scene_name, txt_group_nub;
    private ImageView img_scene;
    private SmartRefreshLayout refreshLayout;
    private LinearLayout line_bg, line_add_group;
    private RecyclerView recyclerview;
    private SceneGroupsAdapter sceneGroupsAdapter;
    private HashMap<Integer, String> hashMap_room = new HashMap<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(EditSceneActivity.this);
        BarUtils.setStatusBarLightMode(EditSceneActivity.this, true);
        setContentView(R.layout.editsceneactivity);

        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        select_sceneBean = (SceneBean) getIntent().getSerializableExtra("select_sceneBean");
        line_bg = findViewById(R.id.line_bg);

        line_add_group = findViewById(R.id.line_add_group);
        line_add_group.setOnClickListener(listener);
        txt_group_nub = findViewById(R.id.txt_group_nub);
        txt_scene_name = findViewById(R.id.txt_scene_name);
        txt_scene_name.setText(select_sceneBean.getScene_name());
        img_scene = findViewById(R.id.img_scene);
        Glide.with(EditSceneActivity.this).load(Api.FeasyHomeServer + select_sceneBean.getScene_image()).into(img_scene);

        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);
        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(EditSceneActivity.this));
        sceneGroupsAdapter = new SceneGroupsAdapter(EditSceneActivity.this, itemClickCallback);
        recyclerview.setAdapter(sceneGroupsAdapter);

        refreshLayout = findViewById(R.id.refreshLayout);


        if (null != FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().getValue()) {
            room_names = new String[FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().getValue().size()];
            for (int i = 0; i < FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().getValue().size(); i++) {
                room_names[i] = FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().getValue().get(i).getRoom_name();
                hashMap_room.put(FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().getValue().get(i).getId(), FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().getValue().get(i).getRoom_name());
            }
        }


        sceneGroupsAdapter.setHashMap_room(hashMap_room);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                getSceneDetail();
            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        refreshLayout.autoRefresh();
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void onClick(View view) {

            if (view == rela_back) {
                if (null != popaddgroup && popaddgroup.isShowing()) {
                    popaddgroup.dismiss();
                    return;
                }
                finish();
            } else if (view == line_add_group) {
                if (null == FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().getValue() || FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().getValue().size() <= 0) {
                    ToastUtils.showLong("没有可添加的设备组");
                } else {
                    ShowGroupsPop();
                }
            }
        }
    };


    ItemClickCallback itemClickCallback = new ItemClickCallback() {
        @Override
        public void onItemClick(int p, String str) {

            if (str.equals("del")) {
                DelDialog delDialog = new DelDialog(EditSceneActivity.this);
                delDialog.show();
                delDialog.txt_title.setText("确认删除该组！");
                delDialog.txt_del.setText("删除");
                delDialog.txt_cancler.setText("取消");
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
                        DelSceneGroup(p);
                    }
                });

            } else if (str.equals("edit")) {

                Intent intent;
                if (existDevGroups.get(p).getDevice_type().equals("01")) {
                    intent = new Intent(EditSceneActivity.this, ConfigSceneLightActivity.class);
                } else {
                    intent = new Intent(EditSceneActivity.this, ConfigSceneCurtainActivity.class);
                }

                intent.putExtra("sceneBean", select_sceneBean);
                intent.putExtra("room_name", hashMap_room.get(existDevGroups.get(p).getRoom_id()));
                intent.putExtra("room_id", existDevGroups.get(p).getRoom_id() + "");
                intent.putExtra("group_name", existDevGroups.get(p).getGroup_name());
                intent.putExtra("group_id", existDevGroups.get(p).getId() + "");
                intent.putExtra("group_address", existDevGroups.get(p).getGroup_address());
                intent.putExtra("device_type", existDevGroups.get(p).getDevice_type());
                startActivity(intent);

            }
        }
    };


    private void DelSceneGroup(int p) {
        LoadingDialog.getInstance().Show(EditSceneActivity.this, getString(R.string.requesting));
        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("scene_code", select_sceneBean.getScene_code());
        formbody.add("family_id", select_sceneBean.getFamily_id() + "");
        formbody.add("address", existDevGroups.get(p).getGroup_address());
        RequestOkhttp.getInstance().PostBody(Api.deleteSceneGroup, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                LoadingDialog.getInstance().Dismiss();
                String str = response.body().string();
                Log.e("vvvv", "======删除场景中的组==========" + str);
                //{"code":1,"msg":"删除成功","time":1739341201,"data":null}
                try {

                    JSONObject jsonObject = JSON.parseObject(str);
                    if (jsonObject.getIntValue("code") == 1) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                FscMeshSdk.getApi2().SceneUnbindGroup(Integer.parseInt(select_sceneBean.getScene_code()), existDevGroups.get(p).getGroup_address());
                                ToastUtils.showShort(jsonObject.getString("msg"));
                                refreshLayout.autoRefresh();
                            }
                        });

                    }

                } catch (Exception e) {

                }

            }
        });
    }


    private PopupWindow popaddgroup = null;
    private String[] room_names;
    private String[] group_names;
    private int room_name_p = 0;
    private int group_name_p = 0;

    private NumberPicker numberpicker1, numberpicker2;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void ShowGroupsPop() {

        View pop_view = LayoutInflater.from(EditSceneActivity.this).inflate(R.layout.pop_add_grouptoscene, null);
        RelativeLayout rela_pop_back = pop_view.findViewById(R.id.rela_pop_back);
        numberpicker1 = pop_view.findViewById(R.id.numberpicker1);
        numberpicker2 = pop_view.findViewById(R.id.numberpicker2);

        rela_pop_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popaddgroup.dismiss();
            }
        });
        TextView txt_pop_create = pop_view.findViewById(R.id.txt_pop_create);
        txt_pop_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (tempGroups.size() <= 0) {
                    ToastUtils.showShort("未选中设备组！");
                } else if (!FscMeshSdk.getApi2().isConnect()) {
                    ToastUtils.showShort("请先连接网络！");
                } else {
                    Intent intent;
                    if (tempGroups.get(group_name_p).getDevice_type().equals("01")) {
                        intent = new Intent(EditSceneActivity.this, ConfigSceneLightActivity.class);
                    } else {
                        intent = new Intent(EditSceneActivity.this, ConfigSceneCurtainActivity.class);
                    }
                    intent.putExtra("sceneBean", select_sceneBean);
                    intent.putExtra("room_name", room_names[room_name_p]);
                    intent.putExtra("room_id", tempGroups.get(group_name_p).getRoom_id() + "");
                    intent.putExtra("group_name", tempGroups.get(group_name_p).getGroup_name());
                    intent.putExtra("group_id", tempGroups.get(group_name_p).getId() + "");
                    intent.putExtra("group_address", tempGroups.get(group_name_p).getAddress());
                    intent.putExtra("device_type", tempGroups.get(group_name_p).getDevice_type());
                    startActivity(intent);
                    popaddgroup.dismiss();
                }

            }
        });


        popaddgroup = new PopupWindow(pop_view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popaddgroup.setElevation(3);
        popaddgroup.setAnimationStyle(R.style.PopupAnimation);
        popaddgroup.setOutsideTouchable(true);
        popaddgroup.showAtLocation(line_bg, Gravity.BOTTOM, 0, 0);


        handler.post(new Runnable() {
            @Override
            public void run() {
                numberpicker1.setDisplayedValues(room_names);
                numberpicker1.setMinValue(0);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    // 在这里执行针对Android 10（API级别29）及更高版本的代码
                    numberpicker1.setTextColor(getResources().getColor(R.color.black));
                    numberpicker1.setSelectionDividerHeight(1);
                }
                numberpicker1.setMaxValue(room_names.length - 1);

                numberpicker1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // 关闭编辑模式
                numberpicker1.setWrapSelectorWheel(false);
                numberpicker1.setValue(room_name_p);

                numberpicker1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                        room_name_p = i1;
                        SelectGroup();
                    }
                });

                SelectGroup();

            }
        });


    }

    private ArrayList<DevGroupBean> tempGroups = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void SelectGroup() {

        LoadingDialog.getInstance().Show(EditSceneActivity.this, getString(R.string.requesting));
        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue().getId() + "");
        formbody.add("room_id", FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().getValue().get(room_name_p).getId() + "");

        RequestOkhttp.getInstance().PostBody(Api.scenegrouplist, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                LoadingDialog.getInstance().Dismiss();

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String str = response.body().string();
                Log.e("vvvv", "========场景可添加组列表========" + str);
                try {

                    JSONObject jsonObject = JSON.parseObject(str);
                    if (jsonObject.getIntValue("code") == 1) {
                        JSONObject json_data = jsonObject.getJSONObject("data");
                        if (null != json_data) {

                            tempGroups.clear();

                            JSONArray groupList = json_data.getJSONArray("groupList");
                            JSONArray curtainGroupList = json_data.getJSONArray("curtainGroupList");

                            if (null != groupList) {
                                for (int i = 0; i < groupList.size(); i++) {
                                    DevGroupBean devGroupBean = JSON.parseObject(groupList.getString(i), DevGroupBean.class);
                                    boolean exists = existDevGroups.stream()
                                            .anyMatch(bean -> bean.getGroup_address().equals(devGroupBean.getAddress()));
                                    if (!exists) {
                                        tempGroups.add(devGroupBean);
                                    }

                                }
                            }

                            if (null != curtainGroupList) {
                                for (int i = 0; i < curtainGroupList.size(); i++) {
                                    DevGroupBean devGroupBean = JSON.parseObject(curtainGroupList.getString(i), DevGroupBean.class);
                                    boolean exists = existDevGroups.stream()
                                            .anyMatch(bean -> bean.getGroup_address().equals(devGroupBean.getAddress()));
                                    if (!exists) {
                                        tempGroups.add(devGroupBean);
                                    }
                                }

                            }


                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    numberpicker2.setMinValue(0);
                                    numberpicker2.setMaxValue(0); // 设置 min=max=0，使其无法选择任何值
                                    numberpicker2.setValue(0);

                                    if (tempGroups.size() > 0) {
                                        group_name_p = 0;
                                        group_names = new String[tempGroups.size()];
                                        for (int i = 0; i < tempGroups.size(); i++) {
                                            group_names[i] = tempGroups.get(i).getGroup_name().trim();
                                        }

                                        numberpicker2.setVisibility(View.VISIBLE);
                                        numberpicker2.setDisplayedValues(group_names);
                                        numberpicker2.setMinValue(0);
                                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                                            numberpicker2.setTextColor(getResources().getColor(R.color.black));
                                            numberpicker2.setSelectionDividerHeight(1);
                                        }
                                        numberpicker2.setMaxValue(group_names.length - 1);

                                        numberpicker2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // 关闭编辑模式
                                        numberpicker2.setWrapSelectorWheel(false);
                                        numberpicker2.setDisplayedValues(group_names);

                                        numberpicker2.setValue(group_name_p);


                                        numberpicker2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                                            @Override
                                            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                                                group_name_p = i1;
                                            }
                                        });


                                    } else {
                                        group_names = null;
                                        group_name_p = 0;
                                        numberpicker2.setVisibility(View.INVISIBLE);
                                    }

                                }
                            });


                        }
                    }

                } catch (Exception e) {

                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LoadingDialog.getInstance().Dismiss();
                    }
                }, 500);

            }
        });


    }


    private ArrayList<SceneGroupBean> existDevGroups = new ArrayList<>();

    private void getSceneDetail() {

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", select_sceneBean.getFamily_id() + "");
        formbody.add("scene_code", select_sceneBean.getScene_code());

        RequestOkhttp.getInstance().PostBody(Api.sceneDetail, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                refreshLayout.finishRefresh();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = response.body().string();
                Log.e("vvv", "======获取场景中的组=======" + str);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            refreshLayout.finishRefresh();
                            JSONObject jsonObject = JSON.parseObject(str);
                            if (jsonObject.getIntValue("code") == 1) {
                                existDevGroups.clear();
                                JSONObject json_data = jsonObject.getJSONObject("data");
                                JSONArray groupList = json_data.getJSONArray("groupList");
                                JSONArray curtainGroupList = json_data.getJSONArray("curtainGroupList");

                                if (null != groupList) {
                                    for (int i = 0; i < groupList.size(); i++) {
                                        SceneGroupBean sceneGroupBean = JSON.parseObject(groupList.getString(i), SceneGroupBean.class);
                                        existDevGroups.add(sceneGroupBean);
                                    }
                                }
                                if (null != curtainGroupList) {
                                    for (int i = 0; i < curtainGroupList.size(); i++) {
                                        SceneGroupBean sceneGroupBean = JSON.parseObject(curtainGroupList.getString(i), SceneGroupBean.class);
                                        existDevGroups.add(sceneGroupBean);
                                    }
                                }
                                sceneGroupsAdapter.setDevGroupBeans(existDevGroups);
                                txt_group_nub.setText("已添加组 (" + existDevGroups.size() + ")");

                            }

                        } catch (Exception e) {

                        }

                    }
                });

            }
        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (null != popaddgroup && popaddgroup.isShowing()) {
            popaddgroup.dismiss();
        }
    }

    Handler handler = new Handler() {


    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
