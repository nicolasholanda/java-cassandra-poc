package com.github.nicolasholanda.repository;

import com.datastax.driver.core.Session;

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
}
