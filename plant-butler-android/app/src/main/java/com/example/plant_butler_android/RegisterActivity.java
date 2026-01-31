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

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText editTextRegUsername, editTextRegEmail, editTextRegPassword, editTextConfirmPassword;
    Button buttonRegister;
    TextView textViewLogin;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();

        databaseHelper = new DatabaseHelper(this);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextRegUsername.getText().toString().trim();
                String email = editTextRegEmail.getText().toString().trim();
                String password = editTextRegPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();

                if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "请填写所有字段", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    new AlertDialog.Builder(RegisterActivity.this)
                            .setTitle("注册提示")
                            .setMessage("您两次输入的密码不一致")
                            .setPositiveButton("返回注册", null)
                            .show();
                } else {
                    // 使用服务器API注册
                    registerWithServer(username, password, email);
                }
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initViews() {
        editTextRegUsername = findViewById(R.id.editTextRegUsername);
        editTextRegEmail = findViewById(R.id.editTextRegEmail);
        editTextRegPassword = findViewById(R.id.editTextRegPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);
    }

    private void registerWithServer(String username, String password, String email) {
        ApiService.getInstance().register(username, password, email, new ApiService.ApiCallback() {
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

                            // 注册成功后直接跳转到主界面，像登录一样
                            Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.putExtra("USER_ID", userId); // 传递用户ID
                            intent.putExtra("USERNAME", userName); // 传递用户名
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "服务器返回数据异常", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorMsg = jsonResponse.optString("error", "注册失败");
                        Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } catch (org.json.JSONException e) {
                    Toast.makeText(RegisterActivity.this, "解析服务器响应失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                if (error.contains("该用户名已注册")) {
                    new AlertDialog.Builder(RegisterActivity.this)
                            .setTitle("注册提示")
                            .setMessage(error)
                            .setPositiveButton("返回注册", null)
                            .show();
                } else {
                    Toast.makeText(RegisterActivity.this, "注册失败: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}