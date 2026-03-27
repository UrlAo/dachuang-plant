package com.example.plant_butler_android;

import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DeviceDetailActivity extends AppCompatActivity {

    private TextView textDeviceName, textDeviceId, textStatus, textLastSeen;
    private TextView textTemperature, textSoilHumidity, textAirHumidity, textLightIntensity, textLastWatering;
    private LinearLayout wateringRecordsContainer;
    private Button buttonBack, buttonRefresh;
    private String deviceId;
    private Handler handler;
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        deviceId = getIntent().getStringExtra("DEVICE_ID");

        // 初始化视图
        textDeviceName = findViewById(R.id.textDeviceName);
        textDeviceId = findViewById(R.id.textDeviceId);
        textStatus = findViewById(R.id.textStatus);
        textLastSeen = findViewById(R.id.textLastSeen);
        textTemperature = findViewById(R.id.textTemperature);
        textSoilHumidity = findViewById(R.id.textSoilHumidity);
        textAirHumidity = findViewById(R.id.textAirHumidity);
        textLightIntensity = findViewById(R.id.textLightIntensity);
        textLastWatering = findViewById(R.id.textLastWatering);
        wateringRecordsContainer = findViewById(R.id.wateringRecordsContainer);
        buttonBack = findViewById(R.id.buttonBack);
        buttonRefresh = findViewById(R.id.buttonRefresh);

        buttonBack.setOnClickListener(v -> finish());
        buttonRefresh.setOnClickListener(v -> loadDeviceData());

        // 设置自动刷新（每5秒）
        handler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadDeviceData();
                handler.postDelayed(this, 5000);
            }
        };

        // 首次加载
        loadDeviceData();
        loadWateringRecords();
        handler.postDelayed(refreshRunnable, 5000);
    }

    private void loadDeviceData() {
        // 使用专用接口只拉取当前设备的遥测数据，不再拉取全部设备列表
        ApiService.getInstance().getDeviceTelemetry(deviceId, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Gson gson = new Gson();
                    Telemetry telemetry = gson.fromJson(response, Telemetry.class);
                    updateTelemetryUI(telemetry);
                } catch (Exception e) {
                    Toast.makeText(DeviceDetailActivity.this, "数据解析失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(DeviceDetailActivity.this, "加载失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTelemetryUI(Telemetry t) {
        if (t == null) {
            textTemperature.setText("🌡️ 温度: 无数据");
            textSoilHumidity.setText("💧 土壤湿度: 无数据");
            textAirHumidity.setText("💨 空气湿度: 无数据");
            textLightIntensity.setText("☀️ 光照强度: 无数据");
            textLastWatering.setText("🚿 上次浇水: 无数据");
            return;
        }
        textTemperature.setText("🌡️ 温度: " + t.temperature + "°C");
        textSoilHumidity.setText("💧 土壤湿度: " + t.soil_humidity + "%");
        textAirHumidity.setText("💨 空气湿度: " + t.air_humidity + "%");
        textLightIntensity.setText("☀️ 光照强度: " + t.light_intensity + " lux");
        textLastWatering.setText("🚿 上次浇水: " + formatTime(t.auto_watering));
    }

    private String formatTime(Long timestamp) {
        if (timestamp == null || timestamp == 0) {
            return "从未";
        }
        try {
            Date date = new Date(timestamp * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return "未知";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }

    // 加载浇水记录
    private void loadWateringRecords() {
        ApiService.getInstance().getWateringRecords(deviceId, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    JSONArray data = json.optJSONArray("data");
                    updateWateringRecordsUI(data);
                } catch (Exception e) {
                    // 解析失败时静默处理，不打扰用户
                }
            }

            @Override
            public void onFailure(String error) {
                // 静默处理，传感器数据更重要
            }
        });
    }

    // 渲染浇水记录列表
    private void updateWateringRecordsUI(JSONArray data) {
        wateringRecordsContainer.removeAllViews();

        if (data == null || data.length() == 0) {
            TextView empty = new TextView(this);
            empty.setText("暂无浇水记录");
            empty.setTextSize(14);
            empty.setTextColor(0xFF999999);
            empty.setPadding(8, 8, 8, 8);
            wateringRecordsContainer.addView(empty);
            return;
        }

        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject record = data.getJSONObject(i);
                String type = record.optString("type", "unknown");
                long timestamp = record.optLong("timestamp", 0);
                String status = record.optString("status", "");

                String typeLabel = "manual".equals(type) ? "🖐 手动" : "🤖 自动";
                String statusLabel = "done".equals(status) ? "✅" : "pending".equals(status) ? "⏳" : "❌";
                String timeStr = formatTime(timestamp);

                TextView row = new TextView(this);
                row.setText(typeLabel + "  " + timeStr + "  " + statusLabel);
                row.setTextSize(14);
                row.setPadding(12, 10, 12, 10);
                row.setBackgroundColor(i % 2 == 0 ? 0xFFF5F5F5 : 0xFFFFFFFF);
                wateringRecordsContainer.addView(row);
            } catch (Exception ignored) {
            }
        }
    }

    // 遥测数据结构，对应服务器 /api/device/telemetry 返回字段
    static class Telemetry {
        double soil_humidity;
        double temperature;
        int light_intensity;
        double air_humidity;
        long auto_watering;
    }
}
