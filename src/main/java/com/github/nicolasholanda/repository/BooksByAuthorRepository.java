package com.github.nicolasholanda.repository;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.github.nicolasholanda.model.BooksByAuthor;
import java.util.ArrayList;
import java.util.List;

public class BooksByAuthorRepository {
    private static final String KEYSPACE = "library";
    private static final String TABLE_NAME = "books_by_author";
    private final Session session;

    public BooksByAuthorRepository(Session session) {
        this.session = session;
    }

    public void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS " +
                KEYSPACE + "." + TABLE_NAME + "(" +
                "author text, " +
                "book_id text, " +
                "book_title text, " +
                "PRIMARY KEY (author, book_id));";
        session.execute(query);
    }

    public void deleteTable() {
        String query = "DROP TABLE IF EXISTS " + KEYSPACE + "." + TABLE_NAME + ";";
        session.execute(query);
    }

    public void insert(BooksByAuthor booksByAuthor) {
        String query = String.format("INSERT INTO %s.%s (author, book_id, book_title) VALUES (?, ?, ?);", KEYSPACE, TABLE_NAME);
        session.execute(session.prepare(query).bind(
                booksByAuthor.getAuthor(),
                booksByAuthor.getBookId(),
                booksByAuthor.getBookTitle()
        ));
    }

    public List<BooksByAuthor> findByAuthor(String author) {
        String query = String.format("SELECT author, book_id, book_title FROM %s.%s WHERE author = ?;", KEYSPACE, TABLE_NAME);
        List<BooksByAuthor> books = new ArrayList<>();
        for (Row row : session.execute(session.prepare(query).bind(author))) {
            books.add(new BooksByAuthor(
                    row.getString("author"),
                    row.getString("book_id"),
                    row.getString("book_title")
            ));
        }
        return books;
    }
}
