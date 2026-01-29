package com.example.plant_butler_android;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ControlActivity extends AppCompatActivity {
    EditText editDeviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        editDeviceId = findViewById(R.id.editDeviceId);

        findViewById(R.id.buttonWater).setOnClickListener(v -> sendWaterCommand());
        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
    }

    private void sendWaterCommand() {
        String deviceId = editDeviceId.getText().toString().trim();
        if (deviceId.isEmpty()) {
            Toast.makeText(this, "请输入设备ID", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService.getInstance().sendCommand(deviceId, "water", 5, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ControlActivity.this, "命令发送成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ControlActivity.this, "发送失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
