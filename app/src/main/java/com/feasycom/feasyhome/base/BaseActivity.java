package com.feasycom.feasyhome.base;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ToastUtils;
import com.lky.toucheffectsmodule.factory.TouchEffectsFactory;


public class BaseActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        TouchEffectsFactory.initTouchEffects(this);
        super.onCreate(savedInstanceState);


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        ToastUtils.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }
}
