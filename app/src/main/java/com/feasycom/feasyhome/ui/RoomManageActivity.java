package com.feasycom.feasyhome.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.adapter.RoomListAdapter;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.bean.FamilyBean;
import com.feasycom.feasyhome.bean.RoomBean;
import com.feasycom.feasyhome.callback.ItemClickCallback;
import com.feasycom.feasyhome.logic.FamilyModelFactory;
import com.feasycom.feasyhome.ui.dialog.DelDialog;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.feasycom.feasyhome.ui.dialog.SingleInputDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class RoomManageActivity extends BaseActivity implements ItemClickCallback {


    private RelativeLayout rela_back;
    private View view_status;
    private TextView txt_create_room;
    private RecyclerView recyclerview;
    private RoomListAdapter roomListAdapter;
    private FamilyBean familyBean;
    private String str_room_list;
    private ArrayList<RoomBean> roomBeans = new ArrayList<>();
    private int selectRoom = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BarUtils.transparentStatusBar(RoomManageActivity.this);
        BarUtils.setStatusBarLightMode(RoomManageActivity.this, true);

        setContentView(R.layout.roommanageactivity);

        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        str_room_list = getIntent().getStringExtra("str_room_list");
        familyBean = (FamilyBean) getIntent().getSerializableExtra("familyBean");
        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);

        txt_create_room = findViewById(R.id.txt_create_room);
        txt_create_room.setOnClickListener(listener);

        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(RoomManageActivity.this));
        roomListAdapter = new RoomListAdapter(RoomManageActivity.this, RoomManageActivity.this);
        recyclerview.setAdapter(roomListAdapter);
        JSONArray jsonArray = JSON.parseArray(str_room_list);
        if (null != jsonArray) {
            for (int i = 0; i < jsonArray.size(); i++) {
                RoomBean roomBean = JSON.parseObject(jsonArray.getString(i), RoomBean.class);
                roomBeans.add(roomBean);
            }

            roomListAdapter.setRoomBeans(roomBeans);

        }


    }


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view == rela_back) {
                finish();
            } else if (view == txt_create_room) {

                SingleInputDialog singleInputDialog = new SingleInputDialog(RoomManageActivity.this);
                singleInputDialog.show();
                singleInputDialog.txt_title.setText(R.string.createaroom);
                singleInputDialog.edit_name.setHint(R.string.tips29);
                singleInputDialog.txt_cancler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        singleInputDialog.dismiss();
                    }
                });
                singleInputDialog.txt_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        singleInputDialog.dismiss();
                        if (TextUtils.isEmpty(singleInputDialog.edit_name.getText().toString().trim())) {
                            ToastUtils.showShort(R.string.tips29);
                        } else {
                            CreateRoom(singleInputDialog.edit_name.getText().toString().trim());

                        }

                    }
                });


            /*    Intent intent = new Intent(RoomManageActivity.this, CreateRoomActivity.class);
                startActivity(intent);*/

            }

        }
    };


    @Override
    public void onItemClick(int p, String str) {
        selectRoom = p;
        if (str.equals("修改名称")) {

            SingleInputDialog singleInputDialog = new SingleInputDialog(RoomManageActivity.this);
            singleInputDialog.show();
            singleInputDialog.txt_title.setText(R.string.tips30);
            singleInputDialog.edit_name.setHint(R.string.tips29);
            singleInputDialog.txt_cancler.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    singleInputDialog.dismiss();
                }
            });
            singleInputDialog.txt_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    singleInputDialog.dismiss();
                    if (TextUtils.isEmpty(singleInputDialog.edit_name.getText().toString().trim())) {
                        ToastUtils.showShort(R.string.tips29);

                    } else if (singleInputDialog.edit_name.getText().toString().trim().length() < 2) {
                        ToastUtils.showShort(R.string.tips31);
                    } else {
                        ModifyRoomName(roomBeans.get(p), singleInputDialog.edit_name.getText().toString().trim());

                    }

                }
            });


        } else if (str.equals("删除房间")) {

            DelDialog delDialog = new DelDialog(RoomManageActivity.this);
            delDialog.show();
            delDialog.txt_title.setText(getString(R.string.tips32) + roomBeans.get(p).getRoom_name());
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
                    DeleteRoom();
                }
            });


        }

    }


    private void CreateRoom(String str_name) {

        LoadingDialog.getInstance().Show(RoomManageActivity.this, getString(R.string.requesting));
        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", familyBean.getId() + "");
        formbody.add("roomName", str_name);
        RequestOkhttp.getInstance().PostBody(Api.CreateRoom, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String str_res = response.body().string();
                Log.e("vvvv", "=======创建房间=====" + str_res);
                try {
                    JSONObject jsonObject = JSON.parseObject(str_res);
                    //  {"code":1,"msg":"操作成功","time":1724394808,"data":null}
                    if (jsonObject.getIntValue("code") == 1) {

                    }
                    GetRoomList();
                } catch (Exception e) {
                    LoadingDialog.getInstance().Dismiss();
                    ToastUtils.showShort("创建房间-网络异常");
                }

            }
        });


    }

    private int roomsize = 0;

    private void GetRoomList() {
        roomsize = roomBeans.size();
        FormBody.Builder formbody = new FormBody.Builder();

        formbody.add("family_id", familyBean.getId() + "");
        RequestOkhttp.getInstance().PostBody(Api.RoomList, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                LoadingDialog.getInstance().Dismiss();
                String str_res = response.body().string();
                Log.e("vvvv", "======房间列表=======" + str_res);
                try {

                    JSONObject jsonObject = JSON.parseObject(str_res);
                    if (jsonObject.getIntValue("code") == 1) {
                        roomBeans.clear();
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        str_room_list = jsonArray.toString();
                        for (int i = 0; i < jsonArray.size(); i++) {
                            RoomBean roomBean = JSON.parseObject(jsonArray.getString(i), RoomBean.class);
                            roomBeans.add(roomBean);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                roomListAdapter.setRoomBeans(roomBeans);
                                if (roomsize != 0 && roomsize < roomBeans.size()) {
                                    if (null != FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean() && familyBean.getId() == FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue().getId()) {
                                        FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().setValue(roomBeans);
                                    }
                                }
                            }
                        });

                    }

                } catch (Exception e) {

                }
            }
        });


    }


    private void DeleteRoom() {

        LoadingDialog.getInstance().Show(RoomManageActivity.this, getString(R.string.requesting));
        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", familyBean.getId() + "");
        formbody.add("room_id", roomBeans.get(selectRoom).getId() + "");

        RequestOkhttp.getInstance().PostBody(Api.DeleteRoom, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                LoadingDialog.getInstance().Dismiss();
                String str_res = response.body().string();
                Log.e("vvvvv", "========删除房间=======" + str_res);
                //  {"code":1,"msg":"删除成功","time":1724384195,"data":null}

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            JSONObject jsonObject = JSON.parseObject(str_res);
                            if (jsonObject.getIntValue("code") == 1) {
                                roomBeans.remove(selectRoom);
                                roomListAdapter.setRoomBeans(roomBeans);
                                ToastUtils.showShort(jsonObject.getString("msg"));
                                if (familyBean.getId() == FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue().getId()) {
                                    FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().setValue(roomBeans);
                                }

                            }

                        } catch (Exception e) {

                        }

                    }
                });


            }
        });


    }


    private void ModifyRoomName(RoomBean roomBean, String str_name) {
        LoadingDialog.getInstance().Show(RoomManageActivity.this, getString(R.string.requesting));

        FormBody.Builder formbody = new FormBody.Builder();

        formbody.add("room_id", roomBean.getId() + "");
        formbody.add("room_name", str_name);
        RequestOkhttp.getInstance().PostBody(Api.ModifyRoomName, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
                ToastUtils.showShort("修改失败1");

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                LoadingDialog.getInstance().Dismiss();
                String str_res = response.body().string();
                Log.e("vvvvv", "========修改房间name=======" + str_res);
                //{"code":1,"msg":"修改成功","time":1724319471,"data":null}
                try {

                    JSONObject jsonObject = JSON.parseObject(str_res);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showShort(jsonObject.getString("msg"));

                            if (jsonObject.getIntValue("code") == 1) {
                                roomBeans.get(selectRoom).setRoom_name(str_name);
                                roomListAdapter.setRoomBeans(roomBeans);
                                if (null != FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean() && familyBean.getId() == FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue().getId()) {
                                    FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().setValue(roomBeans);
                                }
                            } else {

                            }

                        }
                    });


                } catch (Exception e) {

                }

            }
        });


    }


}
