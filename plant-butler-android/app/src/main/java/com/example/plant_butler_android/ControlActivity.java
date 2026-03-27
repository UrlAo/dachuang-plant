package com.example.plant_butler_android;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ControlActivity extends AppCompatActivity {
    EditText editDeviceId;
    EditText editDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        editDeviceId = findViewById(R.id.editDeviceId);
        editDuration = findViewById(R.id.editDuration);

        findViewById(R.id.buttonWater).setOnClickListener(v -> sendWaterCommand());
        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
    }

    private void sendWaterCommand() {
        String deviceId = editDeviceId.getText().toString().trim();
        if (deviceId.isEmpty()) {
            Toast.makeText(this, "请输入设备ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // 读取浇水时长，默认5秒
        int duration = 5;
        try {
            String durationStr = editDuration.getText().toString().trim();
            if (!durationStr.isEmpty()) {
                duration = Integer.parseInt(durationStr);
                if (duration <= 0) {
                    Toast.makeText(this, "浇水时长必须大于0", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (duration > 60) {
                    Toast.makeText(this, "浇水时长不能超过60秒", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的浇水时长", Toast.LENGTH_SHORT).show();
            return;
        }

        final int finalDuration = duration;
        ApiService.getInstance().sendCommand(deviceId, "water", duration, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ControlActivity.this, "命令发送成功，浇水" + finalDuration + "秒", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ControlActivity.this, "发送失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
