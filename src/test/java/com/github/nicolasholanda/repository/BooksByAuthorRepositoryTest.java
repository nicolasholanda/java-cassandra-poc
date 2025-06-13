package com.github.nicolasholanda.repository;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;
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

    @Test
    public void testFindByBookTitleWithAllowFilteringAndSecondaryIndex() {
        booksByAuthorRepository.deleteTable();
        booksByAuthorRepository.createTable();
        booksByAuthorRepository.createSecondaryIndexOnBookTitle();
        BooksByAuthor book1 = new BooksByAuthor("Author A", "id-1", "Title X");
        BooksByAuthor book2 = new BooksByAuthor("Author B", "id-2", "Title Y");
        BooksByAuthor book3 = new BooksByAuthor("Author C", "id-3", "Title X");
        booksByAuthorRepository.insert(book1);
        booksByAuthorRepository.insert(book2);
        booksByAuthorRepository.insert(book3);
        List<BooksByAuthor> found = booksByAuthorRepository.findByBookTitleWithAllowFiltering("Title X");
        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(b -> b.getAuthor().equals("Author A")));
        assertTrue(found.stream().anyMatch(b -> b.getAuthor().equals("Author C")));
    }

    @Test(expected = InvalidQueryException.class)
    public void testQueryByNonPrimaryKeyColumnWithoutAllowFilteringShouldFail() {
        booksByAuthorRepository.deleteTable();
        booksByAuthorRepository.createTable();
        BooksByAuthor book = new BooksByAuthor("Test Author", "id-999", "Special Title");
        booksByAuthorRepository.insert(book);
        session.execute("SELECT * FROM " + KEYSPACE_NAME + "." + TABLE_NAME + " WHERE book_title = 'Special Title';");
    }
}
