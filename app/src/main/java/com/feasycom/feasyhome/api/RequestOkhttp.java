package com.feasycom.feasyhome.api;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.SPUtils;
import com.feasycom.feasyhome.logic.FamilyModelFactory;

import java.io.File;
import java.io.IOException;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.fsdk.FscMeshNetworkCallback;
import no.nordicsemi.android.mesh.fsdk.FscMeshSdk;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestOkhttp {

    private static volatile RequestOkhttp instance;

    public OkHttpClient okHttpClient;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static RequestOkhttp getInstance() {
        if (instance == null) {
            synchronized (RequestOkhttp.class) {
                if (instance == null) {
                    instance = new RequestOkhttp();
                }
            }
        }
        return instance;
    }


    private String device_id,device_model,app_name,brand, app_key;


    private RequestOkhttp() {

        device_id = DeviceUtils.getAndroidID();
        device_model = DeviceUtils.getModel();
        app_name = "FeasyHome2";
        brand = DeviceUtils.getManufacturer();
        app_key = "88888888";


        okHttpClient = new OkHttpClient();

        okHttpClient.newBuilder().connectTimeout(120, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS).callTimeout(120, TimeUnit.SECONDS).build();
        //.protocols(Collections.singletonList(Protocol.HTTP_1_1))
        //  okHttpClient.protocols().add(Protocol.HTTP_1_1);

    }


    public void Get(String url, Callback callback) {

        //  Log.e("vvvv", "=========get  url==========" + url);
        Request.Builder request = new Request.Builder();
        if (!TextUtils.isEmpty(SPUtils.getInstance().getString("userinfo", ""))) {
            request.addHeader("token", com.alibaba.fastjson.JSON.parseObject(SPUtils.getInstance().getString("userinfo", "")).getString("token"));
            //    Log.e("vvv", "========token==========" + com.alibaba.fastjson.JSON.parseObject(SPUtils.getInstance().getString("userinfo", "")).getString("token"));
        }
        request.addHeader("device_id",device_id);
        request.addHeader("device_model",device_model);
        request.addHeader("app_name",app_name);
        request.addHeader("brand",brand);
        request.addHeader("app_key",app_key);
        request.url(url).build();
        okHttpClient.newCall(request.build()).enqueue(callback);

    }


    public void PostBody(String url, FormBody.Builder formBody, Callback callback) {

        if (!TextUtils.isEmpty(SPUtils.getInstance().getString("userinfo", ""))) {
            formBody.add("token", com.alibaba.fastjson.JSON.parseObject(SPUtils.getInstance().getString("userinfo", "")).getString("token"));
        }
        Request.Builder request = new Request.Builder();
        request.post(formBody.build());
        request.addHeader("device_id",device_id);
        request.addHeader("device_model",device_model);
        request.addHeader("app_name",app_name);
        request.addHeader("brand",brand);
        request.addHeader("app_key",app_key);
        request.url(url).build();
        okHttpClient.newCall(request.build()).enqueue(callback);

    }


    public String getUrl(String url, Map<String, Object> params) {

        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();

        if (params != null && params.size() > 0) {
            for (String key : params.keySet()) {
                builder.addQueryParameter(key, (String) params.get(key));
            }
        }
        String newUrl = builder.build().toString();

        return newUrl;

    }


    private final MediaType MEDIA_TYPE_FILE = MediaType.parse("application/octet-stream");

    /**
     * 上传文件，并将 token 放在请求头中
     *
     * @param url      上传地址
     * @param file     要上传的文件
     * @param callback 回调接口
     */
    public void uploadFileWithTokenInHeader(String url, File file, Callback callback) {

        // 创建请求体
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(file, MEDIA_TYPE_FILE))
                .build();

        // 创建请求
        Request request = new Request.Builder()
                .url(url)
                .header("token", com.alibaba.fastjson.JSON.parseObject(SPUtils.getInstance().getString("userinfo", "")).getString("token")) // 将 token 放在请求头中
                .post(requestBody)
                .build();

        // 发起请求
        okHttpClient.newCall(request).enqueue(callback);

    }


    public void SyncMeshData() {

        try {

            FscMeshSdk.getApi2().QueryMeshNetwork(FscMeshSdk.getApi2().getMeshNetwork().getMeshUUID(), new FscMeshNetworkCallback() {
                @Override
                public void onMeshNetwork(MeshNetwork meshNetwork) {
                    Log.e("vvvv", "===========同步Mesh网络数据=========" + meshNetwork.getNodes().size());

                    String networkData = FscMeshSdk.getApi2().exportMeshNetwork(meshNetwork);
                    JSONObject jsonObject = com.alibaba.fastjson.JSON.parseObject(networkData);


                    //    jsonObject.put("seq_nub", FscMeshSdk.getApi2().getMeshNetwork().getNodes().get(0).incrementSequenceNumber());
                    FormBody.Builder formbody = new FormBody.Builder();
                    formbody.add("networkId", meshNetwork.getMeshUUID());
                    formbody.add("family_str", FamilyModelFactory.getInstance().getFamilyModel().getCurrent_familyBean().getValue().getFamily_str());
                    formbody.add("networkData", new String(EncodeUtils.base64Encode(jsonObject.toJSONString())));
            //        Log.e("vvvv", "===========同步Mesh网络数据=========" + networkData);

                    PostBody(Api.updateFamily, formbody, new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.e("vvvv", "======数据同步失败！========");
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                            String str = response.body().string();
                            Log.e("vvvv", "======数据同步结果========" + str);


                        }
                    });

                }
            });


        } catch (Exception e) {
            Log.e("vvvv", "======数据同步失败！========" + e.toString());

        }


    }

}
