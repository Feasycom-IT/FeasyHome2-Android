package com.feasycom.feasyhome.ui;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.blankj.utilcode.util.BarUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.adapter.FragAdapter;
import com.feasycom.feasyhome.base.BaseActivity;

import no.nordicsemi.android.mesh.fsdk.FscMeshSdk;

import com.feasycom.feasyhome.logic.FamilyModelFactory;
import com.feasycom.feasyhome.ui.mainfragment.DevFragment;
import com.feasycom.feasyhome.ui.mainfragment.MyFragment;
import com.feasycom.feasyhome.ui.mainfragment.SceneFragment;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


public class MainActivity extends BaseActivity {


    private LinearLayout line_dev, line_scene, line_my;
    private ImageView img_dev, img_scene, img_my;
    private TextView txt_dev, txt_scene, txt_my;

    private ViewPager2 viewpager;
    private ArrayList<Fragment> fragments = new ArrayList<Fragment>();
    private DevFragment devFragment;
    private SceneFragment sceneFragment;
    private MyFragment myFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(MainActivity.this);
        BarUtils.setStatusBarLightMode(MainActivity.this, true);

        setContentView(R.layout.activity_main);


        Log.e("vvvv","========MainActivity=====15========");

        FscMeshSdk.getApi2().Init(MainActivity.this);
        FamilyModelFactory.getInstance().setFamilyModelContext(MainActivity.this);

        line_dev = findViewById(R.id.line_dev);
        line_scene = findViewById(R.id.line_scene);
        line_my = findViewById(R.id.line_my);

        img_dev = findViewById(R.id.img_dev);
        img_scene = findViewById(R.id.img_scene);
        img_my = findViewById(R.id.img_my);

        txt_dev = findViewById(R.id.txt_dev);
        txt_scene = findViewById(R.id.txt_scene);
        txt_my = findViewById(R.id.txt_my);

        viewpager = findViewById(R.id.viewpager);
        devFragment = new DevFragment();
        sceneFragment = new SceneFragment();
        myFragment = new MyFragment();
        fragments.add(devFragment);
        fragments.add(sceneFragment);
        fragments.add(myFragment);


        FragAdapter fragAdapter = new FragAdapter(getSupportFragmentManager(), getLifecycle());
        fragAdapter.setFragments(fragments);
        viewpager.setOffscreenPageLimit(3);
        viewpager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewpager.setAdapter(fragAdapter);
        viewpager.setUserInputEnabled(false);

        line_dev.setOnClickListener(listener);
        line_scene.setOnClickListener(listener);
        line_my.setOnClickListener(listener);


        SelectPage(0);

        //   Log.e("vvv", "=======转换==========" + bytesToHex(intToTwoByteArray(Integer.parseInt("00C0", 16))));


    }


    private Bitmap convert(String base64String) {
        // 解码Base64字符串
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

        // 将字节数组转换为Bitmap图片
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        // 将Bitmap转换为字节数组
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // 将字节数组转换为Base64编码的字符串
        String base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);

        return base64String;
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

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == line_dev) {
                SelectPage(0);
            } else if (view == line_scene) {
                SelectPage(1);
            } else if (view == line_my) {
                SelectPage(2);
            }

        }
    };


    private void SelectPage(int page) {
        viewpager.setCurrentItem(page);

        if (page == 0) {

            img_dev.setBackgroundResource(R.mipmap.tabbar_icon_dev_active);
            txt_dev.setTextColor(getResources().getColor(R.color.txt_blue));

            img_scene.setBackgroundResource(R.mipmap.tabbar_icon_scene_nor);
            txt_scene.setTextColor(getResources().getColor(R.color.txt_none));

            img_my.setBackgroundResource(R.mipmap.tabbar_icon_mine_nor);
            txt_my.setTextColor(getResources().getColor(R.color.txt_none));


        } else if (page == 1) {

            img_dev.setBackgroundResource(R.mipmap.tabbar_icon_dev_nor);
            txt_dev.setTextColor(getResources().getColor(R.color.txt_none));

            img_scene.setBackgroundResource(R.mipmap.tabbar_icon_scene_active);
            txt_scene.setTextColor(getResources().getColor(R.color.txt_blue));

            img_my.setBackgroundResource(R.mipmap.tabbar_icon_mine_nor);
            txt_my.setTextColor(getResources().getColor(R.color.txt_none));

        } else if (page == 2) {

            img_dev.setBackgroundResource(R.mipmap.tabbar_icon_dev_nor);
            txt_dev.setTextColor(getResources().getColor(R.color.txt_none));

            img_scene.setBackgroundResource(R.mipmap.tabbar_icon_scene_nor);
            txt_scene.setTextColor(getResources().getColor(R.color.txt_none));

            img_my.setBackgroundResource(R.mipmap.tabbar_icon_mine_active);
            txt_my.setTextColor(getResources().getColor(R.color.txt_blue));

        }

    }


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }




}