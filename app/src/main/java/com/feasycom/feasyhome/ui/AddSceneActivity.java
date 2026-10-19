package com.feasycom.feasyhome.ui;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.adapter.SceneConfigListAdapter;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.bean.SceneBean;
import com.feasycom.feasyhome.callback.ItemClickCallback3;
import com.feasycom.feasyhome.logic.FamilyModelFactory;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.feasycom.feasyhome.ui.dialog.TipsDialogOne;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class AddSceneActivity extends BaseActivity {
    private View view_status;
    private SmartRefreshLayout refreshLayout;
    private RelativeLayout rela_back;
    private RecyclerView recyclerview;
    private TextView txt_select;
    private SceneConfigListAdapter sceneConfigListAdapter;
    private ArrayList<SceneBean> sceneBeans = new ArrayList<>();
    private ArrayList<SceneBean> sceneBeans_temp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(AddSceneActivity.this);
        BarUtils.setStatusBarLightMode(AddSceneActivity.this, true);
        setContentView(R.layout.addsceneactivity);

        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);
        Bundle bundle = getIntent().getExtras();
        sceneBeans_temp = (ArrayList<SceneBean>) bundle.getSerializable("sceneBeans");
        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new GridLayoutManager(AddSceneActivity.this, 2));
        sceneConfigListAdapter = new SceneConfigListAdapter(AddSceneActivity.this, itemClickCallback3);
        recyclerview.setAdapter(sceneConfigListAdapter);
        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                sceneBeans.clear();
                sceneConfigListAdapter.setSceneBeans(sceneBeans);
                getSceneConfigList();

            }
        });

        txt_select = findViewById(R.id.txt_select);
        txt_select.setOnClickListener(listener);
        refreshLayout.autoRefresh();

        SetSpacing();
    }


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view == rela_back) {
                finish();
            } else if (view == txt_select) {
                SceneBean select_sceneBean = null;
                for (int i = 0; i < sceneConfigListAdapter.getSceneBeans().size(); i++) {
                    if (sceneConfigListAdapter.getSceneBeans().get(i).isSelect()) {
                        select_sceneBean = sceneConfigListAdapter.getSceneBeans().get(i);
                        break;
                    }
                }

                if (null == select_sceneBean) {
                    ToastUtils.showLong("没有场景被选中。");
                } else {
                    AddScene(select_sceneBean);
                }

            }
        }
    };


    private void AddScene(SceneBean sceneBean) {
        LoadingDialog.getInstance().Show(AddSceneActivity.this, getString(R.string.requesting));
        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue().getId() + "");
        formbody.add("scene_code", sceneBean.getScene_code());
        RequestOkhttp.getInstance().PostBody(Api.addScene, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                LoadingDialog.getInstance().Dismiss();
                String str = response.body().string();
                Log.e("vvvv", "=====添加场景======" + str);
                //  {"code":1,"msg":"操作成功","time":1738734264,"data":null}
                try {
                    JSONObject jsonObject = JSON.parseObject(str);
                    if (jsonObject.getIntValue("code") == 1) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                TipsDialogOne tipsDialogOne = new TipsDialogOne(AddSceneActivity.this);
                                tipsDialogOne.show();
                                tipsDialogOne.txt_title.setText("添加成功！");
                                tipsDialogOne.txt_cancler.setText("确认");
                                tipsDialogOne.txt_cancler.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        tipsDialogOne.dismiss();
                                        finish();
                                    }
                                });
                            }
                        });
                    }
                } catch (Exception e) {

                }
            }
        });

    }


    ItemClickCallback3 itemClickCallback3 = new ItemClickCallback3() {
        @Override
        public void onItemClick(int p, int type, View view) {

            for (int i = 0; i < sceneConfigListAdapter.getSceneBeans().size(); i++) {
                sceneConfigListAdapter.getSceneBeans().get(i).setSelect(false);
            }
            sceneConfigListAdapter.getSceneBeans().get(p).setSelect(true);
            sceneConfigListAdapter.notifyDataSetChanged();
        }
    };

    private void getSceneConfigList() {

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("family_id", FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue().getId() + "");

        RequestOkhttp.getInstance().PostBody(Api.sceneConfigList, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                refreshLayout.finishRefresh();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = response.body().string();
                Log.e("vvv", "========场景可添加列表========" + str);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            refreshLayout.finishRefresh();
                            JSONObject jsonObject = JSON.parseObject(str);
                            if (jsonObject.getIntValue("code") == 1) {

                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                if (null != jsonArray) {

                                    for (int i = 0; i < jsonArray.size(); i++) {
                                        SceneBean sceneBean = JSON.parseObject(jsonArray.getString(i), SceneBean.class);

                                   /*     Optional<SceneBean> foundAuracastBean = sceneBeans_temp.parallelStream()
                                                .filter(auracastBean1 -> auracastBean1.getScene_code().equals(sceneBean.getScene_code()))
                                                .findFirst();
                                        if (!foundAuracastBean.isPresent()) {
                                            sceneBeans.add(sceneBean);
                                        }*/

                                        sceneBeans.add(sceneBean);

                                    }

                                    sceneConfigListAdapter.setSceneBeans(sceneBeans);
                                }

                            }
                        } catch (Exception e) {

                        }

                    }
                });


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
