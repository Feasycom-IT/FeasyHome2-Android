package com.feasycom.feasyhome.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
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
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.adapter.SelectLightGroupAdapter;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.bean.DevTypeBean;
import com.feasycom.feasyhome.bean.GroupNameBean;
import com.feasycom.feasyhome.bean.DevGroupBean;
import com.feasycom.feasyhome.bean.RoomBean;
import com.feasycom.feasyhome.callback.ItemSelectCallBack;
import com.feasycom.feasyhome.logic.FamilyModelFactory;
import com.feasycom.feasyhome.ui.dialog.DelDialog;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.feasycom.feasyhome.ui.dialog.TipsDialogOne;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import no.nordicsemi.android.mesh.MeshManagerApi;
import no.nordicsemi.android.mesh.fsdk.ExtendedBluetoothDevice;
import no.nordicsemi.android.mesh.fsdk.FscMeshSdk;
import no.nordicsemi.android.mesh.fsdk.FscScancallback;
import no.nordicsemi.android.mesh.fsdk.ProvisioningCallback;
import no.nordicsemi.android.mesh.fsdk.SubscribeGroupCallback;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class SearchUnprovisionedCurtain extends BaseActivity implements ItemSelectCallBack {


    private RelativeLayout rela_back, rela_bg;
    private View view_status;
    private LinearLayout line_search, line_join;
    private TextView txt_create_group, txt_dev_tips, txt_research;
    private DevTypeBean devTypeBean;
    private ImageView img_dev_logo;


    private RoomBean roombean;
    private RecyclerView recyclerview;
    private SelectLightGroupAdapter selectLightGroupAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(SearchUnprovisionedCurtain.this);
        BarUtils.setStatusBarLightMode(SearchUnprovisionedCurtain.this, true);
        setContentView(R.layout.searchunprovisionedcurtain);

        devTypeBean = (DevTypeBean) getIntent().getSerializableExtra("devTypeBean");
        roombean = (RoomBean) getIntent().getSerializableExtra("roombean");
        rela_bg = findViewById(R.id.rela_bg);
        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);


        img_dev_logo = findViewById(R.id.img_dev_logo);
        txt_dev_tips = findViewById(R.id.txt_dev_tips);
        txt_research = findViewById(R.id.txt_research);
        txt_research.setOnClickListener(listener);

        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(SearchUnprovisionedCurtain.this));
        selectLightGroupAdapter = new SelectLightGroupAdapter(SearchUnprovisionedCurtain.this, SearchUnprovisionedCurtain.this);
        recyclerview.setAdapter(selectLightGroupAdapter);

        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);

        line_search = findViewById(R.id.line_search);
        line_search.setOnClickListener(listener);
        line_join = findViewById(R.id.line_join);
        line_join.setOnClickListener(listener);
        txt_create_group = findViewById(R.id.txt_create_group);
        txt_create_group.setOnClickListener(listener);

        GetGroupAddress();

    }

    DevGroupBean devGroupBean = null;
    ProvisionedMeshNode mmmeshNode;

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view == rela_back) {

                if (null != popcreategroup && popcreategroup.isShowing()) {
                    popcreategroup.dismiss();
                    return;
                }
                finish();

            } else if (view == line_search) {
                FscMeshSdk.getApi2().setClearCacheRequired();
                FscMeshSdk.getApi2().Disconnect();
                mextendedBluetoothDevice = null;
                LoadingDialog.getInstance().Show(SearchUnprovisionedCurtain.this, getString(R.string.requesting));
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FscMeshSdk.getApi2().StartScan(5000, MeshManagerApi.MESH_PROVISIONING_UUID, (byte) 0x85, (byte) 0x01, fscScancallback);

                    }
                }, 1500);

            } else if (view == txt_research) {
                FscMeshSdk.getApi2().setClearCacheRequired();
                FscMeshSdk.getApi2().Disconnect();
                mextendedBluetoothDevice = null;
                LoadingDialog.getInstance().Show(SearchUnprovisionedCurtain.this, getString(R.string.requesting));
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FscMeshSdk.getApi2().StartScan(5000, MeshManagerApi.MESH_PROVISIONING_UUID, (byte) 0x85, (byte) 0x01, fscScancallback);
                        img_dev_logo.setBackgroundResource(R.mipmap.img_devnotfound);
                        txt_dev_tips.setText("正在搜索...");
                    }
                }, 1500);


            } else if (view == line_join) {

                devGroupBean = null;
                for (int i = 0; i < selectLightGroupAdapter.getLightGroupBeans().size(); i++) {
                    if (selectLightGroupAdapter.getLightGroupBeans().get(i).isSelect()) {
                        devGroupBean = selectLightGroupAdapter.getLightGroupBeans().get(i);
                    }
                }

                if (null == devGroupBean) {
                    ToastUtils.showLong("请选择分组");
                } else if (null == mextendedBluetoothDevice) {
                    ToastUtils.showLong("请先搜索设备");
                } else {
                    FscMeshSdk.getApi2().CloseAdv();
                    LoadingDialog.getInstance().Show(SearchUnprovisionedCurtain.this, getString(R.string.requesting));

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            FscMeshSdk.getApi2().StartProvisionedNode(mextendedBluetoothDevice, new ProvisioningCallback() {
                                @Override
                                public void onCompleted(ProvisionedMeshNode meshNode) {
                                    mmmeshNode = meshNode;
                                    byte isGroupleader = (byte) 0x01;
                                    if (devGroupBean.getDevice_count() == 0) {
                                        isGroupleader = (byte) 0x01;
                                    } else {
                                        isGroupleader = (byte) 0x00;
                                    }
                                    FscMeshSdk.getApi2().AddNodeToGroup(mmmeshNode, intToTwoByteArray(Integer.parseInt(devGroupBean.getAddress() + "C0", 16)), isGroupleader, new SubscribeGroupCallback() {
                                        @Override
                                        public void onCompleted() {

                                            FormBody.Builder formbody = new FormBody.Builder();
                                            formbody.add("second_type", String.format("%02X", devTypeBean.getDevType()) + mextendedBluetoothDevice.getSecondType());
                                            formbody.add("mac", mextendedBluetoothDevice.getDevice().getAddress());
                                            formbody.add("device_ver", mextendedBluetoothDevice.getDevice_ver());
                                            formbody.add("family_id", devGroupBean.getFamily_id() + "");
                                            formbody.add("room_id", devGroupBean.getRoom_id() + "");
                                            formbody.add("group_id", devGroupBean.getId() + "");
                                            formbody.add("group_name", devGroupBean.getGroup_name());
                                            formbody.add("address", devGroupBean.getAddress());
                                            formbody.add("device_type", devGroupBean.getDevice_type());
                                            formbody.add("device_id", mextendedBluetoothDevice.getDevice().getAddress());
                                            formbody.add("destination", meshNode.getUnicastAddress() + "");
                                            createDevice(formbody);
                                            Log.e("vvvv", "=====onCompleted======" + mextendedBluetoothDevice.getSecondType() + "    " + devGroupBean.getGroup_name() + "    " + devGroupBean.getAddress());

                                        }

                                        @Override
                                        public void onFailed() {
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    LoadingDialog.getInstance().Dismiss();
                                                }
                                            });

                                        }
                                    });


                                    //    Log.e("vvv", "secondType:" + mextendedBluetoothDevice.getSecondType() + "   group_name:" + lightGroupBean.getGroup_name());
                                }

                                @Override
                                public void onFailed(String erro) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                FscMeshSdk.getApi2().Disconnect();
                                                LoadingDialog.getInstance().Dismiss();
                                                TipsDialogOne tipsDialogOne = new TipsDialogOne(SearchUnprovisionedCurtain.this);
                                                tipsDialogOne.show();
                                                tipsDialogOne.txt_title.setText("设备繁忙，请重试！");
                                                tipsDialogOne.txt_cancler.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        tipsDialogOne.dismiss();
                                                    }
                                                });
                                            } catch (Exception e) {

                                            }

                                        }
                                    });

                                }
                            });
                        }
                    }, 1000);


                }

            } else if (view == txt_create_group) {

                if (null != popcreategroup && popcreategroup.isShowing()) {
                    return;
                }
                LoadingDialog.getInstance().Show(SearchUnprovisionedCurtain.this, getString(R.string.requesting));
                getCurtainGroupNameList();

            }

        }
    };


    private void createDevice(FormBody.Builder formbody) {
        RequestOkhttp.getInstance().PostBody(Api.createCurtain, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("vvv", "========添加设备入组失败==========" + e.toString());
                LoadingDialog.getInstance().Dismiss();

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = response.body().string();
                Log.e("vvv", "=======添加设备入组===========" + str);
                //{"code":1,"msg":"操作成功","time":1731638946,"data":null}


                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            JSONObject jsonObject = JSON.parseObject(str);
                            mextendedBluetoothDevice = null;
                            FscMeshSdk.getApi2().Disconnect();
                            GetGroupAddress();
                            LoadingDialog.getInstance().Dismiss();
                            if (jsonObject.getIntValue("code") == 1) {
                                TipsDialogOne tipsDialogOne = new TipsDialogOne(SearchUnprovisionedCurtain.this);
                                tipsDialogOne.show();
                                tipsDialogOne.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        finish();
                                    }
                                });
                                tipsDialogOne.txt_title.setText("入网成功！");
                                tipsDialogOne.txt_cancler.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        tipsDialogOne.dismiss();
                                    }
                                });

                                FamilyModelFactory.getInstance().getFamilyModel().getRoomid().setValue(roombean.getId());
                                RequestOkhttp.getInstance().SyncMeshData();
                            }
                        } catch (Exception e) {

                        }
                    }
                }, 100);


            }
        });

    }


    private void GetGroupAddress() {
        ArrayList<DevGroupBean> devGroupBeans = new ArrayList<>();

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", "" + roombean.getFamily_id());
        formbody.add("room_id", "" + roombean.getId());
        formbody.add("device_type", String.format("%02X", devTypeBean.getDevType()));

        Log.e("vvvv", "========" + roombean.getFamily_id() + "    " + roombean.getId() + "    " + Integer.parseInt(String.format("%02X", devTypeBean.getDevType()), 16));
        RequestOkhttp.getInstance().PostBody(Api.curtainGroupList, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {


            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String str_res = response.body().string();
                Log.e("vvvv", "======窗帘===GetGroupAddress============" + str_res);

                try {
                    JSONObject jsonObject = JSON.parseObject(str_res);
                    if (jsonObject.getIntValue("code") == 1) {

                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < jsonArray.size(); i++) {
                            DevGroupBean devGroupBean = JSON.parseObject(jsonArray.getString(i), DevGroupBean.class);
                            if (null != devGroupBean) {
                                devGroupBeans.add(devGroupBean);
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                selectLightGroupAdapter.setLightGroupBeans(devGroupBeans);
                            }
                        });

                    }
                } catch (Exception e) {

                }
            }
        });


    }


    private ArrayList<GroupNameBean> groupNameBeans = new ArrayList<>();


    private void getCurtainGroupNameList() {

        Map<String, Object> params10 = new HashMap<>();

        RequestOkhttp.getInstance().Get(RequestOkhttp.getInstance().getUrl(Api.curtainTypeList, params10), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        LoadingDialog.getInstance().Dismiss();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = response.body().string();
                Log.e("", "========获取窗帘群组===========" + str);
                try {

                    JSONObject jsonObject = JSON.parseObject(str);
                    if (jsonObject.getIntValue("code") == 1) {
                        groupNameBeans.clear();
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < jsonArray.size(); i++) {
                            GroupNameBean groupNameBean = JSON.parseObject(jsonArray.getString(i), GroupNameBean.class);
                            groupNameBeans.add(groupNameBean);
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                LoadingDialog.getInstance().Dismiss();
                                ShowCreateGroupPOP(rela_bg);
                            }
                        });
                    }

                } catch (Exception e) {

                }
            }
        });


    }


    private PopupWindow popcreategroup = null;
    private int name_nub = 1;

    @SuppressLint("NewApi")
    private void ShowCreateGroupPOP(View parent) {

        View pop_view = LayoutInflater.from(SearchUnprovisionedCurtain.this).inflate(R.layout.pop_create_curtaingroup, null);
        RelativeLayout rela_pop_back = pop_view.findViewById(R.id.rela_pop_back);
        NumberPicker numberpicker1 = pop_view.findViewById(R.id.numberpicker1);

        String[] gn = new String[groupNameBeans.size()];

        for (int i = 0; i < groupNameBeans.size(); i++) {
            gn[i] = groupNameBeans.get(i).getText();
        }

        numberpicker1.setDisplayedValues(gn);
        numberpicker1.setMinValue(0);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            numberpicker1.setTextColor(getResources().getColor(R.color.black));
            numberpicker1.setSelectionDividerHeight(1);
        }

        numberpicker1.setMaxValue(groupNameBeans.size() - 1);

        numberpicker1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // 关闭编辑模式
        numberpicker1.setWrapSelectorWheel(false);
        numberpicker1.setValue(name_nub);
        numberpicker1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                name_nub = i1;
            }
        });


        rela_pop_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popcreategroup.dismiss();
            }
        });
        TextView txt_pop_create = pop_view.findViewById(R.id.txt_pop_create);
        txt_pop_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadingDialog.getInstance().Show(SearchUnprovisionedCurtain.this, getString(R.string.requesting));
                String curtain_name = groupNameBeans.get(name_nub).getText();
                String curtain_code = groupNameBeans.get(name_nub).getCode();
                String device_type = String.format("%02X", devTypeBean.getDevType());
                String family_id = roombean.getFamily_id() + "";
                String room_id = roombean.getId() + "";
                //    String address = GenerateAddress.getInstance().GenerateCurtainGroupAdr() + "";
                //   String address_number = getLastTwoDigitsDecimal(address) + "";
                // CreateGroup(group_name, group_code, family_id, room_id, address, address_number);
                enableGroupAddress(curtain_name, curtain_code, device_type, family_id, room_id);
                popcreategroup.dismiss();
            }
        });


        popcreategroup = new PopupWindow(pop_view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popcreategroup.setElevation(3);
        popcreategroup.setAnimationStyle(R.style.PopupAnimation);
        popcreategroup.setOutsideTouchable(false);
        popcreategroup.showAtLocation(parent, Gravity.BOTTOM, 0, 0);

    }


    private void enableGroupAddress(String curtain_name, String curtain_code, String device_type, String family_id, String room_id) {
        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("curtain_name", curtain_name);
        formbody.add("curtain_code", curtain_code);
        formbody.add("device_type", device_type);
        formbody.add("family_id", family_id);
        formbody.add("room_id", room_id);

        RequestOkhttp.getInstance().PostBody(Api.enableGroupAddress, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        LoadingDialog.getInstance().Dismiss();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = response.body().string();
                Log.e("vvvv", "===========获取窗帘可用群组地址============" + str);
                //{"code":1,"msg":"返回成功","time":1734503024,"data":0}
                try {
                    JSONObject jsonObject = JSON.parseObject(str);
                    if (jsonObject.getIntValue("code") == 1) {
                        CreateGroup(curtain_name, curtain_code, device_type, family_id, room_id, String.format("%02X", Integer.parseInt(jsonObject.getIntValue("data") + "")), jsonObject.getIntValue("data") + "");

                    }
                } catch (Exception e) {

                }
            }
        });
    }

    private void CreateGroup(String curtain_name, String curtain_code, String device_type, String family_id, String room_id, String address, String address_number) {


        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("curtain_name", curtain_name);
        formbody.add("curtain_code", curtain_code);
        formbody.add("device_type", device_type);
        formbody.add("family_id", family_id);
        formbody.add("room_id", room_id);
        formbody.add("address", address);
        formbody.add("address_number", address_number);

        Log.e("vvvv", "======创建窗帘群组=========" + curtain_name + "   " + device_type + "    " + address + "    " + address_number);
        RequestOkhttp.getInstance().PostBody(Api.curtainCreategroup, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        LoadingDialog.getInstance().Dismiss();
                    }
                });

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String str_res = response.body().string();
                Log.e("VVVV", "==========创建窗帘群组==========" + str_res);
                //  {"code":1,"msg":"操作成功","time":1727252310,"data":null}


                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            JSONObject jsonObject = JSON.parseObject(str_res);
                            if (jsonObject.getIntValue("code") == 1) {
                                GetGroupAddress();
                            }

                        } catch (Exception e) {

                        }
                        FamilyModelFactory.getInstance().getFamilyModel().getRoomid().setValue(roombean.getId());

                        LoadingDialog.getInstance().Dismiss();
                    }
                }, 2000);


            }
        });


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        FscMeshSdk.getApi2().StopScan();
        FscMeshSdk.getApi2().CloseAdv();

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (null != popcreategroup && popcreategroup.isShowing()) {
            popcreategroup.dismiss();
        }

    }

    @Override
    public void onSelect(int p, boolean select) {

        for (int i = 0; i < selectLightGroupAdapter.getLightGroupBeans().size(); i++) {
            if (i == p) {
                selectLightGroupAdapter.getLightGroupBeans().get(i).setSelect(true);
            } else {
                selectLightGroupAdapter.getLightGroupBeans().get(i).setSelect(false);
            }
        }
        selectLightGroupAdapter.notifyDataSetChanged();


    }


    private ExtendedBluetoothDevice mextendedBluetoothDevice = null;
    FscScancallback fscScancallback = new FscScancallback() {

        @Override
        public void onFscScanResult(ExtendedBluetoothDevice extendedBluetoothDevice) {
            mextendedBluetoothDevice = extendedBluetoothDevice;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    FscMeshSdk.getApi2().OpenSearchAdv(mextendedBluetoothDevice.getAddress());
                    img_dev_logo.setBackgroundResource(R.mipmap.dev_img_curtain);
                    txt_dev_tips.setText(extendedBluetoothDevice.getAddress());
                    txt_research.setVisibility(View.VISIBLE);
                    LoadingDialog.getInstance().Dismiss();
                }
            });

        }

        @Override
        public void onScanStop() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    LoadingDialog.getInstance().Dismiss();
                    if (null == mextendedBluetoothDevice) {

                        txt_dev_tips.setText("未找到设备！");
                        img_dev_logo.setBackgroundResource(R.mipmap.img_devnotfound);
                    }
                }
            });

        }

    };


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


    private byte[] intToTwoByteArray(int value) {
        if (value < 0 || value > 0xFFFF) {
            throw new IllegalArgumentException("Value must be between 0 and 65535");
        }

        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) ((value >> 8) & 0xFF); // 高字节
        byteArray[1] = (byte) (value & 0xFF);       // 低字节

        return byteArray;
    }


    private int getLastTwoDigitsDecimal(String numberStr) {
        // 确保字符串长度至少为2，否则可能需要处理异常
        if (numberStr == null || numberStr.length() < 2) {
            throw new IllegalArgumentException("字符串长度不足2位");
        }

        // 提取后两位
        String lastTwoDigitsStr = numberStr.substring(numberStr.length() - 2);

        // 将字符串转换为整数
        return Integer.parseInt(lastTwoDigitsStr);
    }


}