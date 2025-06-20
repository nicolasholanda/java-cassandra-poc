package com.github.nicolasholanda.repository;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.github.nicolasholanda.model.Book;
import com.github.nicolasholanda.model.Publisher;
import com.datastax.driver.core.UDTValue;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import com.datastax.driver.core.ResultSet;

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
                "subject text," +
                "publisher frozen<publisher>);";
        session.execute(query);
    }

    public void deleteTable() {
        String query = "DROP TABLE IF EXISTS " + KEYSPACE + "." + TABLE_NAME + ";";
        session.execute(query);
    }

    public void createPublisherUDT() {
        String query = "CREATE TYPE IF NOT EXISTS " + KEYSPACE + ".publisher (name text, address text);";
        session.execute(query);
    }

    public void insertBook(Book book) {
        String query = String.format("INSERT INTO %s.%s (id, author, title, subject, publisher) VALUES (?, ?, ?, ?, ?);", KEYSPACE, TABLE_NAME);
        UDTValue publisherUDT = null;
        if (book.getPublisher() != null) {
            publisherUDT = session.getCluster().getMetadata().getKeyspace(KEYSPACE).getUserType("publisher").newValue()
                .setString("name", book.getPublisher().getName())
                .setString("address", book.getPublisher().getAddress());
        }
        session.execute(session.prepare(query).bind(
                book.getId(),
                book.getAuthor(),
                book.getTitle(),
                book.getSubject(),
                publisherUDT
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
        String query = String.format("SELECT id, author, title, subject, publisher FROM %s.%s WHERE id = ?;", KEYSPACE, TABLE_NAME);
        Row row = session.execute(session.prepare(query).bind(id)).one();
        if (row == null) return null;
        Publisher publisher = null;
        UDTValue publisherUDT = row.getUDTValue("publisher");
        if (publisherUDT != null) {
            publisher = new Publisher(
                publisherUDT.getString("name"),
                publisherUDT.getString("address")
            );
        }
        return new Book(
                row.getUUID("id"),
                row.getString("author"),
                row.getString("title"),
                row.getString("subject"),
                publisher
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
        String query = String.format("INSERT INTO %s.%s (id, author, title, subject, publisher) VALUES (?, ?, ?, ?, ?);", KEYSPACE, TABLE_NAME);
        PreparedStatement prepared = session.prepare(query);
        BatchStatement batch = new BatchStatement();
        for (Book book : books) {
            UDTValue publisherUDT = null;
            if (book.getPublisher() != null) {
                publisherUDT = session.getCluster().getMetadata().getKeyspace(KEYSPACE).getUserType("publisher").newValue()
                    .setString("name", book.getPublisher().getName())
                    .setString("address", book.getPublisher().getAddress());
            }
            batch.add(prepared.bind(
                book.getId(),
                book.getAuthor(),
                book.getTitle(),
                book.getSubject(),
                publisherUDT
            ));
        }
        session.execute(batch);
    }

    public void createMaterializedViewByTitle() {
        String query = "CREATE MATERIALIZED VIEW IF NOT EXISTS " + KEYSPACE + ".books_by_title AS " +
                "SELECT id, author, title, subject, publisher " +
                "FROM " + KEYSPACE + "." + TABLE_NAME + " " +
                "WHERE title IS NOT NULL AND id IS NOT NULL " +
                "PRIMARY KEY (title, id);";
        session.execute(query);
    }

    public void deleteMaterializedViewByTitle() {
        String query = "DROP MATERIALIZED VIEW IF EXISTS " + KEYSPACE + ".books_by_title;";
        session.execute(query);
    }

    public List<Book> findByTitleUsingMaterializedView(String title) {
        String query = "SELECT id, author, title, subject, publisher FROM " + KEYSPACE + ".books_by_title WHERE title = ?;";
        List<Book> books = new ArrayList<>();
        for (Row row : session.execute(session.prepare(query).bind(title))) {
            Publisher publisher = null;
            UDTValue publisherUDT = row.getUDTValue("publisher");
            if (publisherUDT != null) {
                publisher = new Publisher(
                    publisherUDT.getString("name"),
                    publisherUDT.getString("address")
                );
            }
            books.add(new Book(
                row.getUUID("id"),
                row.getString("author"),
                row.getString("title"),
                row.getString("subject"),
                publisher
            ));
        }
        return books;
    }

    public boolean insertBookIfNotExists(Book book) {
        String query = String.format("INSERT INTO %s.%s (id, author, title, subject, publisher) VALUES (?, ?, ?, ?, ?) IF NOT EXISTS;", KEYSPACE, TABLE_NAME);
        UDTValue publisherUDT = null;
        if (book.getPublisher() != null) {
            publisherUDT = session.getCluster().getMetadata().getKeyspace(KEYSPACE).getUserType("publisher").newValue()
                .setString("name", book.getPublisher().getName())
                .setString("address", book.getPublisher().getAddress());
        }
        ResultSet result = session.execute(session.prepare(query).bind(
                book.getId(),
                book.getAuthor(),
                book.getTitle(),
                book.getSubject(),
                publisherUDT
        ));
        return result.one().getBool("[applied]");
    }

    public boolean updateBookIfExists(Book book) {
        String query = String.format("UPDATE %s.%s SET author = ?, title = ?, subject = ?, publisher = ? WHERE id = ? IF EXISTS;", KEYSPACE, TABLE_NAME);
        UDTValue publisherUDT = null;
        if (book.getPublisher() != null) {
            publisherUDT = session.getCluster().getMetadata().getKeyspace(KEYSPACE).getUserType("publisher").newValue()
                .setString("name", book.getPublisher().getName())
                .setString("address", book.getPublisher().getAddress());
        }
        ResultSet result = session.execute(session.prepare(query).bind(
                book.getAuthor(),
                book.getTitle(),
                book.getSubject(),
                publisherUDT,
                book.getId()
        ));
        return result.one().getBool("[applied]");
    }
}
