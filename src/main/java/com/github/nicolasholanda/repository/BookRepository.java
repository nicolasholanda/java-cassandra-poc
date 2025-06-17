package com.github.nicolasholanda.repository;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.github.nicolasholanda.model.Book;
import java.util.List;
import java.util.UUID;

public class BookRepository {

    private static final String KEYSPACE = "library";
    private static final String TABLE_NAME = "books";
    private final Session session;

    public BookRepository(Session session) {
        this.session = session;
    }

    public void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS " +
                KEYSPACE + "." + TABLE_NAME + "(" +
                "id uuid PRIMARY KEY, " +
                "author text," +
                "title text," +
                "subject text);";
        session.execute(query);
    }

    public void deleteTable() {
        String query = "DROP TABLE IF EXISTS " + KEYSPACE + "." + TABLE_NAME + ";";
        session.execute(query);
    }

    public void insertBook(Book book) {
        String query = String.format("INSERT INTO %s.%s (id, author, title, subject) VALUES (?, ?, ?, ?);", KEYSPACE, TABLE_NAME);
        session.execute(session.prepare(query).bind(
                book.getId(),
                book.getAuthor(),
                book.getTitle(),
                book.getSubject()
        ));
    }

    public void insertBookWithTTL(Book book, int ttlSeconds) {
        String query = String.format("INSERT INTO %s.%s (id, author, title, subject) VALUES (?, ?, ?, ?) USING TTL ?;", KEYSPACE, TABLE_NAME);
        session.execute(session.prepare(query).bind(
                book.getId(),
                book.getAuthor(),
                book.getTitle(),
                book.getSubject(),
                ttlSeconds
        ));
    }

    public Book getBookById(UUID id) {
        String query = String.format("SELECT id, author, title, subject FROM %s.%s WHERE id = ?;", KEYSPACE, TABLE_NAME);
        Row row = session.execute(session.prepare(query).bind(id)).one();
        if (row == null) return null;
        return new Book(
                row.getUUID("id"),
                row.getString("author"),
                row.getString("title"),
                row.getString("subject")
        );
    }

    public void updateBook(Book book) {
        String query = String.format("UPDATE %s.%s SET author = ?, title = ?, subject = ? WHERE id = ?;", KEYSPACE, TABLE_NAME);
        session.execute(session.prepare(query).bind(
                book.getAuthor(),
                book.getTitle(),
                book.getSubject(),
                book.getId()
        ));
    }

    public void deleteBook(UUID id) {
        String query = String.format("DELETE FROM %s.%s WHERE id = ?;", KEYSPACE, TABLE_NAME);
        session.execute(session.prepare(query).bind(id));
    }

    public void insertBooksBatch(List<Book> books) {
        String query = String.format("INSERT INTO %s.%s (id, author, title, subject) VALUES (?, ?, ?, ?);", KEYSPACE, TABLE_NAME);
        PreparedStatement prepared = session.prepare(query);
        BatchStatement batch = new BatchStatement();
        for (Book book : books) {
            batch.add(prepared.bind(
                book.getId(),
                book.getAuthor(),
                book.getTitle(),
                book.getSubject()
            ));
        }
        session.execute(batch);
    }
}
