package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private ProgressBar progressBar;

    // <-- SharedPreferences 파일 이름과 키를 MainActivity와 동일하게 맞춤 -->
    private static final String PREFS_NAME = "prefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        btnRegister= findViewById(R.id.btnRegister);
        progressBar= findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void loginUser() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "유효한 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        etEmail.setEnabled(false);
        etPassword.setEnabled(false);
        btnLogin.setEnabled(false);
        btnRegister.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    etEmail.setEnabled(true);
                    etPassword.setEnabled(true);
                    btnLogin.setEnabled(true);
                    btnRegister.setEnabled(true);

                    if (task.isSuccessful()) {
                        // <-- 로그인이 성공하면 동일한 "prefs" 파일에 true로 저장 -->
                        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply();

                        Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    } else {
                        String errorMessage;
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException | FirebaseAuthInvalidCredentialsException e) {
                            errorMessage = "아이디 또는 비밀번호가 일치하지 않습니다.";
                        } catch (Exception e) {
                            errorMessage = "로그인에 실패했습니다: " + e.getMessage();
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "유효한 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        etEmail.setEnabled(false);
        etPassword.setEnabled(false);
        btnLogin.setEnabled(false);
        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    etEmail.setEnabled(true);
                    etPassword.setEnabled(true);
                    btnLogin.setEnabled(true);
                    btnRegister.setEnabled(true);

                    if (task.isSuccessful()) {
                        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply();

                        Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    } else {
                        String errorMessage;
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            errorMessage = "잘못된 이메일 형식입니다.";
                        } catch (Exception e) {
                            errorMessage = "회원가입에 실패했습니다: " + e.getMessage();
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
