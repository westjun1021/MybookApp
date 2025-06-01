package com.example.myapplication.model;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * SharedPreferences에 “현재 사용자 자신의 RatingComment”를 저장/불러오는 헬퍼.
 * 책마다 1명(현재 사용자)만의 별점·코멘트를 로컬에 남깁니다.
 * Firestore에 업로드/삭제할 때 참고용으로 사용하며,
 * 실제 여러 사용자 간 데이터 공유는 Firestore로 처리합니다.
 *
 * prefs 이름: "rating_comments_prefs"
 * 키: ISBN별 JSON을 하나의 문자열에 담아 관리합니다.
 */
public class RatingCommentStorage {
    private static final String PREFS_NAME = "rating_comments_prefs";
    private static final String KEY_JSON  = "rating_comments_json";

    /** 모든 ISBN → RatingComment 맵을 불러옴 */
    private static Map<String, RatingComment> loadAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_JSON, null);
        Map<String, RatingComment> map = new HashMap<>();
        if (json == null || json.isEmpty()) return map;

        try {
            JSONObject root = new JSONObject(json);
            Iterator<String> keys = root.keys();
            while (keys.hasNext()) {
                String isbn = keys.next();
                JSONObject jo = root.getJSONObject(isbn);
                float rating = (float) jo.optDouble("rating", 0.0);
                String comment = jo.optString("comment", "");
                map.put(isbn, new RatingComment(rating, comment));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    /** 특정 ISBN의 내 댓글(별점+코멘트)을 가져옴 */
    public static RatingComment get(Context context, String isbn) {
        if (isbn == null || isbn.isEmpty()) return null;
        Map<String, RatingComment> map = loadAll(context);
        return map.get(isbn);
    }

    /** 특정 ISBN의 내 댓글(별점+코멘트)을 저장 또는 삭제 (ratingComment==null인 경우 삭제) */
    public static void put(Context context, String isbn, RatingComment ratingComment) {
        if (isbn == null) return;
        Map<String, RatingComment> map = loadAll(context);
        if (ratingComment == null) {
            map.remove(isbn);
        } else {
            map.put(isbn, ratingComment);
        }
        JSONObject root = new JSONObject();
        try {
            for (Map.Entry<String, RatingComment> entry : map.entrySet()) {
                String key = entry.getKey();
                RatingComment rc = entry.getValue();
                JSONObject jo = new JSONObject();
                jo.put("rating", rc.getRating());
                jo.put("comment", rc.getComment());
                root.put(key, jo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_JSON, root.toString()).apply();
    }
}
