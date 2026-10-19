package com.feasycom.feasyhome.ui.mainfragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.IntentUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.adapter.SceneListAdapter;
import com.feasycom.feasyhome.api.Api;
import com.feasycom.feasyhome.api.RequestOkhttp;
import com.feasycom.feasyhome.base.BaseFragment;
import com.feasycom.feasyhome.logic.FamilyModelFactory;
import com.feasycom.feasyhome.ui.AboutUsActivity;
import com.feasycom.feasyhome.ui.FamilyListActivity;
import com.feasycom.feasyhome.ui.HelpActivity;
import com.feasycom.feasyhome.ui.LoginActivity;
import com.feasycom.feasyhome.ui.MemberManageActivity;
import com.feasycom.feasyhome.ui.PrivacyActivity;
import com.feasycom.feasyhome.ui.ScanQrActivity;
import com.feasycom.feasyhome.ui.ShareQrcodeActivity;
import com.feasycom.feasyhome.ui.SuggestionActivity;
import com.feasycom.feasyhome.ui.UserAgreementActivity;
import com.feasycom.feasyhome.ui.UserDetailsActivity;
import com.feasycom.feasyhome.ui.dialog.DelDialog;
import com.feasycom.feasyhome.ui.dialog.LoadingDialog;
import com.hjq.permissions.OnPermissionCallback;

import com.hjq.permissions.XXPermissions;
import com.hjq.permissions.permission.PermissionLists;
import com.hjq.permissions.permission.base.IPermission;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import no.nordicsemi.android.mesh.fsdk.FscMeshSdk;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class MyFragment extends BaseFragment {


    private View view_status;
    private RelativeLayout rela_suggestion, rela_family_manage, rela_head, rela_yh, rela_ys, rela_about_us, rela_help, rela_sync, rela_invite, rela_saoyisao;
    private ImageView img_head;
    private TextView txt_nickname;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.myfragment, container, false);

        view_status = view.findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        rela_family_manage = view.findViewById(R.id.rela_family_manage);
        rela_family_manage.setOnClickListener(listener);
        rela_head = view.findViewById(R.id.rela_head);
        rela_head.setOnClickListener(listener);
        img_head = view.findViewById(R.id.img_head);
        txt_nickname = view.findViewById(R.id.txt_nickname);
        rela_yh = view.findViewById(R.id.rela_yh);
        rela_ys = view.findViewById(R.id.rela_ys);
        rela_yh.setOnClickListener(listener);
        rela_ys.setOnClickListener(listener);
        rela_about_us = view.findViewById(R.id.rela_about_us);
        rela_about_us.setOnClickListener(listener);
        rela_help = view.findViewById(R.id.rela_help);
        rela_help.setOnClickListener(listener);
        rela_sync = view.findViewById(R.id.rela_sync);
        rela_sync.setOnClickListener(listener);
        rela_invite = view.findViewById(R.id.rela_invite);
        rela_invite.setOnClickListener(listener);
        rela_saoyisao = view.findViewById(R.id.rela_saoyisao);
        rela_saoyisao.setOnClickListener(listener);
        rela_suggestion = view.findViewById(R.id.rela_suggestion);
        rela_suggestion.setOnClickListener(listener);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        try {
            if (!TextUtils.isEmpty(SPUtils.getInstance().getString("userinfo", ""))) {
                JSONObject jsonObject = JSON.parseObject(SPUtils.getInstance().getString("userinfo"));
                txt_nickname.setText(jsonObject.getString("nickname"));

                if (jsonObject.getString("avatar").indexOf("svg") != -1) {
                    Glide.with(getActivity()).asBitmap().load(decodeSvgToBitmap(jsonObject.getString("avatar"))).apply(RequestOptions.circleCropTransform()).into(img_head);

                } else {
                    Glide.with(getActivity()).load(Api.FeasyHomeServer + jsonObject.getString("avatar")).apply(RequestOptions.circleCropTransform()).into(img_head);

                }
            }
        } catch (Exception e) {

        }
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == rela_family_manage) {

                Intent intent = new Intent(getActivity(), FamilyListActivity.class);
                startActivity(intent);


            } else if (view == rela_head) {
                Intent intent = new Intent(getActivity(), UserDetailsActivity.class);
                startActivity(intent);
            } else if (view == rela_yh) {
                Intent intent = new Intent(getActivity(), UserAgreementActivity.class);
                startActivity(intent);
            } else if (view == rela_ys) {
                Intent intent = new Intent(getActivity(), PrivacyActivity.class);
                startActivity(intent);
            } else if (view == rela_about_us) {
                Intent intent = new Intent(getActivity(), AboutUsActivity.class);
                startActivity(intent);
            } else if (view == rela_help) {
                Intent intent = new Intent(getActivity(), HelpActivity.class);
                startActivity(intent);
            } else if (view == rela_sync) {
                if (null == FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean()) {
                    ToastUtils.showLong(getString(R.string.tips13));
                } else {
                    UpDataNet();
                }

            } else if (view == rela_invite) {

                if (null == FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue()) {
                    ToastUtils.showShort(getString(R.string.tips9));
                } else {
                    Intent intent = new Intent(getActivity(), ShareQrcodeActivity.class);
                    intent.putExtra("familyBean", FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue());
                    startActivity(intent);
                }

            } else if (view == rela_saoyisao) {


                if (XXPermissions.isGrantedPermission(getActivity(), PermissionLists.getCameraPermission())) {
                    StarSaoyisao();
                } else {

                    new AlertDialog.Builder(getActivity())
                            .setMessage(getString(R.string.tips14))
                            .setPositiveButton(getString(R.string.agree), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    GetCameraPermiss();
                                    dialog.dismiss();
                                }
                            }).setNegativeButton(getString(R.string.cancler), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();


                }

            } else if (view == rela_suggestion) {

                Intent intent = new Intent(getActivity(), SuggestionActivity.class);
                startActivity(intent);

            }

        }
    };


    private void GetCameraPermiss() {

        XXPermissions.with(getActivity()).permission(PermissionLists.getCameraPermission()).request(new OnPermissionCallback() {
            @Override
            public void onResult(@NonNull List<IPermission> grantedList, @NonNull List<IPermission> deniedList) {
                boolean allGranted = deniedList.isEmpty();
                if (allGranted) {
                    StarSaoyisao();
                } else {
                    boolean doNotAskAgain = XXPermissions.isDoNotAskAgainPermissions(getActivity(), deniedList);
                    if (doNotAskAgain) {
                        new AlertDialog.Builder(getActivity())
                                .setMessage(getString(R.string.tips15))
                                .setPositiveButton(getString(R.string.agree), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = IntentUtils.getLaunchAppDetailsSettingsIntent("com.feasycom.feasyhome");
                                        startActivity(intent);
                                        dialog.dismiss();
                                    }
                                }).setNegativeButton(getString(R.string.cancler), new DialogInterface.OnClickListener() {
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


    private void StarSaoyisao() {

        Intent intent = new Intent(getActivity(), ScanQrActivity.class);
        startActivity(intent);

    }


    private void UpDataNet() {

        LoadingDialog.getInstance().Show(getActivity(), getString(R.string.tips16) + "....");

        RequestOkhttp.getInstance().SyncMeshData();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LoadingDialog.getInstance().Dismiss();
            }
        }, 2000);


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
            //  Paint paint = new Paint();
            svg.renderToCanvas(canvas);

            return bitmap;
        } catch (Exception e) {
            Log.e("SvgUtils", "Error parsing SVG", e);
            return null;
        }
    }


}
