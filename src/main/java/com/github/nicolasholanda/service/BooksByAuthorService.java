package com.github.nicolasholanda.service;

import com.github.nicolasholanda.model.BooksByAuthor;
import com.github.nicolasholanda.repository.BooksByAuthorRepository;
import java.util.List;

public class BooksByAuthorService {
    private final BooksByAuthorRepository booksByAuthorRepository;

    public BooksByAuthorService(BooksByAuthorRepository booksByAuthorRepository) {
        this.booksByAuthorRepository = booksByAuthorRepository;
    }

    public void addBookByAuthor(BooksByAuthor booksByAuthor) {
        booksByAuthorRepository.insert(booksByAuthor);
    }

    public List<BooksByAuthor> getBooksByAuthor(String author) {
        return booksByAuthorRepository.findByAuthor(author);
    }

    public void createTable() {
        booksByAuthorRepository.createTable();
    }

    public void deleteTable() {
        booksByAuthorRepository.deleteTable();
    }
}


