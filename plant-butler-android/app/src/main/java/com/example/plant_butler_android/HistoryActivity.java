package com.example.plant_butler_android;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HistoryActivity extends AppCompatActivity {
    TextView textViewHistory;
    EditText editDeviceId, editDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        textViewHistory = findViewById(R.id.textViewHistory);
        editDeviceId = findViewById(R.id.editDeviceId);
        editDays = findViewById(R.id.editDays);

        findViewById(R.id.buttonLoadHistory).setOnClickListener(v -> loadHistory());
        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
    }

    private void loadHistory() {
        String deviceId = editDeviceId.getText().toString().trim();
        String daysStr = editDays.getText().toString().trim();

        if (deviceId.isEmpty()) {
            Toast.makeText(this, "请输入设备ID", Toast.LENGTH_SHORT).show();
            return;
        }

        int days = daysStr.isEmpty() ? 7 : Integer.parseInt(daysStr);

        ApiService.getInstance().getHistory(deviceId, days, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                textViewHistory.setText(response);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(HistoryActivity.this, "获取失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
