package com.feasycom.feasyhome.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.base.BaseActivity;

import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends BaseActivity {


    private TextView txt_ver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(StartActivity.this);
        BarUtils.setStatusBarLightMode(StartActivity.this, true);

        setContentView(R.layout.startactivity);

        ToastUtils.getDefaultMaker().setBgColor(getColor(R.color.white));
        ToastUtils.getDefaultMaker().setBgResource(R.drawable.shap_white_circular);
        ToastUtils.getDefaultMaker().setGravity(Gravity.BOTTOM, 0, ConvertUtils.dp2px(200));

        txt_ver = findViewById(R.id.txt_ver);
        txt_ver.setText("ver: " + AppUtils.getAppVersionName());


        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {


                if (TextUtils.isEmpty(SPUtils.getInstance().getString("userinfo", ""))) {

                    Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                    startActivity(intent);

                } else {

                    Intent intent = new Intent(StartActivity.this, MainActivity.class);
                    startActivity(intent);

                }
                finish();
            }
        }, 1000);

    }



}
