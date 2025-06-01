package com.example.myapplication.model;

/**
 * 한 책(ISBN)에 대해 유저가 매긴 별점(rating: 0.0~5.0)과 코멘트(comment)를 저장하기 위한 데이터 클래스.
 */
public class RatingComment {
    private final float rating;
    private final String comment;

    public RatingComment(float rating, String comment) {
        this.rating = rating;
        this.comment = (comment != null) ? comment : "";
    }

    public float getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }
}
