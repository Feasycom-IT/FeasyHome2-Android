package com.feasycom.feasyhome.ui.mainfragment;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.adapter.SceneListAdapter;
import com.feasycom.feasyhome.adapter.SelectFamilyAdapter;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseFragment;
import com.feasycom.feasyhome.bean.FamilyBean;
import com.feasycom.feasyhome.bean.RoomBean;
import com.feasycom.feasyhome.bean.SceneBean;
import com.feasycom.feasyhome.callback.ItemClickCallback;
import com.feasycom.feasyhome.callback.ItemClickCallback3;
import com.feasycom.feasyhome.logic.FamilyModelFactory;
import com.feasycom.feasyhome.model.FamilyModel;
import com.feasycom.feasyhome.ui.AddSceneActivity;
import com.feasycom.feasyhome.ui.EditSceneActivity;
import com.feasycom.feasyhome.ui.FamilyListActivity;
import com.feasycom.feasyhome.ui.MainActivity;
import com.feasycom.feasyhome.ui.RoomFragment;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.feasycom.feasyhome.utils.MaxHeightLinearLayoutManager;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.mesh.fsdk.FscMeshSdk;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class SceneFragment extends BaseFragment {


    private View view_status;
    private LinearLayout line_family_select, line_tips;
    private TextView txt_family_name, txt_tips_name;
    private RelativeLayout rela_join;
    private SmartRefreshLayout refreshLayout;
    private RecyclerView recyclerview;
    private SceneListAdapter sceneListAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scenefragment, container, false);

        view_status = view.findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        line_family_select = view.findViewById(R.id.line_family_select);
        line_family_select.setOnClickListener(listener);
        line_tips = view.findViewById(R.id.line_tips);
        txt_tips_name = view.findViewById(R.id.txt_tips_name);
        txt_family_name = view.findViewById(R.id.txt_family_name);
        rela_join = view.findViewById(R.id.rela_join);
        rela_join.setOnClickListener(listener);
        recyclerview = view.findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        refreshLayout = view.findViewById(R.id.refreshLayout);
        sceneListAdapter = new SceneListAdapter(getActivity(), itemClickCallback3);
        recyclerview.setAdapter(sceneListAdapter);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

                getSceneList();
            }
        });


        SetSpacing();
        ObserverCurrentFamilyBean();

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!refreshLayout.isRefreshing()) {
            refreshLayout.autoRefresh();
        }
    }

    private void ObserverCurrentFamilyBean() {
        FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().observe(getViewLifecycleOwner(), new Observer<FamilyBean>() {
            @Override
            public void onChanged(FamilyBean familyBean) {

                if (null != familyBean) {
                    sceneBeans.clear();
                    sceneListAdapter.setSceneBeans(sceneBeans);
                    txt_family_name.setText(familyBean.getFamily_name());
                    if (!refreshLayout.isRefreshing()) {
                        refreshLayout.autoRefresh();
                    }
                } else {
                    txt_family_name.setText(getString(R.string.createafamily));
                    sceneBeans.clear();
                    sceneListAdapter.setSceneBeans(sceneBeans);
                }

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

                if (null == FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue() || FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue().size() <= 0) {
                    //去创建房间
                    ToastUtils.showLong(getString(R.string.tips9));
                }else {
                    Intent intent = new Intent(getActivity(), AddSceneActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("sceneBeans", sceneListAdapter.getSceneBeans());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }

            }

        }
    };

    private ArrayList<SceneBean> sceneBeans = new ArrayList<>();

    private void getSceneList() {

        if (null == FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue() || FamilyModelFactory.getInstance().getFamilyModel().getFamilyBeans().getValue().size() <= 0) {
            refreshLayout.finishRefresh();
            return;
        }

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue().getId() + "");
        Log.e("vvvv","======getSceneList============"+Api.sceneList);
        RequestOkhttp.getInstance().PostBody(Api.sceneList, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                refreshLayout.finishRefresh();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = response.body().string();
             //   Log.e("vvv", "======场景列表======" + str);
                try {

                    JSONObject jsonObject = JSON.parseObject(str);
                    if (jsonObject.getIntValue("code") == 1) {
                        sceneBeans.clear();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                JSONObject json_data = jsonObject.getJSONObject("data");
                                JSONArray scene_jsonarray = json_data.getJSONArray("sceneList");
                                JSONObject nowscene_json = json_data.getJSONObject("nowScene");
                                String nowscene_code = "-1";
                                if (null != nowscene_json) {
                                    nowscene_code = nowscene_json.getString("scene_code");
                                }
                                if (scene_jsonarray.size() > 0) {
                                    for (int i = 0; i < scene_jsonarray.size(); i++) {
                                        SceneBean sceneBean = JSON.parseObject(scene_jsonarray.getString(i), SceneBean.class);
                                        if (null != sceneBean) {
                                            if (sceneBean.getScene_code().equals(nowscene_code)) {
                                                sceneBean.setSelect(true);
                                            }
                                            sceneBeans.add(sceneBean);
                                        }
                                    }
                                }
                                sceneListAdapter.setSceneBeans(sceneBeans);
                                if (sceneBeans.size() > 0) {
                                    line_tips.setVisibility(View.INVISIBLE);
                                    for (int i = 0; i < sceneBeans.size(); i++) {
                                        if (sceneBeans.get(i).isSelect()) {
                                            line_tips.setVisibility(View.VISIBLE);
                                            txt_tips_name.setText(sceneBeans.get(i).getScene_name());
                                        }
                                    }
                                } else {
                                    line_tips.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }

                    refreshLayout.finishRefresh();

                } catch (Exception e) {

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


    private PopupWindow popsceneedit = null;

    private void ShowPopSceneEdit(View parent, int p) {


        View pop_view = LayoutInflater.from(getContext()).inflate(R.layout.pop_scene_edit, null);
        LinearLayout line_edit = pop_view.findViewById(R.id.line_edit);
        LinearLayout line_del = pop_view.findViewById(R.id.line_del);
        line_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popsceneedit.dismiss();
                Intent intent = new Intent(getActivity(), EditSceneActivity.class);
                intent.putExtra("select_sceneBean", sceneBeans.get(p));
                startActivity(intent);
            }
        });

        line_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popsceneedit.dismiss();
                DelScene(sceneBeans.get(p));
            }
        });

        popsceneedit = new PopupWindow(pop_view, ConvertUtils.dp2px(140), ConvertUtils.dp2px(112));
        popsceneedit.setElevation(3);
        popsceneedit.setOutsideTouchable(true);
        popsceneedit.showAsDropDown(parent, -ConvertUtils.dp2px(115), ConvertUtils.dp2px(0));
        //    Log.e("vvvv", "-==========切换家庭==========");


    }


    ItemClickCallback3 itemClickCallback3 = new ItemClickCallback3() {
        @Override
        public void onItemClick(int p, int type, View view) {

            if (type == 2) {
                ShowPopSceneEdit(view, p);
            } else if (type == 1) {
                NowScene(p);
            }

        }
    };


    private void NowScene(int p) {
        LoadingDialog.getInstance().Show(getActivity(), getString(R.string.setting)+"...");
        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue().getId() + "");
        formbody.add("id", sceneBeans.get(p).getId() + "");
        RequestOkhttp.getInstance().PostBody(Api.nowScene, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String str = response.body().string();
                //{"code":1,"msg":"操作成功","time":1738721667,"data":null}
                Log.e("vvvv", "========切换场景========" + str);
                try {
                    JSONObject jsonObject = JSON.parseObject(str);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (jsonObject.getIntValue("code") == 1) {
                                FscMeshSdk.getApi2().ControlSceneOnOff(Integer.parseInt(sceneBeans.get(p).getScene_code(),16), (byte) 0x01);

                                for (int i = 0; i < sceneBeans.size(); i++) {
                                    sceneBeans.get(i).setSelect(false);
                                }
                                sceneBeans.get(p).setSelect(true);
                                txt_tips_name.setText(sceneBeans.get(p).getScene_name());
                                line_tips.setVisibility(View.VISIBLE);
                                sceneListAdapter.setSceneBeans(sceneBeans);
                            }

                            ToastUtils.showLong(jsonObject.getString("msg"));

                        }
                    });

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LoadingDialog.getInstance().Dismiss();
                        }
                    }, 600);
                } catch (Exception e) {

                }

            }
        });


    }


    private void DelScene(SceneBean sceneBean) {
        LoadingDialog.getInstance().Show(getActivity(), getString(R.string.deleting)+"...");
        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue().getId() + "");
        formbody.add("scene_code", sceneBean.getScene_code());
        RequestOkhttp.getInstance().PostBody(Api.deleteScene, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                LoadingDialog.getInstance().Dismiss();
                String str = response.body().string();
                Log.e("vvv", "========删除场景=======" + str);
                //{"code":1,"msg":"删除成功","time":1738746750,"data":null}
                try {
                    JSONObject jsonObject = JSON.parseObject(str);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (jsonObject.getIntValue("code") == 1) {
                                FscMeshSdk.getApi2().DelScene(Integer.parseInt(sceneBean.getScene_code(),16));
                                refreshLayout.autoRefresh();
                            }

                            ToastUtils.showShort(jsonObject.getString("msg"));

                        }
                    });


                } catch (Exception e) {

                }

            }
        });

    }


    Handler handler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);


        }
    };


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


}
