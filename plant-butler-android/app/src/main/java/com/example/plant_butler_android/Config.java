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

    public static final String SERVER_HOST = "10.206.94.17"; // 修改这里更换服务器地址   ，是安卓端主动访问服务器的地址
    // 10.206.94.17:3000
    // │
    // └── 这是「电脑」的 IP + 端口
    //     服务器程序运行在这台电脑上

    // 安卓端 → 服务器	✅ 能	主动发请求、发指令、查数据
    // 服务器 → 安卓端	❌ 不能	无法主动推送
    public static final int SERVER_PORT = 3000;//服务器端口号
    public static final String BASE_URL = "http://" + SERVER_HOST + ":" + SERVER_PORT;//拼接成完整的请求地址
}
