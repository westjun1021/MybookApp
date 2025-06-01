// app/src/main/java/com/example/myapplication/MyPageActivity.java
package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class MyPageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private TextView tvUserEmail;
    private EditText etNickname;
    private Button btnSaveNickname;

    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etNewPasswordConfirm;
    private Button btnChangePassword;

    private Button btnLogout;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        // 1) FirebaseAuth 초기화 및 현재 사용자 가져오기
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2) 뷰 바인딩 (XML의 ID와 반드시 일치해야 합니다)
        tvUserEmail           = findViewById(R.id.tvUserEmail);
        etNickname            = findViewById(R.id.etNickname);
        btnSaveNickname       = findViewById(R.id.btnSaveNickname);

        etCurrentPassword     = findViewById(R.id.etCurrentPassword);
        etNewPassword         = findViewById(R.id.etNewPassword);
        etNewPasswordConfirm  = findViewById(R.id.etNewPasswordConfirm);
        btnChangePassword     = findViewById(R.id.btnChangePassword);

        btnLogout             = findViewById(R.id.btnLogout);
        progressBar           = findViewById(R.id.progressBar);

        // 3) 이메일과 닉네임 초기값 세팅
        tvUserEmail.setText("이메일: " + currentUser.getEmail());
        String existingDisplayName = currentUser.getDisplayName();
        if (!TextUtils.isEmpty(existingDisplayName)) {
            etNickname.setText(existingDisplayName);
        }

        // 4) 닉네임 저장 버튼 클릭 시
        btnSaveNickname.setOnClickListener(v -> {
            String newNickname = etNickname.getText().toString().trim();
            if (TextUtils.isEmpty(newNickname)) {
                etNickname.setError("닉네임을 입력하세요.");
                return;
            }
            updateNickname(newNickname);
        });

        // 5) 비밀번호 변경 버튼 클릭 시
        btnChangePassword.setOnClickListener(v -> {
            String currentPw = etCurrentPassword.getText().toString().trim();
            String newPw     = etNewPassword.getText().toString().trim();
            String confirmPw = etNewPasswordConfirm.getText().toString().trim();

            if (TextUtils.isEmpty(currentPw)) {
                etCurrentPassword.setError("현재 비밀번호를 입력하세요.");
                return;
            }
            if (TextUtils.isEmpty(newPw) || newPw.length() < 6) {
                etNewPassword.setError("새 비밀번호는 6자 이상이어야 합니다.");
                return;
            }
            if (!newPw.equals(confirmPw)) {
                etNewPasswordConfirm.setError("새 비밀번호가 일치하지 않습니다.");
                return;
            }
            if (newPw.equals(currentPw)) {
                etNewPassword.setError("새 비밀번호가 현재 비밀번호와 동일합니다.");
                return;
            }
            reauthenticateAndChangePassword(currentPw, newPw);
        });

        // 6) 로그아웃 버튼 클릭 시
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            // 로그인 상태 해제(SharedPreferences)
            getSharedPreferences("prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isLoggedIn", false)
                    .apply();
            Toast.makeText(MyPageActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MyPageActivity.this, LoginActivity.class));
            finish();
        });
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 1) 닉네임 업데이트
    private void updateNickname(String newNickname) {
        if (currentUser == null) return;

        // 로딩 표시 및 입력란/버튼 비활성화
        progressBar.setVisibility(View.VISIBLE);
        etNickname.setEnabled(false);
        btnSaveNickname.setEnabled(false);

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newNickname)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    // 작업 후 로딩 숨김 및 입력란/버튼 활성화
                    progressBar.setVisibility(View.GONE);
                    etNickname.setEnabled(true);
                    btnSaveNickname.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(MyPageActivity.this,
                                "닉네임이 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                        // 변경된 닉네임을 메인 화면(네비게이션 헤더)에 반영하기 위해
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(MyPageActivity.this,
                                "닉네임 변경 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 2) 재인증 후 비밀번호 변경
    private void reauthenticateAndChangePassword(String currentPw, String newPw) {
        if (currentUser == null || currentUser.getEmail() == null) return;

        String email = currentUser.getEmail();

        // 로딩 표시 및 입력란/버튼 비활성화
        progressBar.setVisibility(View.VISIBLE);
        etCurrentPassword.setEnabled(false);
        etNewPassword.setEnabled(false);
        etNewPasswordConfirm.setEnabled(false);
        btnChangePassword.setEnabled(false);

        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPw);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        // 재인증 성공 → 비밀번호 업데이트
                        currentUser.updatePassword(newPw)
                                .addOnCompleteListener(updateTask -> {
                                    // 작업 후 로딩 숨김 및 입력란/버튼 활성화
                                    progressBar.setVisibility(View.GONE);
                                    etCurrentPassword.setEnabled(true);
                                    etNewPassword.setEnabled(true);
                                    etNewPasswordConfirm.setEnabled(true);
                                    btnChangePassword.setEnabled(true);

                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(MyPageActivity.this,
                                                "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                        // 입력란 초기화
                                        etCurrentPassword.setText("");
                                        etNewPassword.setText("");
                                        etNewPasswordConfirm.setText("");
                                    } else {
                                        Toast.makeText(MyPageActivity.this,
                                                "비밀번호 변경 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // 재인증 실패: 잘못된 현재 비밀번호
                        progressBar.setVisibility(View.GONE);
                        etCurrentPassword.setEnabled(true);
                        etNewPassword.setEnabled(true);
                        etNewPasswordConfirm.setEnabled(true);
                        btnChangePassword.setEnabled(true);

                        Toast.makeText(MyPageActivity.this,
                                "현재 비밀번호가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
