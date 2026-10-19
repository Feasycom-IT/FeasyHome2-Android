package com.feasycom.feasyhome.ui;

import android.content.DialogInterface;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.bean.FamilyBean;

import com.feasycom.feasyhome.logic.FamilyModelFactory;
import com.feasycom.feasyhome.ui.dialog.DelDialog;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.feasycom.feasyhome.ui.dialog.SingleInputDialog;
import com.feasycom.feasyhome.ui.dialog.TipsDialogOne;

import java.io.IOException;
import java.util.List;

import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.fsdk.FscMeshNetworkCallback;
import no.nordicsemi.android.mesh.fsdk.FscMeshSdk;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class FamilyDetailsActivity extends BaseActivity {


    private View view_status;

    private RelativeLayout rela_back, rela_member, rela_room, rela_disband;
    private FamilyBean familyBean;
    private TextView txt_title, txt_room_nub, member_nub, txt_del, txt_edit;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BarUtils.transparentStatusBar(FamilyDetailsActivity.this);
        BarUtils.setStatusBarLightMode(FamilyDetailsActivity.this, true);

        setContentView(R.layout.familydetailsactivity);

        familyBean = (FamilyBean) getIntent().getSerializableExtra("familyBean");

        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);

        rela_member = findViewById(R.id.rela_member);
        rela_member.setOnClickListener(listener);

        rela_room = findViewById(R.id.rela_room);
        rela_room.setOnClickListener(listener);
        rela_disband = findViewById(R.id.rela_disband);
        rela_disband.setOnClickListener(listener);

        txt_title = findViewById(R.id.txt_title);
        txt_title.setText(familyBean.getFamily_name());

        txt_room_nub = findViewById(R.id.txt_room_nub);
        member_nub = findViewById(R.id.member_nub);

        txt_del = findViewById(R.id.txt_del);
        txt_del.setOnClickListener(listener);

        txt_edit = findViewById(R.id.txt_edit);
        txt_edit.setOnClickListener(listener);

    }


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == rela_back) {
                finish();
            } else if (view == rela_member) {
                Intent intent = new Intent(FamilyDetailsActivity.this, MemberManageActivity.class);
                intent.putExtra("familyBean", familyBean);
                intent.putExtra("str_member_list", str_member_list);
                startActivity(intent);
            } else if (view == rela_room) {
                Intent intent = new Intent(FamilyDetailsActivity.this, RoomManageActivity.class);
                intent.putExtra("familyBean", familyBean);
                intent.putExtra("str_room_list", str_room_list);
                startActivity(intent);

            } else if (view == txt_del) {

                DelDialog delDialog = new DelDialog(FamilyDetailsActivity.this);
                delDialog.show();
                delDialog.txt_title.setText(getString(R.string.tips23) + familyBean.getFamily_name());
                delDialog.txt_cancler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        delDialog.dismiss();
                    }
                });

                delDialog.txt_del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        DelFamily();
                        delDialog.dismiss();

                    }
                });

            } else if (view == txt_edit) {

                SingleInputDialog singleInputDialog = new SingleInputDialog(FamilyDetailsActivity.this);
                singleInputDialog.show();
                singleInputDialog.edit_name.setHint(getString(R.string.tips18));
                singleInputDialog.txt_title.setText(getString(R.string.tips24));
                singleInputDialog.txt_cancler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        singleInputDialog.dismiss();
                    }
                });
                singleInputDialog.txt_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        boolean exists = FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue().stream()
                                .anyMatch(bean -> bean.getFamily_name().equals(singleInputDialog.edit_name.getText().toString()));

                        if (TextUtils.isEmpty(singleInputDialog.edit_name.getText().toString().trim())) {
                            ToastUtils.showShort(getString(R.string.tips18));

                        } else if (singleInputDialog.edit_name.getText().toString().trim().length() > 12) {
                            ToastUtils.showShort(getString(R.string.tips20));
                        } else if (exists) {
                            ToastUtils.showShort(getString(R.string.tips21));
                        } else {
                            EditFamilyName(singleInputDialog.edit_name.getText().toString().trim());
                            singleInputDialog.dismiss();
                        }


                    }
                });


            } else if (view == rela_disband) {

                if (null != FscMeshSdk.getApi2().getMeshNetwork() && FscMeshSdk.getApi2().getMeshNetwork().getMeshUUID().equals(familyBean.getNetwork_id())) {
                    SingleInputDialog delDialog = new SingleInputDialog(FamilyDetailsActivity.this);
                    delDialog.show();
                    delDialog.txt_title.setText(getString(R.string.tips25));
                    delDialog.edit_name.setHint(getString(R.string.tips26));
                    delDialog.txt_cancler.setText(R.string.cancler);
                    delDialog.txt_ok.setText(R.string.determine);
                    delDialog.txt_cancler.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            delDialog.dismiss();
                        }
                    });
                    delDialog.txt_ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (delDialog.edit_name.getText().toString().equals("111111")) {
                                delDialog.dismiss();
                                DisbandNetwork();
                            } else {
                                ToastUtils.showShort(R.string.tips27);
                            }

                        }
                    });
                } else {
                    ToastUtils.showLong(R.string.tips28);
                }


            }

        }
    };


    private void DisbandNetwork() {
        LoadingDialog.getInstance().Show(FamilyDetailsActivity.this, getString(R.string.requesting));
        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", familyBean.getId() + "");
        formbody.add("password", "111111");
        RequestOkhttp.getInstance().PostBody(Api.DissolveNetwork, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String str = response.body().string();
                Log.e("vvv", "=====解散全网=====" + str);
                //   {"code":1,"msg":"解散成功","time":1739154261,"data":null}

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            JSONObject jsonObject = JSON.parseObject(str);
                            if (jsonObject.getIntValue("code") == 1) {
                                FscMeshSdk.getApi2().ResetAllNode();

                                List<ProvisionedMeshNode> nodes = FscMeshSdk.getApi2().getMeshNetwork().getNodes();
                                if (!nodes.isEmpty()) {
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.e("vvvv", "===删除节点===" + nodes.size());
                                            for (int i = 0; i < nodes.size(); i++) {
                                                if (nodes.get(i).getNodeName().indexOf("Provisioner") == -1) {
                                                    Log.e("vvvv", "===删除节点name===" + nodes.get(i).getNodeName());
                                                    FscMeshSdk.getApi2().deleteNode(nodes.get(i));

                                                }
                                            }
                                            FamilyModelFactory.getInstance().getFamilyModel().getRoomid().setValue(999);
                                            TipsDialogOne tipsDialogOne = new TipsDialogOne(FamilyDetailsActivity.this);
                                            tipsDialogOne.show();
                                            tipsDialogOne.txt_title.setText("解散全网完成！");
                                            tipsDialogOne.txt_cancler.setText("返回");
                                            tipsDialogOne.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                @Override
                                                public void onDismiss(DialogInterface dialogInterface) {
                                                    FscMeshSdk.getApi2().Disconnect();
                                                    RequestOkhttp.getInstance().SyncMeshData();
                                                }
                                            });

                                            tipsDialogOne.txt_cancler.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    tipsDialogOne.dismiss();
                                                }
                                            });


                                        }
                                    }, 2500);

                                }

                            }
                            ToastUtils.showLong(jsonObject.getString("msg"));
                        } catch (Exception e) {

                        }

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                LoadingDialog.getInstance().Dismiss();
                            }
                        }, 2500);

                    }
                });
            }
        });

    }


    private void EditFamilyName(String name) {
        LoadingDialog.getInstance().Show(FamilyDetailsActivity.this, getString(R.string.requesting));
        FormBody.Builder formbody = new FormBody.Builder();

        formbody.add("family_id", familyBean.getId() + "");
        formbody.add("family_name", name);
        RequestOkhttp.getInstance().PostBody(Api.EditFamilyName, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
                ToastUtils.showShort("修改失败1");

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                LoadingDialog.getInstance().Dismiss();
                String str_res = response.body().string();

                // {"code":1,"msg":"修改成功","time":1741154469,"data":null}

                Log.e("vvvv", "===========修改家庭========" + str_res);
                try {

                    JSONObject jsonObject = JSON.parseObject(str_res);
                    if (jsonObject.getIntValue("code") == 1) {

                        familyBean.setFamily_name(name);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txt_title.setText(name);
                                for (int i = 0; i < FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue().size(); i++) {
                                    if (FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue().get(i).getNetwork_id().equals(familyBean.getNetwork_id())) {
                                        FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue().get(i).setFamily_name(name);
                                    }

                                }

                            }
                        });

                        FscMeshSdk.getApi2().QueryMeshNetwork(familyBean.getNetwork_id(), new FscMeshNetworkCallback() {
                            @Override
                            public void onMeshNetwork(MeshNetwork meshNetwork) {
                                meshNetwork.setMeshName(name);
                                FscMeshSdk.getApi2().UpMeshnetwork(meshNetwork);
                            }
                        });


                    } else {

                    }
                    ToastUtils.showShort(jsonObject.getString("msg"));
                } catch (Exception e) {

                }

            }
        });


    }


    private void DelFamily() {

        LoadingDialog.getInstance().Show(FamilyDetailsActivity.this, getString(R.string.requesting));
        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", familyBean.getId() + "");
        //   formbody.add("password", SPUtils.getInstance().getString("pwd"));

        RequestOkhttp.getInstance().PostBody(Api.DeleteFamily, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LoadingDialog.getInstance().Dismiss();
                        ToastUtils.showShort("删除失败1");
                    }
                });

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                LoadingDialog.getInstance().Dismiss();
                String str_res = response.body().string();
                Log.e("vvvv", "======删除家庭=======" + str_res);
                //   {"code":0,"msg":"无管理权限","time":1724125740,"data":null}
                //   {"code":1,"msg":"删除成功","time":1724133095,"data":null}
                try {

                    JSONObject jsonObject = JSON.parseObject(str_res);
                    if (jsonObject.getIntValue("code") == 1) {

                        FscMeshSdk.getApi2().QueryMeshNetwork(familyBean.getNetwork_id(), new FscMeshNetworkCallback() {
                            @Override
                            public void onMeshNetwork(MeshNetwork meshNetwork) {

                                if (null != meshNetwork) {

                                    FscMeshSdk.getApi2().deleteMeshNetworkFromDb(meshNetwork);

                                }

                            }
                        });
                        for (int i = 0; i < FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue().size(); i++) {
                            if (FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue().get(i).getNetwork_id().equals(familyBean.getNetwork_id())) {
                                FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue().remove(i);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue().size() <= 0) {
                                            FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().postValue(null);
                                        } else {
                                            FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().postValue(FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue());
                                        }

                                    }
                                });
                            }

                        }
                        finish();
                    } else {
                        ToastUtils.showShort(jsonObject.getString("msg"));
                    }


                } catch (Exception e) {

                }

            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();

        GetRoomList();
        GetMemberList();

    }

    private void GetRoomList() {

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", familyBean.getId() + "");
        RequestOkhttp.getInstance().PostBody(Api.RoomList, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str_res = response.body().string();
                Log.e("vvvv", "======房间列表=======" + str_res);
                try {

                    JSONObject jsonObject = JSON.parseObject(str_res);
                    if (jsonObject.getIntValue("code") == 1) {
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        str_room_list = jsonArray.toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txt_room_nub.setText(jsonArray.size() + getString(R.string.room));
                            }
                        });


                    }
                } catch (Exception e) {

                }
            }
        });


    }


    private String str_member_list = "";
    private String str_room_list = "";

    private void GetMemberList() {

        FormBody.Builder formbody = new FormBody.Builder();

        formbody.add("family_id", familyBean.getId() + "");
        RequestOkhttp.getInstance().PostBody(Api.MemberList, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str_res = response.body().string();
                Log.e("vvvv", "======成员列表=======" + str_res);
                try {
                    JSONObject jsonObject = JSON.parseObject(str_res);
                    if (jsonObject.getIntValue("code") == 1) {
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        str_member_list = jsonArray.toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                member_nub.setText(jsonArray.size() + getString(R.string.member));
                            }
                        });


                    }
                } catch (Exception e) {

                }

            }
        });


    }


    private Handler handler = new Handler();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
