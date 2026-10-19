package com.feasycom.feasyhome.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.blankj.utilcode.util.BarUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.adapter.MemberListAdapter;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.bean.FamilyBean;
import com.feasycom.feasyhome.bean.MemberBean;

import java.util.ArrayList;
import java.util.List;

public class MemberManageActivity extends BaseActivity {


    private RelativeLayout rela_back;
    private View view_status;
    private RecyclerView recyclerview;
    private MemberListAdapter memberListAdapter;
    private String str_member_list;
    private List<MemberBean> memberBeans = new ArrayList<>();
    private TextView txt_invate;
    private FamilyBean familyBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BarUtils.transparentStatusBar(MemberManageActivity.this);
        BarUtils.setStatusBarLightMode(MemberManageActivity.this, true);

        setContentView(R.layout.membermanageactivity);
        str_member_list = getIntent().getStringExtra("str_member_list");
        familyBean = (FamilyBean) getIntent().getSerializableExtra("familyBean");

        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);


        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);
        txt_invate = findViewById(R.id.txt_invate);
        txt_invate.setOnClickListener(listener);

        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(MemberManageActivity.this));
        memberListAdapter = new MemberListAdapter(MemberManageActivity.this);
        recyclerview.setAdapter(memberListAdapter);
        JSONArray jsonArray = JSON.parseArray(str_member_list);
        if (null != jsonArray) {
            for (int i = 0; i < jsonArray.size(); i++) {
                MemberBean memberBean = JSON.parseObject(jsonArray.getString(i), MemberBean.class);
                if (null != memberBean) {
                    memberBeans.add(memberBean);
                }
            }
            memberListAdapter.setMemberBeans(memberBeans);
        }


    }


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view == rela_back) {
                finish();
            } else if (view == txt_invate) {
                Intent intent = new Intent(MemberManageActivity.this, ShareQrcodeActivity.class);
                intent.putExtra("familyBean", familyBean);
                startActivity(intent);
            }


        }
    };


}
