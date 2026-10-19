package com.feasycom.feasyhome.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.IntentUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caverock.androidsvg.SVG;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.ui.dialog.DelDialog;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.feasycom.feasyhome.ui.dialog.SingleInputDialog;
import com.feasycom.feasyhome.ui.dialog.TipsDialogOne;
import com.hjq.permissions.OnPermissionCallback;

import com.hjq.permissions.XXPermissions;
import com.hjq.permissions.permission.PermissionLists;
import com.hjq.permissions.permission.base.IPermission;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class UserDetailsActivity extends BaseActivity {

    private View view_status;
    private RelativeLayout rela_back, rela_loginout, rela_nickname, rela_zhuxiao;
    private ImageView img_head;
    private TextView txt_nickname, txt_phone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(UserDetailsActivity.this);
        BarUtils.setStatusBarLightMode(UserDetailsActivity.this, true);
        setContentView(R.layout.userdetailactivity);

        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);
        rela_loginout = findViewById(R.id.rela_loginout);
        rela_loginout.setOnClickListener(listener);
        rela_nickname = findViewById(R.id.rela_nickname);
        rela_nickname.setOnClickListener(listener);

        img_head = findViewById(R.id.img_head);
        img_head.setOnClickListener(listener);
        txt_nickname = findViewById(R.id.txt_nickname);
        txt_phone = findViewById(R.id.txt_phone);
        rela_zhuxiao = findViewById(R.id.rela_zhuxiao);
        rela_zhuxiao.setOnClickListener(listener);


        if (!TextUtils.isEmpty(SPUtils.getInstance().getString("userinfo", ""))) {
            JSONObject jsonObject = JSON.parseObject(SPUtils.getInstance().getString("userinfo"));
            txt_phone.setText(jsonObject.getString("mobile"));
            txt_nickname.setText(jsonObject.getString("nickname"));

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (jsonObject.getString("avatar").indexOf("svg") != -1) {
                        Glide.with(UserDetailsActivity.this).asBitmap().load(decodeSvgToBitmap(jsonObject.getString("avatar"))).apply(RequestOptions.circleCropTransform()).into(img_head);

                    } else {
                        Glide.with(UserDetailsActivity.this).load(Api.FeasyHomeServer + jsonObject.getString("avatar")).apply(RequestOptions.circleCropTransform()).into(img_head);
                        Log.e("vvvv", "======加载头像==1==" + Api.FeasyHomeServer + jsonObject.getString("avatar"));
                    }
                    img_head.requestFocus();
                }
            }, 500);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();


    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == rela_back) {
                finish();
            } else if (view == rela_loginout) {

                LoginOut();

            } else if (view == rela_nickname) {
                SingleInputDialog singleInputDialog = new SingleInputDialog(UserDetailsActivity.this);
                singleInputDialog.show();
                singleInputDialog.edit_name.setHint(getString(R.string.tips33));
                singleInputDialog.txt_title.setText(getString(R.string.tips34));
                singleInputDialog.txt_cancler.setText(R.string.cancler);
                singleInputDialog.txt_ok.setText(R.string.determine);
                singleInputDialog.txt_cancler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        singleInputDialog.dismiss();
                    }
                });
                singleInputDialog.txt_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(singleInputDialog.edit_name.getText().toString())) {
                            ToastUtils.showShort(R.string.tips33);
                        } else if (singleInputDialog.edit_name.getText().toString().length() < 2) {
                            ToastUtils.showShort(R.string.tips35);
                        } else {
                            singleInputDialog.dismiss();
                            LoadingDialog.getInstance().Show(UserDetailsActivity.this, getString(R.string.requesting));
                            ChangeNickName(singleInputDialog.edit_name.getText().toString());

                        }
                    }
                });

            } else if (view == img_head) {

                if (XXPermissions.isGrantedPermission(UserDetailsActivity.this, PermissionLists.getReadExternalStoragePermission())) {
                    ChangePic();
                } else {

                    new AlertDialog.Builder(UserDetailsActivity.this)
                            .setMessage(R.string.tips36)
                            .setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    GetPicPermiss();
                                    dialog.dismiss();
                                }
                            }).setNegativeButton(R.string.cancler, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();


                }


            } else if (view == rela_zhuxiao) {

                DelDialog delDialog = new DelDialog(UserDetailsActivity.this);
                delDialog.show();
                delDialog.txt_title.setText(R.string.tips37);
                delDialog.txt_cancler.setText(R.string.cancler);
                delDialog.txt_del.setText(R.string.determine);
                delDialog.txt_cancler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        delDialog.dismiss();
                    }
                });
                delDialog.txt_del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        delDialog.dismiss();
                        zhuxiao();
                    }
                });

            }
        }
    };


    private void zhuxiao() {
        LoadingDialog.getInstance().Show(UserDetailsActivity.this, getString(R.string.requesting));

        RequestOkhttp.getInstance().PostBody(Api.deleteUser, new FormBody.Builder(), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                LoadingDialog.getInstance().Dismiss();
                String str = response.body().string();
                Log.e("vvv", "========注销账户=========" + str);
                //{"code":1,"msg":"注销成功","time":1740541388,"data":null}

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject jsonObject = JSON.parseObject(str);
                            if (jsonObject.getIntValue("code") == 1) {

                                SPUtils.getInstance().put("userinfo", "", true);
                                startActivity(new Intent(UserDetailsActivity.this, LoginActivity.class));
                                finish();
                                ActivityUtils.finishActivity(MainActivity.class);
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


    private static final int PICK_IMAGE_REQUEST = 1;

    DelDialog changePicDialog;

    private void ChangePic() {
        changePicDialog = new DelDialog(UserDetailsActivity.this);
        changePicDialog.show();
        changePicDialog.txt_title.setText(R.string.tips38);
        changePicDialog.txt_cancler.setText(R.string.cancler);
        changePicDialog.txt_del.setText(R.string.set);
        changePicDialog.txt_cancler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePicDialog.dismiss();
            }
        });
        changePicDialog.txt_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePicDialog.dismiss();

                if (XXPermissions.isGrantedPermission(UserDetailsActivity.this, PermissionLists.getReadExternalStoragePermission())) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                }

            }
        });

    }


    private void GetPicPermiss() {

        XXPermissions.with(UserDetailsActivity.this).permission(PermissionLists.getReadExternalStoragePermission())
                .request(new OnPermissionCallback() {
                    @Override
                    public void onResult(@NonNull List<IPermission> grantedList, @NonNull List<IPermission> deniedList) {
                        boolean allGranted = deniedList.isEmpty();
                        if (allGranted) {
                            ChangePic();
                        } else {
                            boolean doNotAskAgain = XXPermissions.isDoNotAskAgainPermissions(UserDetailsActivity.this, deniedList);
                            if (doNotAskAgain) {
                                new AlertDialog.Builder(UserDetailsActivity.this)
                                        .setMessage("需要“存储”权限，去设置一下吧!")
                                        .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = IntentUtils.getLaunchAppDetailsSettingsIntent("com.feasycom.feasyhome");
                                                startActivity(intent);
                                                dialog.dismiss();
                                            }
                                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }

                        }
                    }
                });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (null != changePicDialog && changePicDialog.isShowing()) {
            changePicDialog.dismiss();
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            if (selectedImageUri != null) {
                String str_img = getImageRealPathFromUri(selectedImageUri);
                Log.e("vvvv", "=======照片路径======" + str_img);
                LoadingDialog.getInstance().Show(UserDetailsActivity.this, getString(R.string.requesting));
                RequestOkhttp.getInstance().uploadFileWithTokenInHeader(Api.upload, new File(str_img), new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                Log.e("vvvv", "=======onFailure======" + e.toString());
                                LoadingDialog.getInstance().Dismiss();
                                ToastUtils.showShort("上传失败-网络异常1");
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                String str = response.body().string();
                                Log.e("vvvv", "======onResponse============" + str);
                                //{"code":1,"msg":"Upload successful","time":1740452981,"data":{"url":"\/uploads\/20250225\/963f0a51f9a048457e13693be8dd6770.jpg"}}

                                try {
                                    JSONObject jsonObject = JSON.parseObject(str);
                                    if (jsonObject.getIntValue("code") == 1) {
                                        JSONObject json_data = jsonObject.getJSONObject("data");
                                        ChangeHeader(json_data.getString("url"));

                                    } else {
                                        LoadingDialog.getInstance().Dismiss();
                                        ToastUtils.showShort(jsonObject.getString("msg"));
                                    }

                                } catch (Exception e) {
                                    LoadingDialog.getInstance().Dismiss();
                                    ToastUtils.showShort("上传失败-网络异常2");
                                }

                            }
                        }
                );
            } else {
                ToastUtils.showLong("请选择照片！");
            }
        } else {
            ToastUtils.showLong("请选择照片！");
        }
    }


    private String getImageRealPathFromUri(Uri uri) {
        String filePath = null;
        if (uri != null) {
            try {
                // 尝试通过 ContentResolver 查询真实路径
                String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        filePath = cursor.getString(columnIndex);
                    }
                    cursor.close();
                }

                // 如果无法通过查询获取路径，尝试从 Uri 路径中提取
                if (filePath == null) {
                    String path = uri.getPath();
                    if (path != null) {
                        filePath = path;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return filePath;
    }

    private void LoginOut() {

        RequestOkhttp.getInstance().PostBody(Api.logout, new FormBody.Builder(), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = response.body().string();
                Log.e("vvvv", "=========登出======" + str);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TipsDialogOne tipsDialogOne = new TipsDialogOne(UserDetailsActivity.this);
                        tipsDialogOne.show();
                        tipsDialogOne.txt_title.setText(R.string.tips39);
                        tipsDialogOne.txt_cancler.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                tipsDialogOne.dismiss();
                                SPUtils.getInstance().put("userinfo", "", true);
                                startActivity(new Intent(UserDetailsActivity.this, LoginActivity.class));
                                finish();
                                ActivityUtils.finishActivity(MainActivity.class);
                            }
                        });
                    }
                });

            }
        });


    }


    private void ChangeNickName(String newnickname) {

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("type", "nickname");
        formbody.add("nickname", newnickname);

        RequestOkhttp.getInstance().PostBody(Api.modifyUserInfo, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String str = response.body().string();
                Log.e("vvvv", "===========更改nickname===========" + str);
                //{"code":1,"msg":"修改成功","time":1739958283,"data":null}
                // {"code":0,"msg":"名称2-18个字节","time":1741082624,"data":null}

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        LoadingDialog.getInstance().Dismiss();
                        try {
                            JSONObject jsonObject = JSON.parseObject(str);
                            if (jsonObject.getIntValue("code") == 1) {
                                JSONObject jsonObject_user = JSON.parseObject(SPUtils.getInstance().getString("userinfo"));
                                jsonObject_user.put("nickname", newnickname);
                                SPUtils.getInstance().put("userinfo", jsonObject_user.toString(), true);
                                txt_nickname.setText(newnickname);
                            } else {

                            }
                            ToastUtils.showLong(jsonObject.getString("msg"));

                        } catch (Exception e) {

                        }


                    }
                });


            }
        });

    }


    private void ChangeHeader(String img_url) {

        FormBody.Builder formbody = new FormBody.Builder();
        formbody.add("type", "avatar");
        formbody.add("avatar", img_url);

        RequestOkhttp.getInstance().PostBody(Api.modifyUserInfo, formbody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LoadingDialog.getInstance().Dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String str = response.body().string();
                Log.e("vvvv", "===========更改头像===========" + str);
                //{"code":1,"msg":"修改成功","time":1739958283,"data":null}
                try {
                    JSONObject jsonObject = JSON.parseObject(str);
                    if (jsonObject.getIntValue("code") == 1) {
                        ToastUtils.showShort(jsonObject.getString("msg"));
                        JSONObject jsonObject_user = JSON.parseObject(SPUtils.getInstance().getString("userinfo"));
                        jsonObject_user.put("avatar", img_url);
                        SPUtils.getInstance().put("userinfo", jsonObject_user.toString(), true);


                        if (null != changePicDialog) {
                            changePicDialog.dismiss();
                        }
                    }
                } catch (Exception e) {

                }
                LoadingDialog.getInstance().Dismiss();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Glide.with(UserDetailsActivity.this).load(Api.FeasyHomeServer + img_url).apply(RequestOptions.circleCropTransform()).into(img_head);
                        Log.e("vvvv", "======加载头像==2==" + Api.FeasyHomeServer + img_url);

                        img_head.requestFocus();
                    }
                }, 500);

            }
        });

    }


    private Bitmap decodeSvgToBitmap(String base64Svg) {
        base64Svg = base64Svg.split(",")[1];
        // Decode the Base64 string
        byte[] decodedBytes = Base64.decode(base64Svg, Base64.DEFAULT);

        // Convert byte array to InputStream
        //   ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes);

        try {
            // Parse the SVG from the InputStream
            SVG svg = SVG.getFromString(new String(decodedBytes));

            // Create a Bitmap with the same dimensions as the SVG
            int width = (int) svg.getDocumentWidth();
            int height = (int) svg.getDocumentHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            // Draw the SVG onto the Bitmap
            Canvas canvas = new Canvas(bitmap);
            //    Paint paint = new Paint();
            svg.renderToCanvas(canvas);

            return bitmap;
        } catch (Exception e) {
            Log.e("SvgUtils", "Error parsing SVG", e);
            return null;
        }
    }


    Handler handler = new Handler();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
