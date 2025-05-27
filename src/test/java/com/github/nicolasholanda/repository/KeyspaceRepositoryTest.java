package com.github.nicolasholanda.repository;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.github.nicolasholanda.config.CassandraConnector;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class KeyspaceRepositoryTest {

    private Session session;
    private KeyspaceRepository schemaRepository;

    @Before
    public void connect() {
        CassandraConnector client = new CassandraConnector();
        client.connect("127.0.0.1", 9142);
        session = client.getSession();
        schemaRepository = new KeyspaceRepository(session);
    }

    @Test
    public void whenCreatingAKeyspace_thenCreated() {
        String keyspaceName = "library";
        schemaRepository.createKeyspace(keyspaceName, "SimpleStrategy", 1);

        ResultSet result = session.execute("SELECT * FROM system_schema.keyspaces;");

        List<String> matchedKeyspaces = result.all()
                .stream()
                .filter(r -> r.getString(0).equals(keyspaceName.toLowerCase()))
                .map(r -> r.getString(0))
                .toList();

        assertEquals(1, matchedKeyspaces.size());
        assertEquals(keyspaceName.toLowerCase(), matchedKeyspaces.getFirst());
    }
}
