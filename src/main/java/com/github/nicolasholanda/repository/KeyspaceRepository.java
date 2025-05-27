package com.github.nicolasholanda.repository;

import com.datastax.driver.core.Session;

public class KeyspaceRepository {

    private final Session session;

    public KeyspaceRepository(Session session) {
        this.session = session;
    }

    public void createKeyspace(String keyspaceName, String replicationStrategy, int replicationFactor) {
        String query = "CREATE KEYSPACE IF NOT EXISTS " +
                keyspaceName + " WITH replication = {" +
                "'class':'" + replicationStrategy +
                "','replication_factor':" + replicationFactor +
                "};";
        this.session.execute(query);
    }
}
