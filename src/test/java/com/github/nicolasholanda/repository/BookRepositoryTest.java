package com.github.nicolasholanda.repository;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.github.nicolasholanda.config.CassandraConnector;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.cassandra.CassandraContainer;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BookRepositoryTest {

    private Session session;
    private BookRepository bookRepository;
    private KeyspaceRepository keyspaceRepository;
    private static CassandraContainer cassandraContainer;

    private static final String KEYSPACE_NAME = "library";
    private static final String BOOKS = "books";

    @Before
    public void connect() {
        if (cassandraContainer == null) {
            cassandraContainer = new CassandraContainer("cassandra:3.11.2");
            cassandraContainer.start();
        }
        CassandraConnector client = new CassandraConnector();
        client.connect(cassandraContainer.getHost(), cassandraContainer.getFirstMappedPort());
        this.session = client.getSession();
        bookRepository = new BookRepository(session);
        keyspaceRepository = new KeyspaceRepository(session);
        keyspaceRepository.createKeyspace(KEYSPACE_NAME, "SimpleStrategy", 1);
    }

    @AfterClass
    public static void tearDown() {
        if (cassandraContainer != null) {
            cassandraContainer.stop();
        }
    }

    @Test
    public void whenCreatingATable_thenCreatedCorrectly() {
        bookRepository.deleteTable();
        bookRepository.createTable();
        ResultSet result = session.execute("SELECT * FROM " + KEYSPACE_NAME + "." + BOOKS + ";");

        List<String> columnNames = result.getColumnDefinitions().asList().stream()
                .map(ColumnDefinitions.Definition::getName).toList();
        assertEquals(4, columnNames.size());
        assertTrue(columnNames.contains("id"));
        assertTrue(columnNames.contains("title"));
        assertTrue(columnNames.contains("author"));
        assertTrue(columnNames.contains("subject"));
    }
}
