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
    Button buttonLogout, buttonDevices, buttonAddDevice, buttonHistory, buttonControl;
    private int userId = -1; // 存储当前登录用户的ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取传递过来的用户信息
        Intent intent = getIntent();
        userId = intent.getIntExtra("USER_ID", -1); // 获取用户ID
        String username = intent.getStringExtra("USERNAME");

        textViewWelcome = findViewById(R.id.textViewWelcome);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonDevices = findViewById(R.id.buttonDevices);
        buttonAddDevice = findViewById(R.id.buttonAddDevice);
        buttonHistory = findViewById(R.id.buttonHistory);
        buttonControl = findViewById(R.id.buttonControl);

        // 显示欢迎信息
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
            // 跳转到设备列表界面
            Intent deviceIntent = new Intent(MainActivity.this, DeviceListActivity.class);
            deviceIntent.putExtra("USER_ID", userId);
            startActivity(deviceIntent);
        });

        buttonAddDevice.setOnClickListener(v -> {
            // 添加设备
            showAddDeviceDialog();
        });

        buttonHistory.setOnClickListener(v -> {
            // 跳转到历史数据页面
            Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(historyIntent);
        });

        buttonControl.setOnClickListener(v -> {
            // 跳转到控制页面
            Intent controlIntent = new Intent(MainActivity.this, ControlActivity.class);
            startActivity(controlIntent);
        });
    }

    private void showAddDeviceDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("添加新设备");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("请输入设备名称");
        builder.setView(input);

        builder.setPositiveButton("添加", (dialog, which) -> {
            String deviceName = input.getText().toString().trim();
            if (deviceName.isEmpty()) {
                Toast.makeText(MainActivity.this, "设备名称不能为空", Toast.LENGTH_SHORT).show();
            } else {
                addDevice(deviceName);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void addDevice(String deviceName) {
        if (userId == -1) {
            Toast.makeText(MainActivity.this, "用户未登录", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService.getInstance().addDevice(userId, deviceName, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(MainActivity.this, "设备添加成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(MainActivity.this, "添加失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}