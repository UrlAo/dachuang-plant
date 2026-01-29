package com.example.plant_butler_android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView textViewWelcome;
    Button buttonLogout, buttonDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewWelcome = findViewById(R.id.textViewWelcome);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonDevices = findViewById(R.id.buttonDevices);

        // 获取传递过来的用户名信息并显示欢迎信息
        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");
        if (username != null && !username.isEmpty()) {
            textViewWelcome.setText("欢迎, " + username + "!");
        } else {
            textViewWelcome.setText("欢迎!");
        }

        buttonLogout.setOnClickListener(v -> {
            // 跳转回登录页面
            Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logoutIntent);
            finish();
        });

        buttonDevices.setOnClickListener(v -> {
            // 获取设备列表
            getDeviceList();
        });
    }

    private void getDeviceList() {
        ApiService.getInstance().getDevices(new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Device>>() {
                    }.getType();
                    List<Device> devices = gson.fromJson(response, listType);

                    if (devices != null && !devices.isEmpty()) {
                        StringBuilder deviceInfo = new StringBuilder("设备列表:\n");
                        for (Device device : devices) {
                            deviceInfo.append("ID: ").append(device.getId())
                                    .append(", 名称: ").append(device.getName())
                                    .append("\n");
                        }
                        Toast.makeText(MainActivity.this, deviceInfo.toString(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "暂无设备", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "解析数据失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(MainActivity.this, "获取设备列表失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}