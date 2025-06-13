package com.github.nicolasholanda.service;

import com.github.nicolasholanda.model.Book;
import com.github.nicolasholanda.model.BooksByAuthor;
import com.github.nicolasholanda.repository.BookRepository;
import com.github.nicolasholanda.service.BooksByAuthorService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BookServiceTest {
    private BookRepository bookRepository;
    private BooksByAuthorService booksByAuthorService;
    private BookService bookService;

    @Before
    public void setUp() {
        bookRepository = mock(BookRepository.class);
        booksByAuthorService = mock(BooksByAuthorService.class);
        bookService = new BookService(bookRepository, booksByAuthorService);
    }

    @Test
    public void testAddBook_ShouldInsertBookAndBooksByAuthor() {
        UUID id = UUID.randomUUID();
        Book book = new Book(id, "Author Test", "Title Test", "Subject Test");

        bookService.addBook(book);

        verify(bookRepository, times(1)).insertBook(book);
        ArgumentCaptor<BooksByAuthor> captor = ArgumentCaptor.forClass(BooksByAuthor.class);
        verify(booksByAuthorService, times(1)).addBookByAuthor(captor.capture());
        BooksByAuthor booksByAuthor = captor.getValue();
        assertEquals("Author Test", booksByAuthor.getAuthor());
        assertEquals(id.toString(), booksByAuthor.getBookId());
        assertEquals("Title Test", booksByAuthor.getBookTitle());
    }
}

