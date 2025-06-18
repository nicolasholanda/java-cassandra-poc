package com.github.nicolasholanda.repository;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.github.nicolasholanda.config.CassandraConnector;
import com.github.nicolasholanda.model.Book;
import com.github.nicolasholanda.model.Publisher;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.cassandra.CassandraContainer;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class BookRepositoryTest {

    private Session session;
    private BookRepository bookRepository;
    private KeyspaceRepository keyspaceRepository;
    private static CassandraContainer cassandraContainer;

    private static final String KEYSPACE_NAME = "library";
    private static final String TABLE_NAME = "books";

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
        ResultSet result = session.execute("SELECT * FROM " + KEYSPACE_NAME + "." + TABLE_NAME + ";");

        List<String> columnNames = result.getColumnDefinitions().asList().stream()
                .map(ColumnDefinitions.Definition::getName).toList();
        assertEquals(4, columnNames.size());
        assertTrue(columnNames.contains("id"));
        assertTrue(columnNames.contains("title"));
        assertTrue(columnNames.contains("author"));
        assertTrue(columnNames.contains("subject"));
    }

    @Test
    public void whenInsertingBookWithTTL_thenExpiresAfterTTL() throws InterruptedException {
        bookRepository.deleteTable();
        bookRepository.createTable();
        Book book = new Book(java.util.UUID.randomUUID(), "Author TTL", "Book TTL", "Test TTL");
        bookRepository.insertBookWithTTL(book, 2);
        Book found = bookRepository.getBookById(book.getId());
        assertNotNull(found);
        Thread.sleep(3000);
        Book expired = bookRepository.getBookById(book.getId());
        assertNull(expired);
    }

    @Test
    public void testInsertBooksBatch() {
        bookRepository.deleteTable();
        bookRepository.createTable();
        Book book1 = new Book(UUID.randomUUID(), "Author 1", "Title 1", "Subject 1");
        Book book2 = new Book(UUID.randomUUID(), "Author 2", "Title 2", "Subject 2");
        Book book3 = new Book(UUID.randomUUID(), "Author 3", "Title 3", "Subject 3");
        bookRepository.insertBooksBatch(Arrays.asList(book1, book2, book3));
        Book found1 = bookRepository.getBookById(book1.getId());
        Book found2 = bookRepository.getBookById(book2.getId());
        Book found3 = bookRepository.getBookById(book3.getId());
        assertNotNull(found1);
        assertNotNull(found2);
        assertNotNull(found3);
        assertEquals(book1.getTitle(), found1.getTitle());
        assertEquals(book2.getTitle(), found2.getTitle());
        assertEquals(book3.getTitle(), found3.getTitle());
    }

    @Test
    public void whenInsertingAndQueryingBookWithPublisher_thenWorksCorrectly() {
        bookRepository.deleteTable();
        bookRepository.createPublisherUDT();
        bookRepository.createTable();
        Publisher publisher = new Publisher("Test Publisher", "123 Main St");
        Book book = new Book(UUID.randomUUID(), "Author UDT", "Book Title UDT", "Subject UDT", publisher);
        bookRepository.insertBook(book);
        Book found = bookRepository.getBookById(book.getId());
        assertNotNull(found);
        assertEquals("Author UDT", found.getAuthor());
        assertEquals("Book Title UDT", found.getTitle());
        assertEquals("Subject UDT", found.getSubject());
        assertNotNull(found.getPublisher());
        assertEquals("Test Publisher", found.getPublisher().getName());
        assertEquals("123 Main St", found.getPublisher().getAddress());
    }

    @Test
    public void whenUsingMaterializedView_thenCanQueryByTitleEfficiently() {
        bookRepository.deleteTable();
        bookRepository.deleteMaterializedViewByTitle();
        bookRepository.createPublisherUDT();
        bookRepository.createTable();
        bookRepository.createMaterializedViewByTitle();
        
        Publisher publisher1 = new Publisher("Publisher A", "Address A");
        Publisher publisher2 = new Publisher("Publisher B", "Address B");
        
        Book book1 = new Book(UUID.randomUUID(), "Author 1", "Same Title", "Subject 1", publisher1);
        Book book2 = new Book(UUID.randomUUID(), "Author 2", "Same Title", "Subject 2", publisher2);
        Book book3 = new Book(UUID.randomUUID(), "Author 3", "Different Title", "Subject 3", null);
        
        bookRepository.insertBook(book1);
        bookRepository.insertBook(book2);
        bookRepository.insertBook(book3);
        
        List<Book> booksWithSameTitle = bookRepository.findByTitleUsingMaterializedView("Same Title");
        assertEquals(2, booksWithSameTitle.size());
        
        assertTrue(booksWithSameTitle.stream().anyMatch(b -> b.getAuthor().equals("Author 1")));
        assertTrue(booksWithSameTitle.stream().anyMatch(b -> b.getAuthor().equals("Author 2")));
        assertTrue(booksWithSameTitle.stream().allMatch(b -> b.getTitle().equals("Same Title")));
        
        List<Book> booksWithDifferentTitle = bookRepository.findByTitleUsingMaterializedView("Different Title");
        assertEquals(1, booksWithDifferentTitle.size());
        assertEquals("Author 3", booksWithDifferentTitle.get(0).getAuthor());
    }
}
