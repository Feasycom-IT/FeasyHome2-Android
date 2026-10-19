package com.feasycom.feasyhome.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.feasycom.feasyhome.R;

public class LoadingDialog {


    private static volatile LoadingDialog instance;


    public static LoadingDialog getInstance() {
        if (instance == null) {
            synchronized (LoadingDialog.class) {
                if (instance == null) {
                    instance = new LoadingDialog();
                }
            }
        }
        return instance;
    }


    private Ldialog ldialog = null;


    public void Show(Context context, String tips) {

        try {
            if (null != ldialog && ldialog.isShowing()) {

            } else {

                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    if (!activity.isFinishing() && !activity.isDestroyed()) {
                        // Context 可用，可以执行操作
                        ldialog = new Ldialog(context);
                        ldialog.show();
                        ldialog.SetTips(tips);
                    } else {
                        // Context 不可用，避免操作
                    }
                }


            }

        } catch (Exception e) {

        }

    }


    public void Dismiss() {
        try {
            if (null != ldialog && ldialog.isShowing()) {
                ldialog.dismiss();
            }
        } catch (Exception e) {

        }


    }

    public boolean isShowing() {
        if (null == ldialog) {
            return false;
        }
        return ldialog.isShowing();
    }


    private class Ldialog extends Dialog {

        public Ldialog(@NonNull Context context) {
            super(context);
        }


        private TextView txt_pro_tips;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.loadingdialog);
            if (getWindow() != null) {
                getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            setCanceledOnTouchOutside(false);
            setCancelable(false);
            txt_pro_tips = findViewById(R.id.txt_pro_tips);

        }


        public void SetTips(String str) {
            if (TextUtils.isEmpty(str)) {
                txt_pro_tips.setVisibility(View.GONE);
            } else {
                txt_pro_tips.setVisibility(View.VISIBLE);
                txt_pro_tips.setText(str);
            }
        }
    }


}
