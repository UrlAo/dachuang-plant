package com.example.plant_butler_android;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private EditText editDeviceId, editDays;
    private LinearLayout summaryCard, tableHeader, tableBody;
    private TextView textSummary;

    // 列宽（px），与布局中 dp 对应，用代码创建行时复用
    private static final int[] COL_WIDTHS_DP = { 160, 72, 80, 80, 88 };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        editDeviceId = findViewById(R.id.editDeviceId);
        editDays = findViewById(R.id.editDays);
        summaryCard = findViewById(R.id.summaryCard);
        textSummary = findViewById(R.id.textSummary);
        tableHeader = findViewById(R.id.tableHeader);
        tableBody = findViewById(R.id.tableBody);

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
                renderTable(response, deviceId, days);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(HistoryActivity.this, "获取失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderTable(String response, String deviceId, int days) {
        tableBody.removeAllViews();

        try {
            // 服务器返回的是数组或包装对象，兼容两种格式
            JSONArray dataArray;
            int totalCount = 0;

            if (response.trim().startsWith("[")) {
                dataArray = new JSONArray(response);
                totalCount = dataArray.length();
            } else {
                JSONObject root = new JSONObject(response);
                dataArray = root.optJSONArray("data");
                if (dataArray == null)
                    dataArray = new JSONArray();
                totalCount = root.optInt("count", dataArray.length());
            }

            // 统计摘要
            textSummary.setText("设备 " + deviceId + "  ·  近 " + days + " 天  ·  共 " + totalCount + " 条记录");
            summaryCard.setVisibility(View.VISIBLE);
            tableHeader.setVisibility(View.VISIBLE);

            if (dataArray.length() == 0) {
                addEmptyRow("该设备暂无历史数据");
                return;
            }

            // 渲染数据行
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject row = dataArray.getJSONObject(i);

                long ts = row.optLong("timestamp", 0);
                double temp = row.optDouble("temperature", Double.NaN);
                double soil = row.optDouble("soil_humidity", Double.NaN);
                double air = row.optDouble("air_humidity", Double.NaN);
                int light = row.optInt("light_intensity", -1);

                String timeStr = formatTime(ts);
                String tempStr = Double.isNaN(temp) ? "--" : String.format(Locale.getDefault(), "%.1f°C", temp);
                String soilStr = Double.isNaN(soil) ? "--" : String.format(Locale.getDefault(), "%.1f%%", soil);
                String airStr = Double.isNaN(air) ? "--" : String.format(Locale.getDefault(), "%.1f%%", air);
                String lightStr = (light < 0) ? "--" : light + " lx";

                addDataRow(i, timeStr, tempStr, soilStr, airStr, lightStr,
                        temp, soil, air, light);
            }

        } catch (Exception e) {
            addEmptyRow("数据解析错误: " + e.getMessage());
        }
    }

    /** 添加一条数据行，颜色根据数值做轻微提示 */
    private void addDataRow(int index, String time, String temp, String soil, String air, String light,
            double tempVal, double soilVal, double airVal, int lightVal) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        // 斑马纹背景
        row.setBackgroundColor(index % 2 == 0 ? 0xFFF5F5F5 : 0xFFFFFFFF);

        // 底部细线
        row.setPadding(0, 0, 0, 1);

        String[] values = { time, temp, soil, air, light };
        int[] colorHints = {
                Color.parseColor("#212121"), // 时间：普通黑
                getTempColor(tempVal), // 温度：数值着色
                getHumidityColor(soilVal), // 土壤：数值着色
                getHumidityColor(airVal), // 空气：数值着色
                Color.parseColor("#212121") // 光照：普通黑
        };
        int[] gravities = { Gravity.START, Gravity.CENTER, Gravity.CENTER, Gravity.CENTER, Gravity.CENTER };

        for (int c = 0; c < values.length; c++) {
            // 分隔线
            if (c > 0) {
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT));
                divider.setBackgroundColor(Color.parseColor("#E0E0E0"));
                row.addView(divider);
            }

            TextView cell = new TextView(this);
            int widthPx = dpToPx(COL_WIDTHS_DP[c]);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(widthPx,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cell.setLayoutParams(lp);
            cell.setText(values[c]);
            cell.setTextSize(13f);
            cell.setTextColor(colorHints[c]);
            cell.setGravity(gravities[c]);
            cell.setPadding(dpToPx(10), dpToPx(9), dpToPx(10), dpToPx(9));
            row.addView(cell);
        }

        tableBody.addView(row);
    }

    /** 空状态行 */
    private void addEmptyRow(String msg) {
        TextView tv = new TextView(this);
        tv.setText(msg);
        tv.setTextSize(14);
        tv.setTextColor(Color.parseColor("#999999"));
        tv.setPadding(dpToPx(16), dpToPx(24), dpToPx(16), dpToPx(24));
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(null, Typeface.ITALIC);
        tableBody.addView(tv);
    }

    /** 温度颜色：>30 红，<10 蓝，其余绿 */
    private int getTempColor(double val) {
        if (Double.isNaN(val))
            return Color.parseColor("#212121");
        if (val > 30)
            return Color.parseColor("#F44336");
        if (val < 10)
            return Color.parseColor("#2196F3");
        return Color.parseColor("#388E3C");
    }

    /** 湿度颜色：<20 红（过干），>80 蓝（过湿），其余绿 */
    private int getHumidityColor(double val) {
        if (Double.isNaN(val))
            return Color.parseColor("#212121");
        if (val < 20)
            return Color.parseColor("#F44336");
        if (val > 80)
            return Color.parseColor("#2196F3");
        return Color.parseColor("#388E3C");
    }

    private String formatTime(long timestamp) {
        if (timestamp == 0)
            return "--";
        try {
            Date date = new Date(timestamp * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return "--";
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
