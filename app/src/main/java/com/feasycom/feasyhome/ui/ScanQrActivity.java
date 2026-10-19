package com.feasycom.feasyhome.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.annotation.Nullable;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.base.BaseActivity;
import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.mylhyl.zxing.scanner.OnScannerCompletionListener;
import com.mylhyl.zxing.scanner.ScannerView;

public class ScanQrActivity extends BaseActivity {

    private View view_status;
    private RelativeLayout rela_back;
    private ScannerView scanner_view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(ScanQrActivity.this);
        BarUtils.setStatusBarLightMode(ScanQrActivity.this, true);
        setContentView(R.layout.scanqractivity);

        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);
        scanner_view = findViewById(R.id.scanner_view);
        scanner_view.setOnScannerCompletionListener(new OnScannerCompletionListener() {
            @Override
            public void onScannerCompletion(Result rawResult, ParsedResult parsedResult, Bitmap barcode) {
                String str_data = rawResult.getText();
                Log.e("vvvv", "===========扫码结果========" + str_data);
                if (str_data.indexOf("share_id") == -1) {
                    ToastUtils.showShort("请重新扫描！");
                    scanner_view.restartPreviewAfterDelay(1000);
                } else {
                    Intent intent = new Intent(ScanQrActivity.this, JoinFamilyActivity.class);
                    intent.putExtra("str_data", str_data);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == rela_back) {
                finish();
            }

        }
    };


    @Override
    protected void onResume() {
        scanner_view.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        scanner_view.onPause();
        super.onPause();
    }

}
