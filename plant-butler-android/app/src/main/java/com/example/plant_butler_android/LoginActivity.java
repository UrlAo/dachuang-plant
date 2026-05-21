package com.example.plant_butler_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText editTextUsername, editTextPassword;
    Button buttonLogin;
    TextView textViewRegister;
    CheckBox checkAutoLogin; // 新增：自动登录复选框
    DatabaseHelper databaseHelper;
    SessionManager sessionManager; // 新增：会话管理器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this); // 初始化会话管理器
        // onCreate 是 Activity 的生命周期入口，页面第一次创建时自动执行，做了三件事：
        // setContentView(R.layout.activity_login) — 把 XML 布局文件加载显示出来
        // initViews() — 把上面声明的 UI 组件和 XML 里的控件一一绑定（即 findViewById）
        // 初始化 DatabaseHelper 和 SessionManager 两个工具对象，准备好供后续使用

        // 检查是否已经登录（自动登录功能）
        if (sessionManager.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("USER_ID", sessionManager.getUserId());
            intent.putExtra("USERNAME", sessionManager.getUsername());
            startActivity(intent);
            finish();
            return;
        }
        // Intent 是 Android 里的"跳转信使"，用来启动另一个 Activity 并传递数据。
        // 告诉系统：从 LoginActivity 跳转到 MainActivity
        // 如果用户之前登录过且没有退出，自动跳转到主页面
        // 状态（之前登录过且没退出）
        // 如果已登录，创建跳转到 MainActivity 的 Intent，附带用户ID和用户名
        // startActivity(intent) — 启动主页面
        // finish() — 关闭登录页，防止用户按返回键回到登录页
        // return — 直接结束，不再执行后面的代码

        // Intent 跳转页面的同时，可以用 putExtra() 附带数据，下一个页面用 getIntent().getXxxExtra() 取出来。

        // 如果存在之前保存的用户名，预填充用户名输入框
        String savedUsername = sessionManager.getSavedUsername();
        if (!savedUsername.isEmpty()) {
            editTextUsername.setText(savedUsername);
        }//从 SessionManager 读取上次保存的用户名
// 如果有值，直接填入输入框——用户打开 App 不用重新输用户名，直接输密码就行

        // sessionManager的数据是存在哪里的？为什么可以做到下次打开app，默认填入用户名？数据存在哪里？
        // 存在手机本地的 SharedPreferences 文件里。SharedPreferences 是 Android 提供的一种轻量级存储方式，
        // 本质上是一个 XML 文件，存放在 App 的私有目录下：

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            //setOnClickListener — 给登录按钮注册点击事件，用户点击时自动执行里面的代码
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString().trim();//.trim() 去掉首尾空格（防止用户不小心输了空格）
                String password = editTextPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "请填写所有字段", Toast.LENGTH_SHORT).show();//Toast 就是屏幕底部那个小黑条提示，不需要用户点击，自己过几秒消失。
                } else {
                    // 使用服务器API登录
                    loginWithServer(username, password);
                }
            }
        });

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        }); // 给 textViewRegister（页面上的"没有账号？去注册"文字）注册点击事件
        // 点击后创建跳转到 RegisterActivity 的 Intent
        // startActivity(intent) 启动注册页
    }

    private void initViews() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        checkAutoLogin = findViewById(R.id.checkAutoLogin); // 初始化自动登录复选框
    }//把 Java 里声明的变量和 XML 布局文件里的控件一一对应绑定起来。

    private void loginWithServer(String username, String password) {
        ApiService.getInstance().login(username, password, new ApiService.ApiCallback() {
            // ApiService.getInstance();  获取 ApiService 单例
            // .login(username, password, ...); // 发起登录请求，传入用户名、密码
            // new ApiService.ApiCallback(); // 同时传入回调对象，定义"成功/失败后干什么"
            @Override
            public void onSuccess(String response) {
                try {
                    // 解析服务器返回的用户信息
                    org.json.JSONObject jsonResponse = new org.json.JSONObject(response);
                    boolean success = jsonResponse.optBoolean("success", false);

                    if (success) {
                        org.json.JSONObject userData = jsonResponse.optJSONObject("user");
                        if (userData != null) {
                            int userId = userData.optInt("id", -1);
                            String userName = userData.optString("username");
                            String userEmail = userData.optString("email");

                            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();

                            // 如果勾选了自动登录，则保存登录状态
                            if (checkAutoLogin.isChecked()) {
                                sessionManager.createLoginSession(userId, userName);
                            } else {
                                // 即使未勾选自动登录，也保存用户名供下次预填充
                                sessionManager.saveUsernameOnly(userName);
                            }

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("USER_ID", userId); // 传递用户ID
                            intent.putExtra("USERNAME", userName); // 传递用户名
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "服务器返回数据异常", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorMsg = jsonResponse.optString("error", "登录失败");
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } catch (org.json.JSONException e) {
                    Toast.makeText(LoginActivity.this, "解析服务器响应失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                if (error.contains("用户名不存在") || error.contains("您的密码不正确")) {
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("登录提示")
                            .setMessage(error)
                            .setPositiveButton("返回登录", null)
                            .show();
                } else {
                    Toast.makeText(LoginActivity.this, "登录失败: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}