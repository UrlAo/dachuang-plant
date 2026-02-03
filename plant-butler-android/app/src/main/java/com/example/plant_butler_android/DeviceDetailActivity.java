package com.example.plant_butler_android;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DeviceDetailActivity extends AppCompatActivity {

    private TextView textDeviceName, textDeviceId, textStatus, textLastSeen;
    private TextView textTemperature, textSoilHumidity, textAirHumidity, textLightIntensity, textLastWatering;
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
        handler.postDelayed(refreshRunnable, 5000);
    }

    private void loadDeviceData() {
        ApiService.getInstance().getDevices(new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<DeviceWithTelemetry>>() {
                    }.getType();
                    List<DeviceWithTelemetry> devices = gson.fromJson(response, listType);

                    // 找到对应的设备
                    for (DeviceWithTelemetry device : devices) {
                        if (device.id.equals(deviceId)) {
                            updateUI(device);
                            return;
                        }
                    }
                    Toast.makeText(DeviceDetailActivity.this, "未找到设备", Toast.LENGTH_SHORT).show();
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

    private void updateUI(DeviceWithTelemetry device) {
        textDeviceName.setText(device.name != null ? device.name : "未命名设备");
        textDeviceId.setText("ID: " + device.id);
        textStatus.setText("状态: " + (device.status != null ? device.status : "未知"));
        textLastSeen.setText("最后在线: " + formatTime(device.last_seen));

        if (device.telemetry != null) {
            Telemetry t = device.telemetry;
            textTemperature.setText("温度: " + t.temperature + "°C");
            textSoilHumidity.setText("土壤湿度: " + t.soil_humidity + "%");
            textAirHumidity.setText("空气湿度: " + t.air_humidity + "%");
            textLightIntensity.setText("光照强度: " + t.light_intensity + " lux");
            textLastWatering.setText("上次浇水: " + formatTime(t.auto_watering));
        } else {
            textTemperature.setText("温度: 无数据");
            textSoilHumidity.setText("土壤湿度: 无数据");
            textAirHumidity.setText("空气湿度: 无数据");
            textLightIntensity.setText("光照强度: 无数据");
            textLastWatering.setText("上次浇水: 无数据");
        }
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

    // 内部类：设备及遥测数据
    static class DeviceWithTelemetry {
        String id;
        String name;
        String status;
        Long last_seen;
        Telemetry telemetry;
    }

    static class Telemetry {
        double soil_humidity;
        double temperature;
        int light_intensity;
        double air_humidity;
        long auto_watering;
    }
}
