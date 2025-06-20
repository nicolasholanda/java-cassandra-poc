package com.github.nicolasholanda.repository;

import com.datastax.driver.core.*;
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

    public void createSecondaryIndexOnBookTitle() {
        String query = String.format("CREATE INDEX IF NOT EXISTS idx_book_title ON %s.%s (book_title);", KEYSPACE, TABLE_NAME);
        session.execute(query);
    }

    public List<BooksByAuthor> findByBookTitleWithAllowFiltering(String bookTitle) {
        String query = String.format("SELECT author, book_id, book_title FROM %s.%s WHERE book_title = ? ALLOW FILTERING;", KEYSPACE, TABLE_NAME);
        List<BooksByAuthor> books = new ArrayList<>();
        for (Row row : session.execute(session.prepare(query).bind(bookTitle))) {
            books.add(new BooksByAuthor(
                    row.getString("author"),
                    row.getString("book_id"),
                    row.getString("book_title")
            ));
        }
        return books;
    }

    public List<BooksByAuthor> findByAuthorWithPaging(String author, int pageSize, String pagingState) {
        String query = String.format("SELECT author, book_id, book_title FROM %s.%s WHERE author = ?;", KEYSPACE, TABLE_NAME);
        PreparedStatement prepared = session.prepare(query);
        BoundStatement bound = prepared.bind(author);
        bound.setFetchSize(pageSize);
        if (pagingState != null) {
            bound.setPagingState(PagingState.fromString(pagingState));
        }
        ResultSet rs = session.execute(bound);
        List<BooksByAuthor> books = new ArrayList<>();
        int remaining = rs.getAvailableWithoutFetching();
        for (Row row : rs) {
            books.add(new BooksByAuthor(
                    row.getString("author"),
                    row.getString("book_id"),
                    row.getString("book_title")
            ));
            if (--remaining == 0) {
                break;
            }
        }
        return books;
    }
}

