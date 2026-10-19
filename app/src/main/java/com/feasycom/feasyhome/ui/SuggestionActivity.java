package com.feasycom.feasyhome.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.feasycom.feasyhome.ui.dialog.TipsDialogOne;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class SuggestionActivity extends BaseActivity {


    private RelativeLayout rela_back;
    private View view_status;
    private EditText edit_1, edit_2;
    private TextView txt_conmit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(SuggestionActivity.this);
        BarUtils.setStatusBarLightMode(SuggestionActivity.this, true);
        setContentView(R.layout.suggestionactivity);

        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);
        edit_1 = findViewById(R.id.edit_1);
        edit_2 = findViewById(R.id.edit_2);
        txt_conmit = findViewById(R.id.txt_conmit);
        txt_conmit.setOnClickListener(listener);


    }


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == rela_back) {
                finish();
            } else if (view == txt_conmit) {

                if (TextUtils.isEmpty(edit_1.getText().toString())) {
                    ToastUtils.showLong("请输入反馈标题！");
                } else if (TextUtils.isEmpty(edit_2.getText().toString())) {
                    ToastUtils.showLong("请输入反馈内容！");
                } else {
                    LoadingDialog.getInstance().Show(SuggestionActivity.this, getString(R.string.requesting));
                    FormBody.Builder formbody = new FormBody.Builder();
                    formbody.add("title", edit_1.getText().toString());
                    formbody.add("content", edit_2.getText().toString());
                    RequestOkhttp.getInstance().PostBody(Api.feedback, formbody, new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.e("vvv", "=======提交反馈失败======" + e.toString());
                            LoadingDialog.getInstance().Dismiss();
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            String res = response.body().string();
                            Log.e("vvv", "=======提交反馈======" + res);
                            //  {"code":1,"msg":"操作成功","time":1744081485,"data":null}
                            LoadingDialog.getInstance().Dismiss();
                            try {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        JSONObject jsonObject = JSON.parseObject(res);
                                        if (jsonObject.getIntValue("code") == 1) {
                                            TipsDialogOne tipsDialogOne = new TipsDialogOne(SuggestionActivity.this);
                                            tipsDialogOne.show();
                                            tipsDialogOne.txt_title.setText("提交成功！");
                                            tipsDialogOne.txt_cancler.setText("返回");
                                            tipsDialogOne.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                @Override
                                                public void onDismiss(DialogInterface dialogInterface) {
                                                    finish();
                                                }
                                            });
                                            tipsDialogOne.txt_cancler.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    tipsDialogOne.dismiss();

                                                }
                                            });


                                        }

                                    }
                                });

                            } catch (Exception e) {

                            }

                        }
                    });

                }

            }
        }
    };

}
