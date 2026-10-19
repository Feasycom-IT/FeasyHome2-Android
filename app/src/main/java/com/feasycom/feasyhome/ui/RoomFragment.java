package com.feasycom.feasyhome.ui;


import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.adapter.IndexGroupAdapter;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseFragment;
import com.feasycom.feasyhome.bean.DevGroupBean;
import com.feasycom.feasyhome.bean.RoomBean;
import com.feasycom.feasyhome.callback.ItemClickCallback2;
import com.feasycom.feasyhome.logic.FamilyModelFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RoomFragment extends BaseFragment implements ItemClickCallback2 {


    private RoomBean roomBean;


    public RoomFragment() {

    }

    public RoomFragment(RoomBean roomBean) {
        this.roomBean = roomBean;
    }


    private RecyclerView recyclerview;
    private IndexGroupAdapter indexGroupAdapter;
    private LinearLayout line_null;
    private TextView txt_zw;


    public RoomBean getRoomBean() {
        return roomBean;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homefragment, container, false);

        line_null = view.findViewById(R.id.line_null);
        recyclerview = view.findViewById(R.id.recyclerview);
        txt_zw = view.findViewById(R.id.txt_zw);
        recyclerview.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        indexGroupAdapter = new IndexGroupAdapter(getActivity(), RoomFragment.this);
        recyclerview.setAdapter(indexGroupAdapter);
        //   Log.e("vvvv", "=======房间ID====2=======" + roomBean.getId());
        FamilyModelFactory.getInstance().getFamilyModel().getRoomid().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                Log.e("vvvv", "========房间更新组===1======" + integer + "      " + roomBean.getId());
                if (integer == roomBean.getId() || integer == 999) {
                    GetIndexData();
                }


            }
        });
        SetSpacing();
    /*    if (null != roomBean) {
            txt_zw.setText(roomBean.getRoom_name() + "   " + roomBean.getId());
        }*/
        GetIndexData();

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();


    }


    private void GetIndexData() {

        if (null == roomBean) {
            Log.e("vvvv", "=======================roomBean   =    null  =========================================");
            return;
        }
        //    txt_zw.setText(roomBean.getId() + "");
        Map<String, Object> params10 = new HashMap<>();

        params10.put("family_id", roomBean.getFamily_id() + "");
        params10.put("room_id", roomBean.getId() + "");

        RequestOkhttp.getInstance().Get(RequestOkhttp.getInstance().getUrl(Api.IndexPageDate, params10), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("vvvv", "=====GetIndexData======" + e.toString());


            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str_res = response.body().string();
                //{"code":1,"msg":"返回成功","time":1723196934,"data":{"familyList":[],"roomList":[],"lightGroupList":[],"curtainGroupList":[],"panelList":[],"voiceList":[]}}
             //   Log.e("vvv", "=======首页房间群组数据==========" + roomBean.getId() + "   " + str_res);
                try {

                    JSONObject jsonObject = JSON.parseObject(str_res);
                    if (jsonObject.getIntValue("code") == 1) {

                        JSONObject json_data = JSON.parseObject(jsonObject.getString("data"));

                        //有设备群组数据

                        List<DevGroupBean> devGroupBeans = new ArrayList<>();
                        JSONArray lightgrouparray = json_data.getJSONArray("lightGroupList");
                        JSONArray curtaingrouparray = json_data.getJSONArray("curtainGroupList");
                        JSONArray panelarray = json_data.getJSONArray("panelList");
                        JSONArray voicearray = json_data.getJSONArray("voiceList");
                        if (lightgrouparray.size() > 0) {

                            for (int i = 0; i < lightgrouparray.size(); i++) {
                                DevGroupBean devGroupBean = JSON.parseObject(lightgrouparray.getString(i), DevGroupBean.class);
                                devGroupBeans.add(devGroupBean);
                            }

                        }

                        if (curtaingrouparray.size() > 0) {

                            for (int i = 0; i < curtaingrouparray.size(); i++) {
                                DevGroupBean devGroupBean = JSON.parseObject(curtaingrouparray.getString(i), DevGroupBean.class);
                                devGroupBeans.add(devGroupBean);
                            }

                        }

                        if (panelarray.size() > 0) {

                            for (int i = 0; i < panelarray.size(); i++) {
                                DevGroupBean devGroupBean = JSON.parseObject(panelarray.getString(i), DevGroupBean.class);
                                devGroupBeans.add(devGroupBean);
                            }

                        }

                        if (voicearray.size() > 0) {
                            for (int i = 0; i < voicearray.size(); i++) {
                                DevGroupBean devGroupBean = JSON.parseObject(voicearray.getString(i), DevGroupBean.class);
                                devGroupBeans.add(devGroupBean);
                            }

                        }


                        //        Log.e("vvvv", "=======lightGroupBeans   size=======" + lightGroupBeans.size());


                        if (null != getActivity()) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if (devGroupBeans.size() <= 0) {
                                        line_null.setVisibility(View.VISIBLE);
                                        recyclerview.setVisibility(View.GONE);
                                    } else {
                                        line_null.setVisibility(View.GONE);
                                        recyclerview.setVisibility(View.VISIBLE);
                                        indexGroupAdapter.setLightGroupBeans(devGroupBeans);
                                        Log.e("vvv", "=====更新后的房间群组数======" + roomBean.getId() + "     " + devGroupBeans.size());

                                    }

                                }
                            });
                        } else {
                            Log.e("vvvvv", "===============room========getActivity() = null  ===============");

                        }


                    }


                } catch (Exception e) {

                    Log.e("vvvvv", "=============GetIndexData===========" + e.toString());

                }

            }
        });


    }


    @Override
    public void onItemClick(int p, Object obj) {
        //  if (FscMeshSdk.getApi2().isConnect()){
        try {
            if (true) {
                DevGroupBean devGroupBean = indexGroupAdapter.getLightGroupBeans().get(p);
                Log.e("vvv", "===========选中===========" + devGroupBean.getRoom_id() + "     " + p + "     " + devGroupBean.getGroup_name());

                if (null != devGroupBean && devGroupBean.getDevice_type().equals("01")) {

                    Intent intent = new Intent(getActivity(), GroupLightDetailsActivity.class);
                    intent.putExtra("devGroupBean", devGroupBean);
                    getActivity().startActivity(intent);

                } else if (null != devGroupBean && devGroupBean.getDevice_type().equals("05")) {

                    Intent intent = new Intent(getActivity(), GroupCurtainDetailsActivity.class);
                    intent.putExtra("devGroupBean", devGroupBean);
                    getActivity().startActivity(intent);

                } else if (null != devGroupBean && devGroupBean.getDevice_type().equals("02")) {

                    Intent intent = new Intent(getActivity(), GroupPanelDetailsActivity.class);
                    intent.putExtra("devGroupBean", devGroupBean);
                    intent.putExtra("roombean", roomBean);
                    getActivity().startActivity(intent);

                } else if (null != devGroupBean && devGroupBean.getDevice_type().equals("04")) {

                    Intent intent = new Intent(getActivity(), GroupVoiceDetailsActivity.class);
                    intent.putExtra("devGroupBean", devGroupBean);
                    getActivity().startActivity(intent);

                }

            } else {
                ToastUtils.showLong("请先连接网络！");
            }

        } catch (Exception e) {
            Log.e("vvvv", "======跳转异常=====" + e.toString());
        }


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
     //   Log.e("vvvv", "======onDestroy====room====");
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
