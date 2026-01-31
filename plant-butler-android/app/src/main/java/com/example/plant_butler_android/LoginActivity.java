package com.example.plant_butler_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText editTextUsername, editTextPassword;
    Button buttonLogin;
    TextView textViewRegister;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();

        databaseHelper = new DatabaseHelper(this);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "请填写所有字段", Toast.LENGTH_SHORT).show();
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
        });
    }

    private void initViews() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
    }

    private void loginWithServer(String username, String password) {
        ApiService.getInstance().login(username, password, new ApiService.ApiCallback() {
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