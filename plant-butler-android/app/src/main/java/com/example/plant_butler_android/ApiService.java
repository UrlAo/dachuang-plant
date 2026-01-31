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
                            boolean success = json.optBoolean("success", false);
                            if (success) {
                                // 登录成功，返回完整响应数据
                                callback.onSuccess(responseBody);
                            } else {
                                String error = json.optString("error", "操作失败");
                                callback.onFailure(error);
                            }
                        } catch (Exception e) {
                            callback.onFailure("响应格式错误: " + e.getMessage());
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
                            boolean success = json.optBoolean("success", false);
                            if (success) {
                                // 注册成功，返回完整响应数据
                                callback.onSuccess(responseBody);
                            } else {
                                String error = json.optString("error", "注册失败");
                                callback.onFailure(error);
                            }
                        } catch (Exception e) {
                            callback.onFailure("响应格式错误: " + e.getMessage());
                        }
                    } else {
                        String errorMessage = "请求失败";
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

    // 根据用户ID获取设备列表
    public void getDevicesByUserId(int userId, ApiCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/devices?user_id=" + userId)
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

    // 获取历史遥测数据
    public void getHistory(String deviceId, int days, ApiCallback callback) {
        String url = BASE_URL + "/api/history?id=" + deviceId + "&days=" + days;
        Request request = new Request.Builder()
                .url(url)
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
                        callback.onFailure("获取历史数据失败");
                    }
                });
            }
        });
    }

    // 发送控制命令
    public void sendCommand(String deviceId, String command, int duration, ApiCallback callback) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("deviceId", deviceId);
        bodyMap.put("command", command);
        bodyMap.put("duration", duration);

        String json = gson.toJson(bodyMap);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/command")
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
                        callback.onSuccess(responseBody);
                    } else {
                        callback.onFailure("发送命令失败");
                    }
                });
            }
        });
    }

    // 添加设备
    public void addDevice(int userId, String deviceName, ApiCallback callback) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("user_id", userId);
        bodyMap.put("name", deviceName);
        bodyMap.put("secret", "secret");

        String json = gson.toJson(bodyMap);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/devices")
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
                        callback.onSuccess(responseBody);
                    } else {
                        callback.onFailure("添加设备失败");
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
