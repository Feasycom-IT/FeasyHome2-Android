package com.feasycom.feasyhome.ui.mainfragment;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.IntentUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.adapter.FragAdapter2;
import com.feasycom.feasyhome.adapter.SelectFamilyAdapter;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseFragment;
import com.feasycom.feasyhome.bean.FamilyBean;
import com.feasycom.feasyhome.bean.RoomBean;
import com.feasycom.feasyhome.callback.ItemClickCallback;

import com.feasycom.feasyhome.logic.FamilyModelFactory;
import com.feasycom.feasyhome.ui.FamilyListActivity;
import com.feasycom.feasyhome.ui.LoginActivity;
import com.feasycom.feasyhome.ui.MainActivity;
import com.feasycom.feasyhome.ui.SelectDevTypeActivity;
import com.feasycom.feasyhome.ui.RoomFragment;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.feasycom.feasyhome.ui.dialog.TipsDialogOne;
import com.feasycom.feasyhome.utils.MaxHeightLinearLayoutManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;
import com.hjq.permissions.permission.PermissionLists;
import com.hjq.permissions.permission.base.IPermission;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import no.nordicsemi.android.mesh.fsdk.FscConnectCallback;
import no.nordicsemi.android.mesh.fsdk.FscMeshSdk;
import no.nordicsemi.android.mesh.fsdk.QuerNodesCallback;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;


public class DevFragment extends BaseFragment {


    private View view_status;
    private TabLayout tablayout;
    private ViewPager2 viewpager;
    private List<RoomFragment> fragments = new ArrayList<>();
    private List<String> names = new ArrayList<>();
    private LinearLayout line_family_select;
    private RelativeLayout rela_join, rela_tips;
    private TextView txt_tips, txt_btn, txt_family_name, txt_connect_state;

    private SmartRefreshLayout refreshLayout;
    private FragAdapter2 fragAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.devfragment, container, false);
        view_status = view.findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        txt_connect_state = view.findViewById(R.id.txt_connect_state);
        line_family_select = view.findViewById(R.id.line_family_select);
        line_family_select.setOnClickListener(listener);
        rela_join = view.findViewById(R.id.rela_join);
        rela_join.setOnClickListener(listener);

        txt_family_name = view.findViewById(R.id.txt_family_name);
        txt_btn = view.findViewById(R.id.txt_btn);
        txt_tips = view.findViewById(R.id.txt_tips);
        rela_tips = view.findViewById(R.id.rela_tips);
        txt_btn.setOnClickListener(listener);


        tablayout = view.findViewById(R.id.tablayout);
        viewpager = view.findViewById(R.id.viewpager);

        fragAdapter = new FragAdapter2(getChildFragmentManager(), getLifecycle());
        viewpager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewpager.setOffscreenPageLimit(5);
        viewpager.setAdapter(fragAdapter);
        viewpager.setUserInputEnabled(true);
        TabLayoutMediator mediator = new TabLayoutMediator(tablayout, viewpager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position < names.size()) {
                    tab.setText(names.get(position));
                }

            }
        });
        mediator.attach();

        refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                if (CheckEnvironment() == 0) {
                    GetFamilylist();
                } else if (CheckEnvironment() == -1) {
                    //GPS未打开
                    refreshLayout.finishRefresh();
                    ToastUtils.showShort(getString(R.string.tips6));
                } else if (CheckEnvironment() == -2) {
                    //蓝牙未打开
                    refreshLayout.finishRefresh();
                    ToastUtils.showShort(getString(R.string.bluetoothisnotturnedon));
                } else if (CheckEnvironment() == -3) {
                    //权限未给足
                    refreshLayout.finishRefresh();
                    ToastUtils.showShort(getString(R.string.tips8));
                }

            }
        });

        ObserverCurrentFamilyBean();


        return view;
    }


    private void ObserverCurrentFamilyBean() {
        FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().observe(getViewLifecycleOwner(), new Observer<FamilyBean>() {
            @Override
            public void onChanged(FamilyBean familyBean) {
                Log.e("vvv", "======当前家庭变化==2222222====");

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (null != familyBean) {
                            handler.sendEmptyMessage(1001);
                            txt_family_name.setText(familyBean.getFamily_name());
                            GetRoomList();
                        } else {
                            txt_family_name.setText(getString(R.string.createafamily));
                            FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().setValue(null);

                        }

                    }
                });


            }
        });

        FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().observe(getViewLifecycleOwner(), new Observer<ArrayList>() {
            @Override
            public void onChanged(ArrayList arrayList) {
                Log.e("vvv", "======家庭数据变化==2222222====");
                if (null != arrayList && arrayList.size() > 0) {

                    if (SPUtils.getInstance().getInt("select_family", 0) >= arrayList.size()) {
                        SPUtils.getInstance().put("select_family", arrayList.size() - 1, true);
                    }
                    FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().setValue((FamilyBean) arrayList.get(SPUtils.getInstance().getInt("select_family", 0)));

                } else {
                    FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().setValue(null);
                }

            }
        });

        FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().observe(getViewLifecycleOwner(), new Observer<ArrayList>() {
            @Override
            public void onChanged(ArrayList arrayList) {
                Log.e("vvv", "======房间数据变化==2222222====");

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        fragAdapter.RemoveFragments();
                        fragments.clear();
                        names.clear();
                        tablayout.setVisibility(View.GONE);

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (null != arrayList && arrayList.size() > 0) {

                                    for (int i = 0; i < arrayList.size(); i++) {
                                        RoomBean roomBean = (RoomBean) arrayList.get(i);
                                        RoomFragment roomFragment = new RoomFragment(roomBean);
                                        fragments.add(roomFragment);
                                        names.add(roomBean.getRoom_name());
                                    }
                                    tablayout.setVisibility(View.VISIBLE);
                                    fragAdapter.setFragments(fragments);

                                }
                            }
                        }, 500);

                    }
                });
            }
        });


        FamilyModelFactory.getInstance().getFamilyModel().getNeedrefresh().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                refreshLayout.autoRefresh();
            }
        });

    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view == line_family_select) {

                if (null == FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue() || FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue().size() <= 0) {
                    //去创建房间
                    startActivity(new Intent(getActivity(), FamilyListActivity.class));
                } else {
                    ShowPopselectfamily(line_family_select);
                }

            } else if (view == rela_join) {

                if (CheckEnvironment() == 0) {
                    //权限 GPS  蓝牙都已经通过

                    if (null == FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue()) {
                        ToastUtils.showShort(getString(R.string.tips9));
                    } else if (null == FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().getValue()) {
                        ToastUtils.showShort(getString(R.string.tips10));
                    } else {
                        FscMeshSdk.getApi2().StopScan();
                        //     Log.e("vvvv", "=========进入房间=============" + roomList.get(viewpager.getCurrentItem()).getRoom_name());
                        RoomBean roomBean = FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().getValue().get(viewpager.getCurrentItem());
                        if (null == roomBean) {
                            ToastUtils.showShort(getString(R.string.tips11));
                            refreshLayout.autoRefresh();
                        } else {
                            Intent intent = new Intent(getActivity(), SelectDevTypeActivity.class);
                            intent.putExtra("roombean", roomBean);
                            startActivity(intent);
                        }

                    }

                } else if (CheckEnvironment() == -1) {
                    //GPS未打开
                    ToastUtils.showShort(getString(R.string.tips6));
                } else if (CheckEnvironment() == -2) {
                    //蓝牙未打开
                    ToastUtils.showShort(getString(R.string.bluetoothisnotturnedon));
                } else if (CheckEnvironment() == -3) {
                    //权限未给足
                    ToastUtils.showShort(getString(R.string.tips8));
                }


            } else if (view == txt_btn) {

                if (CheckEnvironment() == -3) {
                    //权限未给足
                    CheckPermissions();
                } else if (CheckEnvironment() == -1) {
                    //GPS未打开
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);

                } else if (CheckEnvironment() == -2) {
                    //蓝牙未打开
                    enableBluetooth(getActivity());
                }

            }

        }
    };


    @Override
    public void onResume() {
        super.onResume();


        if (CheckEnvironment() == 0) {
            //权限 GPS  蓝牙都已经通过
            rela_tips.setVisibility(View.GONE);

            if (fragments.size() <= 0) {
                if (!refreshLayout.isRefreshing()) {
                    Log.e("vvvv", "=======onResume======autoRefresh=1===");
                    refreshLayout.autoRefresh();
                }

            } else if (null == FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean() || null == FscMeshSdk.getApi2().getMeshNetwork()) {
                if (!refreshLayout.isRefreshing()) {
                    Log.e("vvvv", "=======onResume======autoRefresh=2===");
                    refreshLayout.autoRefresh();
                }
            } else if (fragments.size() > 0 && !FscMeshSdk.getApi2().isConnect()) {
                Log.e("vvvv", "=======onResume======1002====");
                handler.sendEmptyMessage(1002);
            }

        } else if (CheckEnvironment() == -1) {
            //GPS未打开
            rela_tips.setVisibility(View.VISIBLE);
            txt_tips.setText(getString(R.string.tips6));
            txt_btn.setText(getString(R.string.open));
        } else if (CheckEnvironment() == -2) {
            //蓝牙未打开
            rela_tips.setVisibility(View.VISIBLE);
            txt_tips.setText(getString(R.string.bluetoothisnotturnedon));
            txt_btn.setText(getString(R.string.open));
        } else if (CheckEnvironment() == -3) {
            //权限未给足
            rela_tips.setVisibility(View.VISIBLE);
            txt_tips.setText(getString(R.string.tips7));
            txt_btn.setText(getString(R.string.open));
        }


    }


    private int CheckEnvironment() {

        if (!CheckPermissions(getContext())) {
            rela_tips.setVisibility(View.VISIBLE);
            return -3;
        }

        if (!isGPSEnabled(getContext())) {
            rela_tips.setVisibility(View.VISIBLE);
            return -1;
        }
        if (!isBluetoothEnabled()) {
            rela_tips.setVisibility(View.VISIBLE);
            return -2;
        }

        rela_tips.setVisibility(View.GONE);
        return 0;
    }


    private boolean isGPSEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }


    @SuppressLint("MissingPermission")
    private void enableBluetooth(Context context) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivity(enableIntent);
            }
        }
    }


    private List<IPermission> getiPermissionList() {
        List<IPermission> iPermissionList = new ArrayList<>();
        iPermissionList.add(PermissionLists.getBluetoothAdvertisePermission());
        iPermissionList.add(PermissionLists.getBluetoothConnectPermission());
        iPermissionList.add(PermissionLists.getBluetoothScanPermission());
        iPermissionList.add(PermissionLists.getAccessFineLocationPermission());
        return iPermissionList;
    }

    private boolean CheckPermissions(Context context) {

        //new IPermission[]{Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}
        return XXPermissions.isGrantedPermissions(context, getiPermissionList());
    }


    private void CheckPermissions() {

        XXPermissions.with(getActivity())
                .permissions(getiPermissionList())
                .request(new OnPermissionCallback() {
                    @Override
                    public void onResult(@NonNull List<IPermission> grantedList, @NonNull List<IPermission> deniedList) {
                        boolean allGranted = deniedList.isEmpty();
                        if (allGranted) {
                            refreshLayout.autoRefresh();
                        } else {

                            boolean doNotAskAgain = XXPermissions.isDoNotAskAgainPermissions(getActivity(), deniedList);
                            if (doNotAskAgain) {
                                new AlertDialog.Builder(getActivity())
                                        .setMessage(getString(R.string.tips12))
                                        .setPositiveButton(getString(R.string.set), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = IntentUtils.getLaunchAppDetailsSettingsIntent("com.feasycom.feasyhome");
                                                startActivity(intent);
                                                dialog.dismiss();
                                            }
                                        }).setNegativeButton(getString(R.string.cancler), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }


                        }
                    }

                });
    }


    private PopupWindow popselectfamily = null;

    private void ShowPopselectfamily(View parent) {


        View pop_view = LayoutInflater.from(getContext()).inflate(R.layout.popselectfamily, null);
        RecyclerView recycler_family = pop_view.findViewById(R.id.recycler_family);
        recycler_family.setLayoutManager(new MaxHeightLinearLayoutManager(getActivity(), 4));
        SelectFamilyAdapter selectFamilyAdapter = new SelectFamilyAdapter(getActivity(), new ItemClickCallback() {
            @Override
            public void onItemClick(int p, String str) {
                //    Log.e("vvv", "===---=======" + p);
                SPUtils.getInstance().put("select_family", p, true);
                if (!FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue().getNetwork_id().equals(FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue().get(SPUtils.getInstance().getInt("select_family", 0)).getNetwork_id())) {
                    FscMeshSdk.getApi2().setClearCacheRequired();
                    FscMeshSdk.getApi2().Disconnect();
                    FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().setValue(FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue().get(SPUtils.getInstance().getInt("select_family", 0)));
                }
                ClosePopselectfamily();

            }
        });
        selectFamilyAdapter.setFamilyBeans(FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue());
        recycler_family.setAdapter(selectFamilyAdapter);


        LinearLayout line_family_manage = pop_view.findViewById(R.id.line_family_manage);
        line_family_manage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClosePopselectfamily();
                startActivity(new Intent(getActivity(), FamilyListActivity.class));

            }
        });

        popselectfamily = new PopupWindow(pop_view, ConvertUtils.dp2px(200), ViewGroup.LayoutParams.WRAP_CONTENT);
        popselectfamily.setElevation(3);
        popselectfamily.setOutsideTouchable(true);
        popselectfamily.showAsDropDown(parent, 0, ConvertUtils.dp2px(10));
        //    Log.e("vvvv", "-==========切换家庭==========");
    }


    private void ClosePopselectfamily() {
        if (null != popselectfamily && popselectfamily.isShowing()) {
            popselectfamily.dismiss();
            popselectfamily = null;
        }
    }


    private void GetFamilylist() {

        FormBody.Builder formbody = new FormBody.Builder();
        RequestOkhttp.getInstance().PostBody(Api.FamilyList, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.finishRefresh();
                        FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().setValue(null);

                        //    Log.e("vvv", "=======家庭列表===1=======" + e.toString());
                    }
                }, 1000);

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String str_res = response.body().string();
                //{"code":1,"msg":"返回成功","time":1723190757,"data":[]}
                //  Log.e("vvv", "=======家庭列表==2========" + str_res);
                //{"code":401,"msg":"请登录后操作","time":1740385701,"data":null}
                try {
                    JSONObject jsonObject = JSON.parseObject(str_res);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (jsonObject.getIntValue("code") == 1) {

                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                if (jsonArray.size() <= 0) {
                                    //没有家庭
                                    FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().setValue(null);

                                } else {

                                    ArrayList<FamilyBean> familyBeans = new ArrayList<>();
                                    //有家庭
                                    for (int i = 0; i < jsonArray.size(); i++) {

                                        FamilyBean familyBean = JSON.parseObject(jsonArray.getString(i), FamilyBean.class);
                                        if (null != familyBean) {
                                            familyBeans.add(familyBean);
                                        }
                                    }

                                    FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().setValue(familyBeans);

                                }

                            } else {
                                txt_tips.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        SPUtils.getInstance().put("userinfo", "", true);
                                        TipsDialogOne tipsDialogOne = new TipsDialogOne(getActivity());
                                        tipsDialogOne.show();
                                        tipsDialogOne.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialogInterface) {
                                                startActivity(new Intent(getActivity(), LoginActivity.class));
                                                ActivityUtils.finishActivity(MainActivity.class);
                                            }
                                        });
                                        tipsDialogOne.txt_title.setText(jsonObject.getString("msg"));
                                        tipsDialogOne.txt_cancler.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                tipsDialogOne.dismiss();

                                            }
                                        });
                                    }
                                });

                            }

                        }
                    });
                    refreshLayout.finishRefresh();
                } catch (Exception e) {
                    refreshLayout.finishRefresh();
                }

            }
        });


    }


    private void GetRoomList() {

        Map<String, Object> params10 = new HashMap<>();

        params10.put("family_id", FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue().getId() + "");
        //    params10.put("room_id", "0");

        RequestOkhttp.getInstance().Get(RequestOkhttp.getInstance().getUrl(Api.IndexPageDate, params10), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.finishRefresh();
                        FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().setValue(null);
                    }
                });


            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String str_res = response.body().string();
                //{"code":1,"msg":"返回成功","time":1723196934,"data":{"familyList":[],"roomList":[],"lightGroupList":[],"curtainGroupList":[],"panelList":[],"voiceList":[]}}
                //  Log.e("vvv", "=======GetRoomList==========" + str_res);
                try {

                    JSONObject jsonObject = JSON.parseObject(str_res);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (jsonObject.getIntValue("code") == 1) {

                                JSONObject json_data = JSON.parseObject(jsonObject.getString("data"));
                                //有房间数据
                                JSONArray roomarray = json_data.getJSONArray("roomList");
                                if (roomarray.size() > 0) {
                                    ArrayList<RoomBean> roomBeans = new ArrayList<>();
                                    for (int i = 0; i < roomarray.size(); i++) {
                                        RoomBean roomBean = JSON.parseObject(roomarray.getString(i), RoomBean.class);
                                        roomBeans.add(roomBean);
                                    }
                                    FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().setValue(roomBeans);

                                } else {
                                    FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().setValue(null);
                                }

                            } else {
                                FamilyModelFactory.getInstance().getFamilyModel().getRoomBeans().setValue(null);
                            }
                        }
                    });

                    refreshLayout.finishRefresh();
                } catch (Exception e) {

                }

            }
        });


    }


    public int generateRandomInRange(int min, int max) {

        if (min > max) {
            throw new IllegalArgumentException("最小值不能大于最大值");
        }
        return new Random().nextInt(max - min + 1) + min;
    }

    private String str_json_data;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:

                    if (null == FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue()) {
                        Log.e("vvvv", "=======1001  当前家庭为 null =========");
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!refreshLayout.isRefreshing()) {
                                    refreshLayout.autoRefresh();
                                }

                            }
                        }, 2000);

                        return;
                    }

                    str_json_data = new String(EncodeUtils.base64Decode(FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue().getNetwork_data()));
                    if (null == FscMeshSdk.getApi2().getMeshNetwork()) {
                        FscMeshSdk.getApi2().Disconnect();
                        if (str_json_data.indexOf("$schema") != -1) {
                            FscMeshSdk.getApi2().importMeshNetworkJson(str_json_data);
                            handler.sendEmptyMessageDelayed(1002, 1000);
                        } else {
                            ToastUtils.showLong("Mesh网络格式不对，导入失败！");
                        }
                    } else if (null != FscMeshSdk.getApi2().getMeshNetwork() && !FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue().getNetwork_id().equals(FscMeshSdk.getApi2().getMeshNetwork().getMeshUUID())) {
                        FscMeshSdk.getApi2().Disconnect();
                        FscMeshSdk.getApi2().importMeshNetworkJson(str_json_data);
                        handler.sendEmptyMessageDelayed(1002, 1000);
                    } else {

                        //网络没变
                        if (!FscMeshSdk.getApi2().isConnect()) {
                            //    FscMeshSdk.getApi2().importMeshNetworkJson(str_json_data);
                            handler.sendEmptyMessageDelayed(1002, 1000);
                        }

                    }


                    break;
                case 1002:

                    if (null == FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue()) {
                        Log.e("vvvv", "=======1002  当前家庭为 null =========");
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!refreshLayout.isRefreshing()) {
                                    refreshLayout.autoRefresh();
                                }

                            }
                        }, 2000);
                        return;
                    }


                    if (null == FscMeshSdk.getApi2().getMeshNetwork()) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!refreshLayout.isRefreshing()) {
                                    refreshLayout.autoRefresh();
                                }

                            }
                        }, 2000);
                        return;
                    }


                    FscMeshSdk.getApi2().getNodes(FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue().getNetwork_id(), new QuerNodesCallback() {
                        @Override
                        public void onNodesback(List<ProvisionedMeshNode> nodes) {

                            if (null != nodes && nodes.size() > 1) {

                                Log.e("vvv", "========当前网络已入网设备数量===========" + nodes.size());

                                if (FscMeshSdk.getApi2().isConnect()) {
                                    txt_connect_state.setText(getString(R.string.connected));
                                    txt_connect_state.setTextColor(getResources().getColor(R.color.txt_blue));
                                } else {
                                    LoadingDialog.getInstance().Show(getActivity(), getString(R.string.connecting)+"...");

                                    // int newUnicastAddress = FscMeshSdk.getApi2().getMeshNetwork().nextAvailableUnicastAddress(1, FscMeshSdk.getApi2().getMeshNetwork().getSelectedProvisioner());
                                    int newUnicastAddress = generateRandomInRange(4000, 6553);
                                    //  Log.e("vvv", "=========newUnicastAddress=======" + newUnicastAddress);
                                    //   FscMeshSdk.getApi2().getMeshNetwork().getNodes().get(0).setUnicastAddress(newUnicastAddress);
                                    FscMeshSdk.getApi2().getMeshNetwork().getSelectedProvisioner().setProvisionerAddress(newUnicastAddress);
                                    try {
                                        FscMeshSdk.getApi2().getMeshNetwork().updateProvisioner(FscMeshSdk.getApi2().getMeshNetwork().getSelectedProvisioner());
                                        FscMeshSdk.getApi2().UpMeshnetwork(FscMeshSdk.getApi2().getMeshNetwork());
                                    } catch (Exception e) {
                                        Log.e("vvv", "=======updateProvisioner============" + e.toString());
                                    }

                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            if (null == FscMeshSdk.getApi2().getMeshNetwork() || null == FscMeshSdk.getApi2().getMeshNetwork().getId()) {
                                                refreshLayout.autoRefresh();
                                                Log.e("vvvv", "========数据异常，重新刷新1===========");
                                            } else {

                                                FscMeshSdk.getApi2().ConnectNetWork(FscMeshSdk.getApi2().getMeshNetwork().getId(), new FscConnectCallback() {
                                                    @Override
                                                    public void onDeviceReady(BluetoothDevice device) {

                                                        handler.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    txt_connect_state.setText(getString(R.string.connected));
                                                                    txt_connect_state.setTextColor(getResources().getColor(R.color.txt_blue));
                                                                    LoadingDialog.getInstance().Dismiss();

                                                                } catch (Exception e) {
                                                                    Log.e("vvvv", "==========ConfigCompositionDataGet=======" + e.toString());
                                                                }


                                                            }
                                                        });


                                                    }

                                                    @Override
                                                    public void onDisconnected(BluetoothDevice device) {
                                                        handler.post(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                try {
                                                                    txt_connect_state.setText(getString(R.string.notconnected));
                                                                    txt_connect_state.setTextColor(getResources().getColor(R.color.red));
                                                                    LoadingDialog.getInstance().Dismiss();
                                                                } catch (Exception e) {

                                                                }

                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onFail(String str) {
                                                        handler.post(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                try {
                                                                    Log.e("vvvvv", "=====连接网络节点=======" + str);
                                                                    txt_connect_state.setText(getString(R.string.notconnected));
                                                                    txt_connect_state.setTextColor(getResources().getColor(R.color.red));
                                                                    LoadingDialog.getInstance().Dismiss();
                                                                } catch (Exception e) {

                                                                }

                                                            }
                                                        });

                                                    }
                                                });

                                            }


                                        }
                                    }, 1500);
                                }

                            } else {

                                Log.e("vvv", "========当前网络没有可连网设备===========");

                            }

                        }
                    });

                    break;

            }
        }

    };


    @Override
    public void onDestroy() {
        super.onDestroy();

        ClosePopselectfamily();
        FscMeshSdk.getApi2().Disconnect();

    }


}
