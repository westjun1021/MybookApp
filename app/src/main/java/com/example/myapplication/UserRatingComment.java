// app/src/main/java/com/example/myapplication/model/UserRatingComment.java
package com.example.myapplication.model;

/**
 * Firestore: books/{isbn}/userRatings/{userId} 문서에 저장되는
 * 필드: rating, comment, userName(닉네임)
 */
public class UserRatingComment {
    private double rating;
    private String comment;
    private String userName;   // ★ 새로 추가: 댓글 작성자의 닉네임

    // Firestore 리플렉션용 빈 생성자 (반드시 필요)
    public UserRatingComment() {}

    public UserRatingComment(double rating, String comment, String userName) {
        this.rating = rating;
        this.comment = comment;
        this.userName = userName;
    }

    public double getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getUserName() {
        return userName;
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 댓글 목록을 RecyclerView에 표시할 때 사용할 내부 static 클래스
    public static class Item {
        public final String userName;   // ★ 닉네임을 저장
        public final double rating;
        public final String comment;

        public Item(String userName, double rating, String comment) {
            this.userName = userName;
            this.rating = rating;
            this.comment = comment;
        }
    }
}
