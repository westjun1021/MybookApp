// app/src/main/java/com/example/myapplication/network/RetrofitClient.java
package com.example.myapplication.network;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
// ScalarsConverterFactory import
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    // 실제 사용하는 백엔드 서버의 Base URL로 변경해주세요.
    private static final String BASE_URL = "https://www.nl.go.kr/NL/";
    private static Retrofit instance;

    public static Retrofit getInstance() {
        if (instance == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .build();

            // ScalarsConverterFactory를 먼저 추가 → Call<String> 등이 우선 처리됨
            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }
}
