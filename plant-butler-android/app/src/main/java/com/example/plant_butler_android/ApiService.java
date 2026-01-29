package com.example.plant_butler_android;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiService {

    // Android模拟器访问本机localhost的标准方法
    // 如果连接失败，修改Config.java中的SERVER_HOST
    private static final String BASE_URL = Config.BASE_URL;
    private static ApiService instance;
    private OkHttpClient client;
    private Gson gson;
    private Handler mainHandler;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private ApiService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        gson = new Gson();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    // 登录接口
    public void login(String username, String password, ApiCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);

        String json = gson.toJson(params);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/login")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onFailure(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                mainHandler.post(() -> {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            if (json.optBoolean("success", true)) {
                                callback.onSuccess(responseBody);
                            } else {
                                String error = json.optString("error", "操作失败");
                                callback.onFailure(error);
                            }
                        } catch (Exception e) {
                            // 如果不是JSON，也当作成功（比如获取设备列表返回的是数组）
                            callback.onSuccess(responseBody);
                        }
                    } else {
                        String errorMessage = "请求失败";
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            errorMessage = json.optString("error", "登录失败");
                        } catch (Exception e) {
                            errorMessage = "登录失败 (" + response.code() + ")";
                        }
                        callback.onFailure(errorMessage);
                    }
                });
            }
        });
    }

    // 注册接口
    public void register(String username, String password, String email, ApiCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        params.put("email", email);

        String json = gson.toJson(params);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/register")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onFailure(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                mainHandler.post(() -> {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            if (json.optBoolean("success", true)) {
                                callback.onSuccess(responseBody);
                            } else {
                                String error = json.optString("error", "注册失败");
                                callback.onFailure(error);
                            }
                        } catch (Exception e) {
                            callback.onSuccess(responseBody);
                        }
                    } else {
                        String errorMessage = "注册失败";
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            errorMessage = json.optString("error", "注册失败");
                        } catch (Exception e) {
                            errorMessage = "注册失败 (" + response.code() + ")";
                        }
                        callback.onFailure(errorMessage);
                    }
                });
            }
        });
    }

    // 获取设备列表
    public void getDevices(ApiCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/devices")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onFailure(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                mainHandler.post(() -> {
                    if (response.isSuccessful()) {
                        callback.onSuccess(responseBody);
                    } else {
                        callback.onFailure("获取设备列表失败");
                    }
                });
            }
        });
    }

    // 回调接口
    public interface ApiCallback {
        void onSuccess(String response);

        void onFailure(String error);
    }
}
