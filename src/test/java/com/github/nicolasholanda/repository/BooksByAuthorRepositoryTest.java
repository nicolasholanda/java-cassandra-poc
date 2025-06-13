package com.github.nicolasholanda.repository;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.github.nicolasholanda.config.CassandraConnector;
import com.github.nicolasholanda.model.BooksByAuthor;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.cassandra.CassandraContainer;

import java.util.List;

import static org.junit.Assert.*;

public class BooksByAuthorRepositoryTest {
    private Session session;
    private BooksByAuthorRepository booksByAuthorRepository;
    private KeyspaceRepository keyspaceRepository;
    private static CassandraContainer cassandraContainer;

    private static final String KEYSPACE_NAME = "library";
    private static final String TABLE_NAME = "books_by_author";

    @Before
    public void connect() {
        if (cassandraContainer == null) {
            cassandraContainer = new CassandraContainer("cassandra:3.11.2");
            cassandraContainer.start();
        }
        CassandraConnector client = new CassandraConnector();
        client.connect(cassandraContainer.getHost(), cassandraContainer.getFirstMappedPort());
        this.session = client.getSession();
        booksByAuthorRepository = new BooksByAuthorRepository(session);
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
        booksByAuthorRepository.deleteTable();
        booksByAuthorRepository.createTable();
        ResultSet result = session.execute("SELECT * FROM " + KEYSPACE_NAME + "." + TABLE_NAME + ";");
        List<String> columnNames = result.getColumnDefinitions().asList().stream()
                .map(ColumnDefinitions.Definition::getName).toList();
        assertTrue(columnNames.contains("author"));
        assertTrue(columnNames.contains("book_id"));
        assertTrue(columnNames.contains("book_title"));
    }

    @Test
    public void whenInsertingAndQueryingBooksByAuthor_thenWorksCorrectly() {
        booksByAuthorRepository.deleteTable();
        booksByAuthorRepository.createTable();
        BooksByAuthor book = new BooksByAuthor("Author Test", "id-123", "Book Title Test");
        booksByAuthorRepository.insert(book);
        List<BooksByAuthor> records = booksByAuthorRepository.findByAuthor("Author Test");
        assertEquals(1, records.size());

        BooksByAuthor first = records.getFirst();
        assertEquals("Author Test", first.getAuthor());
        assertEquals("id-123", first.getBookId());
        assertEquals("Book Title Test", first.getBookTitle());
    }
}

