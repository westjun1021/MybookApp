// app/src/main/java/com/example/myapplication/network/LibraryApiService.java

package com.example.myapplication.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LibraryApiService {

    /**
     * DBpia(또는 openApi) 도서 검색: 원래 사용하던 9개 파라미터 버전으로 되돌렸습니다.
     */
    @GET("search/openApi/search.do")
    Call<String> searchBooksRaw(
            @Query("key") String key,
            @Query("apiType") String apiType,
            @Query("srchTarget") String srchTarget,
            @Query("kwd") String kwd,
            @Query("pageNum") int pageNum,
            @Query("pageSize") int pageSize,
            @Query("sort") String sort,
            @Query("category") String category, // encoded=true 옵션 제거
            @Query("licYn") String licYn
    );
}
