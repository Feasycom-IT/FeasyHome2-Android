package com.feasycom.feasyhome.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.bean.DevTypeBean;
import com.feasycom.feasyhome.bean.RoomBean;
import com.feasycom.feasyhome.callback.ResultCallBack;
import com.feasycom.feasyhome.logic.FamilyModelFactory;
import com.feasycom.feasyhome.ui.dialog.DelDialog;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.feasycom.feasyhome.ui.dialog.TipsDialogOne;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import no.nordicsemi.android.mesh.MeshManagerApi;
import no.nordicsemi.android.mesh.fsdk.Api2;
import no.nordicsemi.android.mesh.fsdk.Api2Impl;
import no.nordicsemi.android.mesh.fsdk.ExtendedBluetoothDevice;
import no.nordicsemi.android.mesh.fsdk.FscMeshSdk;
import no.nordicsemi.android.mesh.fsdk.FscScancallback;
import no.nordicsemi.android.mesh.fsdk.ProvisioningCallback;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class SearchUnprovisionedVoice extends BaseActivity {


    private RelativeLayout rela_back, rela_bg;
    private View view_status;
    private LinearLayout line_search, line_join;
    private TextView txt_dev_tips, txt_research;
    private DevTypeBean devTypeBean;
    private ImageView img_dev_logo;
    private RoomBean roombean;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(SearchUnprovisionedVoice.this);
        BarUtils.setStatusBarLightMode(SearchUnprovisionedVoice.this, true);
        setContentView(R.layout.searchunprovisionedvoice);

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

        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);

        line_search = findViewById(R.id.line_search);
        line_search.setOnClickListener(listener);
        line_join = findViewById(R.id.line_join);
        line_join.setOnClickListener(listener);


    }


    ProvisionedMeshNode mmmeshNode;

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view == rela_back) {

                finish();

            } else if (view == line_search) {
                FscMeshSdk.getApi2().setClearCacheRequired();
                FscMeshSdk.getApi2().Disconnect();
                mextendedBluetoothDevice = null;
                LoadingDialog.getInstance().Show(SearchUnprovisionedVoice.this, getString(R.string.requesting));
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FscMeshSdk.getApi2().StartScan(5000, MeshManagerApi.MESH_PROVISIONING_UUID, (byte) 0x82, (byte) 0x80, fscScancallback);

                    }
                }, 1500);

            } else if (view == txt_research) {
                FscMeshSdk.getApi2().setClearCacheRequired();
                FscMeshSdk.getApi2().Disconnect();
                mextendedBluetoothDevice = null;
                LoadingDialog.getInstance().Show(SearchUnprovisionedVoice.this, getString(R.string.requesting));
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FscMeshSdk.getApi2().StartScan(5000, MeshManagerApi.MESH_PROVISIONING_UUID, (byte) 0x82, (byte) 0x80, fscScancallback);
                        img_dev_logo.setBackgroundResource(R.mipmap.img_devnotfound);
                        txt_dev_tips.setText("正在搜索...");
                    }
                }, 1500);


            } else if (view == line_join) {

                if (null == mextendedBluetoothDevice) {
                    ToastUtils.showLong("请先搜索设备");
                } else {
                    FscMeshSdk.getApi2().CloseAdv();
                    LoadingDialog.getInstance().Show(SearchUnprovisionedVoice.this, getString(R.string.requesting));

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            FscMeshSdk.getApi2().StartProvisionedNode(mextendedBluetoothDevice, new ProvisioningCallback() {
                                @Override
                                public void onCompleted(ProvisionedMeshNode meshNode) {
                                    mmmeshNode = meshNode;

                                    FormBody.Builder formbody = new FormBody.Builder();

                                    formbody.add("mac", mextendedBluetoothDevice.getDevice().getAddress());
                                    formbody.add("device_ver", mextendedBluetoothDevice.getDevice_ver());
                                    formbody.add("family_id", roombean.getFamily_id() + "");
                                    formbody.add("room_id", roombean.getId() + "");
                                    formbody.add("device_type", String.format("%02X", devTypeBean.getDevType()));
                                    formbody.add("second_type", String.format("%02X", devTypeBean.getDevType()) + mextendedBluetoothDevice.getSecondType());
                                    formbody.add("device_id", mextendedBluetoothDevice.getDevice().getAddress());
                                    formbody.add("destination", mmmeshNode.getUnicastAddress() + "");

                                    createDevice(formbody);


                                }

                                @Override
                                public void onFailed(String erro) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {

                                                FscMeshSdk.getApi2().Disconnect();
                                                LoadingDialog.getInstance().Dismiss();
                                                TipsDialogOne tipsDialogOne = new TipsDialogOne(SearchUnprovisionedVoice.this);
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

            }

        }
    };


    private void AssignRoom(String mac, int unicastAddress, ResultCallBack resultCallBack) {

        Map<String, Object> params10 = new HashMap<>();
        params10.put("mac", mac);
        Log.e("vvvv", "=======语音盒子mac==========" + mac);
        RequestOkhttp.getInstance().Get(RequestOkhttp.getInstance().getUrl(Api.FeasyHomeServer + "/api/voice/lightTableList", params10), new Callback() {
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
                Log.e("vvv", "=======语音   灯光组列表===设置房间号===" + str);

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            int room_code = 1;
                            JSONObject jsonObject = JSON.parseObject(str);
                            if (jsonObject.getIntValue("code") == 1) {
                                JSONObject json_data = jsonObject.getJSONObject("data");
                                room_code = json_data.getIntValue("room_code");
                                Log.e("vvvv", "====room_code==============" + room_code);
                                resultCallBack.onSuccess();
                            } else {
                                LoadingDialog.getInstance().Dismiss();
                            }

                            FscMeshSdk.getApi2().SetAssignRoom(unicastAddress, (byte) room_code);

                        } catch (Exception e) {
                            LoadingDialog.getInstance().Dismiss();
                        }

                    }
                });


            }
        });

    }


    private void createDevice(FormBody.Builder formbody) {

        RequestOkhttp.getInstance().PostBody(Api.createVoice, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("vvv", "========添加设备入组失败==========" + e.toString());
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
                Log.e("vvv", "=======添加设备入组===========" + str);
                //{"code":1,"msg":"操作成功","time":1731638946,"data":null}


                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LoadingDialog.getInstance().Dismiss();
                            JSONObject jsonObject = JSON.parseObject(str);

                            if (jsonObject.getIntValue("code") == 1) {

                                AssignRoom(mextendedBluetoothDevice.getDevice().getAddress(), mmmeshNode.getUnicastAddress(), new ResultCallBack() {
                                    @Override
                                    public void onSuccess() {
                                        mextendedBluetoothDevice = null;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                TipsDialogOne tipsDialogOne = new TipsDialogOne(SearchUnprovisionedVoice.this);
                                                tipsDialogOne.show();
                                                tipsDialogOne.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                    @Override
                                                    public void onDismiss(DialogInterface dialogInterface) {
                                                        FscMeshSdk.getApi2().Disconnect();
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
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFail(String erro) {

                                    }
                                });


                                FamilyModelFactory.getInstance().getFamilyModel().getRoomid().setValue(roombean.getId());
                                RequestOkhttp.getInstance().SyncMeshData();
                            }
                        } catch (Exception e) {
                            Log.e("vvvv", "========添加语音盒子==========" + e.toString());
                        }

                    }
                }, 500);


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
                    Log.e("vvv", "    onFscScanResult============" + mextendedBluetoothDevice.getDevice_ver() + " " + mextendedBluetoothDevice.getSecondType());
                    img_dev_logo.setBackgroundResource(R.mipmap.search_img_audio);
                    txt_dev_tips.setText(extendedBluetoothDevice.getAddress());
                    txt_dev_tips.setVisibility(View.VISIBLE);
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

                        img_dev_logo.setBackgroundResource(R.mipmap.img_devnotfound);
                        txt_dev_tips.setText("未找到设备！");

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