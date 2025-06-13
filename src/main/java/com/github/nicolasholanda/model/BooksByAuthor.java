package com.github.nicolasholanda.model;

public class BooksByAuthor {
    private String author;
    private String bookId;
    private String bookTitle;

    public BooksByAuthor(String author, String bookId, String bookTitle) {
        this.author = author;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
}
