package com.example.plant_butler_android;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

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
                        // 添加设备数量统计
                        addDeviceCountHeader(devices.size());

                        for (int i = 0; i < devices.size(); i++) {
                            addDeviceCard(devices.get(i), i);
                        }
                    } else {
                        addEmptyView();
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

    // 添加设备数量统计头部
    private void addDeviceCountHeader(int count) {
        TextView headerView = new TextView(this);
        headerView.setText("📊 共 " + count + " 台设备");
        headerView.setTextSize(14);
        headerView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        headerView.setPadding(dpToPx(4), dpToPx(8), dpToPx(4), dpToPx(16));
        deviceListContainer.addView(headerView);
    }

    // 添加空状态视图
    private void addEmptyView() {
        LinearLayout emptyLayout = new LinearLayout(this);
        emptyLayout.setOrientation(LinearLayout.VERTICAL);
        emptyLayout.setGravity(Gravity.CENTER);
        emptyLayout.setPadding(dpToPx(32), dpToPx(64), dpToPx(32), dpToPx(64));

        TextView emptyIcon = new TextView(this);
        emptyIcon.setText("📭");
        emptyIcon.setTextSize(48);
        emptyIcon.setGravity(Gravity.CENTER);

        TextView emptyText = new TextView(this);
        emptyText.setText("暂无设备");
        emptyText.setTextSize(18);
        emptyText.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setPadding(0, dpToPx(16), 0, 0);

        TextView emptyHint = new TextView(this);
        emptyHint.setText("请先在主页添加您的植物管家设备");
        emptyHint.setTextSize(14);
        emptyHint.setTextColor(Color.parseColor("#9E9E9E"));
        emptyHint.setGravity(Gravity.CENTER);
        emptyHint.setPadding(0, dpToPx(8), 0, 0);

        emptyLayout.addView(emptyIcon);
        emptyLayout.addView(emptyText);
        emptyLayout.addView(emptyHint);
        deviceListContainer.addView(emptyLayout);
    }

    // 添加设备卡片
    private void addDeviceCard(Device device, int index) {
        // 创建CardView
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dpToPx(12));
        cardView.setCardElevation((float) dpToPx(3));
        cardView.setCardBackgroundColor(Color.WHITE);
        cardView.setClickable(true);
        cardView.setFocusable(true);

        // 添加点击涟漪效果
        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        cardView.setForeground(ContextCompat.getDrawable(this, outValue.resourceId));

        // 主容器
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.HORIZONTAL);
        mainLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        mainLayout.setGravity(Gravity.CENTER_VERTICAL);

        // 左侧：设备图标区域
        LinearLayout iconContainer = new LinearLayout(this);
        iconContainer.setOrientation(LinearLayout.VERTICAL);
        iconContainer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams iconContainerParams = new LinearLayout.LayoutParams(dpToPx(56), dpToPx(56));
        iconContainer.setLayoutParams(iconContainerParams);

        // 圆形图标背景
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setShape(GradientDrawable.OVAL);
        iconBg.setColor(ContextCompat.getColor(this, R.color.primary_light));
        iconContainer.setBackground(iconBg);

        // 设备图标
        TextView iconText = new TextView(this);
        iconText.setText("🌱");
        iconText.setTextSize(24);
        iconText.setGravity(Gravity.CENTER);
        iconContainer.addView(iconText);

        // 中间：设备信息区域
        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,
                1);
        infoParams.setMargins(dpToPx(16), 0, dpToPx(8), 0);
        infoLayout.setLayoutParams(infoParams);

        // 设备名称
        TextView nameText = new TextView(this);
        String deviceName = device.getName();
        if (deviceName == null || deviceName.isEmpty()) {
            deviceName = "植物管家设备";
        }
        nameText.setText(deviceName);
        nameText.setTextSize(17);
        nameText.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        nameText.setTypeface(null, Typeface.BOLD);
        nameText.setMaxLines(1);

        // 设备ID
        TextView idText = new TextView(this);
        idText.setText("🔖 设备ID: " + device.getId());
        idText.setTextSize(13);
        idText.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        idText.setPadding(0, dpToPx(4), 0, 0);

        // 状态标签
        LinearLayout statusLayout = new LinearLayout(this);
        statusLayout.setOrientation(LinearLayout.HORIZONTAL);
        statusLayout.setPadding(0, dpToPx(8), 0, 0);

        // 在线状态指示 - 根据实际状态显示
        TextView statusBadge = new TextView(this);
        statusBadge.setTextSize(12);
        boolean isOnline = device.isOnline();
        if (isOnline) {
            statusBadge.setText("● 在线");
            statusBadge.setTextColor(ContextCompat.getColor(this, R.color.primary_color));
        } else {
            statusBadge.setText("● 离线");
            statusBadge.setTextColor(Color.parseColor("#9E9E9E"));
        }

        // 序号标签
        TextView indexBadge = new TextView(this);
        indexBadge.setText("  #" + (index + 1));
        indexBadge.setTextSize(12);
        indexBadge.setTextColor(Color.parseColor("#9E9E9E"));

        statusLayout.addView(statusBadge);
        statusLayout.addView(indexBadge);

        infoLayout.addView(nameText);
        infoLayout.addView(idText);
        infoLayout.addView(statusLayout);

        // 右侧：箭头指示
        TextView arrowText = new TextView(this);
        arrowText.setText("›");
        arrowText.setTextSize(28);
        arrowText.setTextColor(Color.parseColor("#BDBDBD"));
        arrowText.setGravity(Gravity.CENTER);

        // 组装视图
        mainLayout.addView(iconContainer);
        mainLayout.addView(infoLayout);
        mainLayout.addView(arrowText);

        cardView.addView(mainLayout);

        // 添加点击事件
        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(DeviceListActivity.this, DeviceDetailActivity.class);
            intent.putExtra("DEVICE_ID", device.getId());
            startActivity(intent);
        });

        deviceListContainer.addView(cardView);
    }

    // dp转px工具方法
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
