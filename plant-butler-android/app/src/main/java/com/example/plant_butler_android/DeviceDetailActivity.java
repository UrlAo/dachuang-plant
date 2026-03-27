package com.example.plant_butler_android;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
    private ImageButton buttonBack;
    private Button buttonRefresh;
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

        // 显示设备ID
        textDeviceId.setText("🔖 ID: " + deviceId);

        buttonBack.setOnClickListener(v -> finish());
        buttonRefresh.setOnClickListener(v -> {
            loadDeviceInfo();
            loadDeviceData();
        });

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
        loadDeviceInfo(); // 加载设备基本信息（名称、状态）
        loadDeviceData(); // 加载传感器数据
        loadWateringRecords();
        handler.postDelayed(refreshRunnable, 5000);
    }

    // 加载设备基本信息（名称、状态、最后在线时间）
    private void loadDeviceInfo() {
        ApiService.getInstance().getDevices(new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Gson gson = new Gson();
                    java.lang.reflect.Type listType = new TypeToken<List<Device>>() {
                    }.getType();
                    List<Device> devices = gson.fromJson(response, listType);

                    // 查找当前设备
                    for (Device device : devices) {
                        if (deviceId.equals(device.getId())) {
                            updateDeviceInfoUI(device);
                            break;
                        }
                    }
                } catch (Exception e) {
                    // 静默处理
                }
            }

            @Override
            public void onFailure(String error) {
                // 静默处理
            }
        });
    }

    // 更新设备基本信息UI
    private void updateDeviceInfoUI(Device device) {
        // 设备名称
        String name = device.getName();
        if (name == null || name.isEmpty()) {
            name = "植物管家设备";
        }
        textDeviceName.setText(name);

        // 设备状态
        boolean isOnline = device.isOnline();
        if (isOnline) {
            textStatus.setText("🟢 在线");
            textStatus.setTextColor(ContextCompat.getColor(this, R.color.primary_color));
        } else {
            textStatus.setText("🔴 离线");
            textStatus.setTextColor(Color.parseColor("#9E9E9E"));
        }

        // 最后在线时间
        Long lastSeen = device.getLastSeen();
        if (lastSeen != null && lastSeen > 0) {
            textLastSeen.setText("⏱ 最后在线: " + formatTime(lastSeen));
        } else {
            textLastSeen.setText("⏱ 最后在线: 从未");
        }
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
            textTemperature.setText("--°C");
            textSoilHumidity.setText("--%");
            textAirHumidity.setText("--%");
            textLightIntensity.setText("-- lux");
            textLastWatering.setText("从未");
            return;
        }
        textTemperature.setText(t.temperature + "°C");
        textSoilHumidity.setText(t.soil_humidity + "%");
        textAirHumidity.setText(t.air_humidity + "%");
        textLightIntensity.setText(t.light_intensity + " lux");
        textLastWatering.setText(formatTime(t.auto_watering));
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
