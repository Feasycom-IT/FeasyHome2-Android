package com.feasycom.feasyhome.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.BarUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.base.BaseActivity;

public class HelpActivity extends BaseActivity {


    private RelativeLayout rela_back;
    private View view_status;
    private WebView webview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(HelpActivity.this);
        BarUtils.setStatusBarLightMode(HelpActivity.this, true);
        setContentView(R.layout.helpactivity);

        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);


        webview = findViewById(R.id.webview);
        webview.getSettings().setUseWideViewPort(true); // 将图片调整到适合webview的大小
        webview.getSettings().setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webview.getSettings().setSupportZoom(true); // 支持缩放
        webview.getSettings().setBuiltInZoomControls(true); // 支持手势缩放（可选）
      //  webview.setInitialScale(40);
        webview.loadUrl("https://mesh.feasycom.com/index/Article/helpCenter");

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
