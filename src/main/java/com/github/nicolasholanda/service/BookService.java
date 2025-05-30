package com.github.nicolasholanda.service;

import com.github.nicolasholanda.model.Book;
import com.github.nicolasholanda.repository.BookRepository;
import java.util.UUID;

public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public void addBook(Book book) {
        bookRepository.insertBook(book);
    }

    public Book getBook(UUID id) {
        return bookRepository.getBookById(id);
    }

    public void updateBook(Book book) {
        bookRepository.updateBook(book);
    }

    public void deleteBook(UUID id) {
        bookRepository.deleteBook(id);
    }
}

