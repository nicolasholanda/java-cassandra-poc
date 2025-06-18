package com.github.nicolasholanda.model;

import java.util.UUID;

public class Book {
    private UUID id;
    private String author;
    private String title;
    private String subject;
    private Publisher publisher;

    public Book() {}

    public Book(UUID id, String author, String title, String subject) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.subject = subject;
    }

    public Book(UUID id, String author, String title, String subject, Publisher publisher) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.subject = subject;
        this.publisher = publisher;
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

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }
}
