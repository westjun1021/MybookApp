// app/src/main/java/com/example/myapplication/model/BookRatingAggregate.java
package com.example.myapplication.model;

/**
 * Firestore의 books/{isbn} 문서에 저장되는
 * ratingSum과 ratingCount 필드를 담는 DTO 클래스
 */
public class BookRatingAggregate {
    private double ratingSum;
    private long ratingCount;

    // Firestore 리플렉션용 빈 생성자
    public BookRatingAggregate() {}

    public BookRatingAggregate(double ratingSum, long ratingCount) {
        this.ratingSum = ratingSum;
        this.ratingCount = ratingCount;
    }

    public double getRatingSum() {
        return ratingSum;
    }

    public long getRatingCount() {
        return ratingCount;
    }
}
