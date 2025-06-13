package com.github.nicolasholanda.service;

import com.github.nicolasholanda.model.Book;
import com.github.nicolasholanda.model.BooksByAuthor;
import com.github.nicolasholanda.repository.BookRepository;

import java.util.UUID;

public class BookService {
    private final BookRepository bookRepository;
    private final BooksByAuthorService booksByAuthorService;

    public BookService(BookRepository bookRepository, BooksByAuthorService booksByAuthorService) {
        this.bookRepository = bookRepository;
        this.booksByAuthorService = booksByAuthorService;
    }

    public void addBook(Book book) {
        bookRepository.insertBook(book);
        BooksByAuthor booksByAuthor = new BooksByAuthor(
            book.getAuthor(),
            book.getId().toString(),
            book.getTitle()
        );
        booksByAuthorService.addBookByAuthor(booksByAuthor);
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
