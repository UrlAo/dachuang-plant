package com.example.plant_butler_android;

/**
 * 应用配置类
 * 修改这里可以更换服务器地址
 */
public class Config {
    // 服务器地址配置
    // 选项1: 10.0.2.2 - 标准的Android模拟器访问本机地址（推荐首先尝试）
    // 选项2: 192.168.x.x - 使用实际本机IP（如果10.0.2.2不工作）
    // 选项3: 127.0.0.1 - 仅用于物理设备或特殊情况

    public static final String SERVER_HOST = "192.168.3.35"; // 修改这里更换服务器地址
    public static final int SERVER_PORT = 3000;
    public static final String BASE_URL = "http://" + SERVER_HOST + ":" + SERVER_PORT;
}
