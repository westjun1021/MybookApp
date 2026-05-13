package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

/**
 * 설정 화면: 다크 모드 토글 기능 구현
 * - 앱 실행 시 SharedPreferences에 저장된 테마를 즉시 적용합니다.
 * - SwitchCompat(switchDarkMode)를 통해 다크/라이트 테마를 전환하고, 변경된 값을 SharedPreferences에 저장합니다.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME     = "prefs";
    private static final String KEY_DARK_MODE  = "dark_mode";

    private SwitchCompat switchDarkMode;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // (1) SharedPreferences에서 저장된 다크 모드 여부를 읽어와서 테마를 먼저 설정합니다.
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkModeOn = prefs.getBoolean(KEY_DARK_MODE, false);
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // (2) 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("설정");
            // 뒤로 가기(up 버튼) 활성화
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // (3) SwitchCompat 참조 및 초기 상태 반영
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchDarkMode.setChecked(isDarkModeOn);

        // (4) 스위치 상태가 바뀔 때마다 SharedPreferences에 저장하고, 테마를 즉시 변경합니다.
        switchDarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // SharedPreferences에 변경된 상태 저장
                prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();

                if (isChecked) {
                    // 다크 모드로 설정
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    // 라이트 모드로 설정
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                // 변경 사항을 즉시 반영하기 위해 액티비티를 재생성
                recreate();
            }
        });
    }

    // 툴바 뒤로가기 버튼을 눌렀을 때 이 액티비티를 종료합니다.
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
