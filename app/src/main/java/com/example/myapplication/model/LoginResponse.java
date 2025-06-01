package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

/** 로그인 응답으로 받는 JSON 예시
 *  {
 *    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
 *    // 필요한 다른 필드가 있다면 여기에 추가
 *  }
 */
public class LoginResponse {
    @SerializedName("access_token")
    private String accessToken;

    // Getter
    public String getAccessToken() {
        return accessToken;
    }
}
