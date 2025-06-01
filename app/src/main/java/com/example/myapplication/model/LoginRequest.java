package com.example.myapplication.model;

/** 로그인 요청 바디에 들어가는 객체 */
public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest(String email, String password) {
        this.email    = email;
        this.password = password;
    }

    // Getter (Retrofit/Gson이 JSON 직렬화할 때 필요)
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
}
