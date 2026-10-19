package com.feasycom.feasyhome.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.BarUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.base.BaseActivity;

public class CreateRoomActivity extends BaseActivity {


    private RelativeLayout rela_back;
    private View view_status;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(CreateRoomActivity.this);
        BarUtils.setStatusBarLightMode(CreateRoomActivity.this, true);

        setContentView(R.layout.createroomactivity);

        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);


        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);

    }


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == rela_back) {
                finish();
            }

        }
    };


}
