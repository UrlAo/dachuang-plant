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
    private static final String BASE_URL = Config.BASE_URL; // 修改这里更换服务器地址
    private static ApiService instance; // 单例实例
    private OkHttpClient client; // OkHttpClient 发送网络请求
    // OkHttpClient 是一个 HTTP 请求库，用于发送网络请求。
    // 1. 创建请求
    // Request request = new Request.Builder()
    // .url("http://10.206.94.17:3000/api/login")
    // .post(body) // POST 请求
    // .build();

    // // 2. 发送请求
    // client.newCall(request).enqueue(new Callback() {
    // @Override
    // public void onResponse(Response response) {
    // // 请求成功
    // }

    // @Override
    // public void onFailure(IOException e) {
    // // 请求失败
    // }
    // });
    private Gson gson; // Gson 解析 JSON 数据
    // Gson 是 Google 开发的 JSON 解析库，用于 JSON 字符串和 Java 对象之间的转换。
    private Handler mainHandler; // Handler 切换到主线程更新 UI
    // Handler 用于在不同线程之间传递消息，最常用的是从子线程切换到主线程。

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // 声明请求体的数据格式是 JSON
    private ApiService() { // 私有构造函数
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                // .connectTimeout(30, TimeUnit.SECONDS) // 连接服务器最多等 30 秒
                // .readTimeout(30, TimeUnit.SECONDS) // 等待服务器返回数据最多 30 秒
                // .writeTimeout(30, TimeUnit.SECONDS) // 发送数据给服务器最多 30 秒
                .build();
        gson = new Gson();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService(); // // 第一次调用：创建实例
        }
        return instance;// 之后每次调用：返回同一个实例
    }

    // 登录接口
    public void login(String username, String password, ApiCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);

        String json = gson.toJson(params);
        RequestBody body = RequestBody.create(json, JSON);
        // 把用户名和密码存入 HashMap → 用 Gson 转成 JSON 字符串 → 包装成 HTTP 请求体 → 组装成一个目标地址是 /api/login 的 POST 请求。

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/login")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            // client.newCall(request) 把刚才组装好的请求交给 OkHttp
            // .enqueue(...) 表示异步发送——不阻塞当前线程，OkHttp 会自己开一个后台线程去发请求，等服务器回复后再回调
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onFailure(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 接收数据：把服务器返回的原始文本读出来（response.body().string()）
                // 解析并判断：把文本解析成 JSON，根据里面的 success 字段决定调用 callback.onSuccess() 还是 callback.onFailure()
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
        //ApiCallback是为了防止程序卡死，是有接口回调技术，，让后台线程去等，主线程继续工作，而可以让其他进程参与进来
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/devices?user_id=" + userId)
                .get()   //去服务器读数据，不改数据
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onFailure(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                //结果（responseBody）是 OkHttp 线程拿到的，
                // callback 是你定义的，负责接收并处理这个结果。
                mainHandler.post(() -> {
                    if (response.isSuccessful()) {  
                         //这里直接把 HTTP 状态码作为成功与否的唯一标准——200 就成功，直接把原始 JSON 字符串传出去，让调用者自己去解析设备列表数据。
                        callback.onSuccess(responseBody);
                    } else {
                        callback.onFailure("获取设备列表失败");
                    }
                });
            }
        });
    }
// OkHttp 内部有一个线程池，会根据需要自动创建和复用线程。你调用 .enqueue() 的时候，OkHttp 从池子里取一个空闲线程出来去发请求，用完再放回去。你不需要关心"现在有几个线程"，这些都是 OkHttp 帮你管的。
// 你的 App 里一共有哪些线程？
// 大致有这几类：
// 线程	                 谁创建的	           干什么
// 主线程（UI线程）  	Android 系统	渲染界面、处理点击事件
// OkHttp 线程池	   OkHttp 自动   	发网络请求、等待响应
// 其他系统线程        	Android 系统	垃圾回收等底层工作

    // 获取历史遥测数据
    public void getHistory(String deviceId, int days, ApiCallback callback) {//callback理解为拿到结果后怎么处理
        String url = BASE_URL + "/api/history?id=" + deviceId + "&days=" + days;
        // 最终 URL 长这样：（这是android发出去的请求），之后服务器会返回一段json
        // http://10.206.94.17:3000/api/history?id=device001&days=7
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

    // 获取单个设备最新遥测数据（传感器数据 + 最近浇水时间）
    // 对应服务器接口：GET /api/device/telemetry?id={deviceId}
    public void getDeviceTelemetry(String deviceId, ApiCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/device/telemetry?id=" + deviceId)
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
                        callback.onFailure("获取遥测数据失败");
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

    // 获取设备浇水历史记录（手动+自动，最近5条）
    // 对应服务器接口：GET /api/watering-records/{deviceId}
    public void getWateringRecords(String deviceId, ApiCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/watering-records/" + deviceId)
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
                        callback.onFailure("获取浇水记录失败");
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
