package com.github.nicolasholanda.model;

import java.util.UUID;

public class Book {
    private UUID id;
    private String author;
    private String title;
    private String subject;

    public Book() {}

    public Book(UUID id, String author, String title, String subject) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.subject = subject;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
