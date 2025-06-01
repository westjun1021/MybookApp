package com.example.myapplication.model;

import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

public class Book {
    private final String title;
    private final String author;
    private final String year;
    private final String isbn;
    private final String coverUrl;

    public Book(String title, String author, String year, String isbn) {
        // null 안전성 검사
        this.title  = (title  != null) ? title  : "";
        this.author = (author != null) ? author : "";
        this.year   = (year   != null) ? year   : "";
        this.isbn   = (isbn   != null) ? isbn.trim() : "";

        if (!this.isbn.isEmpty()) {
            this.coverUrl = "https://covers.openlibrary.org/b/isbn/"
                    + this.isbn + "-L.jpg";
        } else {
            this.coverUrl = "";
        }
    }

    public String getTitle()    { return title; }
    public String getAuthor()   { return author; }
    public String getYear()     { return year; }
    public String getIsbn()     { return isbn; }
    public String getCoverUrl() { return coverUrl; }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Book))
            return false;
        Book b = (Book) o;
        return title.equals(b.title)
                && author.equals(b.author)
                && year.equals(b.year)
                && isbn.equals(b.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, author, year, isbn);
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("title", title);
            obj.put("author", author);
            obj.put("year", year);
            obj.put("isbn", isbn);
            obj.put("coverUrl", coverUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static Book fromJson(JSONObject obj) {
        String title  = obj.optString("title", "");
        String author = obj.optString("author", "");
        String year   = obj.optString("year", "");
        String isbn   = obj.optString("isbn", "");
        return new Book(title, author, year, isbn);
    }
}
