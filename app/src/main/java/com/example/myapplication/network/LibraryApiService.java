package com.example.myapplication.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LibraryApiService {
    @GET("searchKolisNet.do")
    Call<String> searchBooksRaw(
            @Query("key") String apiKey,
            @Query("kwd") String keyword,
            @Query("apiType") String apiType,
            @Query("pageNum") int pageNum,
            @Query("pageSize") int pageSize,
            @Query("sort") String sort,
            @Query("desc") String desc
    );
}
