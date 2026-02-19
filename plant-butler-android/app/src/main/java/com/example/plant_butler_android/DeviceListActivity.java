package com.example.plant_butler_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class DeviceListActivity extends AppCompatActivity {

    LinearLayout deviceListContainer;
    ImageButton buttonBack;
    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        userId = getIntent().getIntExtra("USER_ID", -1);

        deviceListContainer = findViewById(R.id.deviceListContainer);
        buttonBack = findViewById(R.id.buttonBack);

        buttonBack.setOnClickListener(v -> finish());

        loadDevices();
    }

    private void loadDevices() {
        if (userId == -1) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService.getInstance().getDevicesByUserId(userId, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Device>>() {
                    }.getType();
                    List<Device> devices = gson.fromJson(response, listType);

                    deviceListContainer.removeAllViews();

                    if (devices != null && !devices.isEmpty()) {
                        for (Device device : devices) {
                            addDeviceView(device);
                        }
                    } else {
                        TextView emptyView = new TextView(DeviceListActivity.this);
                        emptyView.setText("暂无设备");
                        emptyView.setTextSize(16);
                        emptyView.setGravity(Gravity.CENTER);
                        emptyView.setPadding(16, 32, 16, 32);
                        deviceListContainer.addView(emptyView);
                    }
                } catch (Exception e) {
                    Toast.makeText(DeviceListActivity.this, "解析数据失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(DeviceListActivity.this, "获取设备失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addDeviceView(Device device) {
        TextView deviceView = new TextView(this);
        String deviceInfo = "ID: " + device.getId() + "\n名称: " + device.getName();
        deviceView.setText(deviceInfo);
        deviceView.setTextSize(16);
        deviceView.setPadding(16, 16, 16, 16);
        deviceView.setBackground(getResources().getDrawable(android.R.drawable.dialog_holo_light_frame));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        deviceView.setLayoutParams(params);

        // 添加点击事件，跳转到原生设备详情页面
        deviceView.setOnClickListener(v -> {
            Intent intent = new Intent(DeviceListActivity.this, DeviceDetailActivity.class);
            intent.putExtra("DEVICE_ID", device.getId());
            startActivity(intent);
        });

        deviceListContainer.addView(deviceView);
    }
}
