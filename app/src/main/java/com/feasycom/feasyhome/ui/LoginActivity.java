package com.feasycom.feasyhome.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.ui.dialog.DelDialog;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.feasycom.feasyhome.ui.dialog.TipsDialogOne;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;


public class LoginActivity extends BaseActivity {


    private AppCompatButton btn_login;
    private TextView txt_yzm, txt_ys, txt_yh;
    private EditText edit_phone, edit_yzm;
    private CheckBox checkbox;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BarUtils.transparentStatusBar(LoginActivity.this);
        BarUtils.setStatusBarLightMode(LoginActivity.this, true);

        setContentView(R.layout.loginactivity);

        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(listener);
        txt_yzm = findViewById(R.id.txt_yzm);
        txt_yzm.setOnClickListener(listener);
        edit_yzm = findViewById(R.id.edit_yzm);
        checkbox = findViewById(R.id.checkbox);
        txt_ys = findViewById(R.id.txt_ys);
        txt_ys.setOnClickListener(listener);
        txt_yh = findViewById(R.id.txt_yh);
        txt_yh.setOnClickListener(listener);

        edit_phone = findViewById(R.id.edit_phone);
        edit_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() > 0) {
                    txt_yzm.setTextColor(getResources().getColor(R.color.txt_blue));

                } else {
                    txt_yzm.setTextColor(getResources().getColor(R.color.txt_none));

                }
            }
        });


        if (0 == SPUtils.getInstance().getInt("firstOpen", 0)) {
            SPUtils.getInstance().put("firstOpen", 1, true);
            DelDialog delDialog = new DelDialog(LoginActivity.this);
            delDialog.show();
            delDialog.txt_title.setText(getResources().getString(R.string.tips1));
            delDialog.txt_del.setText(getResources().getString(R.string.refuse));
            delDialog.txt_cancler.setText(getString(R.string.agree));
            delDialog.txt_del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    delDialog.dismiss();

                }
            });
            delDialog.txt_cancler.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkbox.setChecked(true);
                    delDialog.dismiss();
                }
            });
            delDialog.txt_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    delDialog.dismiss();
                    Intent intent = new Intent(LoginActivity.this, PrivacyActivity.class);
                    startActivity(intent);
                }
            });
        }

    }


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == btn_login) {

                if (TextUtils.isEmpty(edit_phone.getText().toString())) {
                    ToastUtils.showLong(R.string.tips2);
                } else if (TextUtils.isEmpty(edit_yzm.getText().toString())) {
                    ToastUtils.showLong(R.string.tips3);
                } else if (!checkbox.isChecked()) {
                    ToastUtils.showLong(R.string.tips4);
                } else {
                    handler.removeMessages(1001);
                    txt_yzm.setText(R.string.sendverificationcode);
                    LoadingDialog.getInstance().Show(LoginActivity.this, getResources().getString(R.string.requesting));
                    Login(edit_phone.getText().toString(), edit_yzm.getText().toString(), new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            LoadingDialog.getInstance().Dismiss();
                            ToastUtils.showLong(e.toString());
                            Log.e("vvvv", "========Login====erro======" + e.toString());
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                            String str_res = response.body().string();
                            Log.e("vvvv", "========Login==========" + str_res);

                            try {
                                LoadingDialog.getInstance().Dismiss();
                                JSONObject jsonObject = JSON.parseObject(str_res);
                                if (jsonObject.getIntValue("code") == 1) {
                                    JSONObject jsondata = JSON.parseObject(jsonObject.getString("data"));
                                    String jsoninfo = jsondata.getString("userinfo");
                                    SPUtils.getInstance().put("userinfo", jsoninfo, true);

                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }, 500);


                                } else {
                                    ToastUtils.showLong(jsonObject.getString("msg"));
                                }
                            } catch (Exception e) {

                            }


                        }
                    });

                }


            } else if (view == txt_yzm) {

                GetYzm();

            } else if (view == txt_ys) {

                Intent intent = new Intent(LoginActivity.this, PrivacyActivity.class);
                startActivity(intent);

            } else if (view == txt_yh) {

                Intent intent = new Intent(LoginActivity.this, UserAgreementActivity.class);
                startActivity(intent);


            }
        }
    };


    private void GetYzm() {

/* else if (!RegexUtils.isMobileSimple(edit_phone.getText().toString())) {
            ToastUtils.showShort(R.string.tips5);
        }*/

        if (TextUtils.isEmpty(edit_phone.getText().toString())) {
            ToastUtils.showShort(R.string.tips2);
        } else if (edit_phone.getText().toString().length()<5) {
            ToastUtils.showShort(R.string.tips17);
        } else {
            LoadingDialog.getInstance().Show(LoginActivity.this, getResources().getString(R.string.requesting));
            FormBody.Builder formbody = new FormBody.Builder();
            formbody.add("mobile", edit_phone.getText().toString());
            Log.e("vvv", "=======获取验证码===========" + Api.getCaptcha);
            RequestOkhttp.getInstance().PostBody(Api.getCaptcha, formbody, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    LoadingDialog.getInstance().Dismiss();
                    Log.e("vvv", "=======获取验证码=====onFailure======" + e.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String str = response.body().string();
                    Log.e("vvv", "=======获取验证码===========" + str);
                    // {"code":1,"msg":"获取数据成功","time":1736144429,"data":7724}

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                LoadingDialog.getInstance().Dismiss();
                                JSONObject jsonObject = JSON.parseObject(str);
                                if (jsonObject.getIntValue("code") == 1) {
                                    js = 60;
                                    handler.sendEmptyMessage(1001);
                                    TipsDialogOne tipsDialogOne = new TipsDialogOne(LoginActivity.this);
                                    tipsDialogOne.show();
                                    tipsDialogOne.txt_title.setText(getString(R.string.vcode)+"：" + jsonObject.getString("data"));
                                    tipsDialogOne.txt_cancler.setText(getString(R.string.determine));
                                    tipsDialogOne.txt_cancler.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            tipsDialogOne.dismiss();
                                        }
                                    });

                                } else {
                                    ToastUtils.showLong(jsonObject.getString("msg"));
                                }
                            } catch (Exception e) {

                            }
                        }
                    });

                }
            });


        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        System.exit(0);
    }


    private void Login(String phone, String pwd, Callback callback) {

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("mobile", phone);
        formbody.add("captcha", pwd);
        RequestOkhttp.getInstance().PostBody(Api.Login, formbody, callback);

    }

    private int js = 60;
    Handler handler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    if (js > 1) {
                        txt_yzm.setText(getString(R.string.sent)+" (" + js + "s)");
                        js--;
                        handler.sendEmptyMessageDelayed(1001, 1000);
                    } else {
                        txt_yzm.setText(getString(R.string.sendverificationcode));
                    }

                    break;

            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
