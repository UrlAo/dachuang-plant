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
                new android.app.AlertDialog.Builder(RegisterActivity.this)
                        .setTitle("注册成功")
                        .setMessage("您已成功注册")
                        .setPositiveButton("返回登录", (dialog, which) -> {
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .setCancelable(false)
                        .show();
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