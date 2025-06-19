# Java Cassandra POC

A hands-on exploration of Apache Cassandra features using Java. This project demonstrates various Cassandra capabilities through practical examples and tests.

## What's Inside

This POC showcases several key Cassandra features:

- **User-Defined Types (UDTs)** - Complex nested data structures
- **Materialized Views** - Efficient querying with automatic data synchronization  
- **Lightweight Transactions (LWT)** - Distributed consistency with conditional operations
- **TTL (Time To Live)** - Automatic data expiration
- **Batch Operations** - Atomic multi-record operations
- **Secondary Indexes** - Alternative query patterns
- **Paging** - Handling large result sets

## Getting Started

### Prerequisites
- Java 8+
- Maven
- Docker (for TestContainers)

### Running the Project

1. Clone the repository
2. Run the tests to see Cassandra features in action:
   ```bash
   mvn test
   ```

The tests use TestContainers to spin up a Cassandra instance automatically, so you don't need to install Cassandra locally.

## Project Structure

- `src/main/java/com/github/nicolasholanda/model/` - Data models including UDTs
- `src/main/java/com/github/nicolasholanda/repository/` - Cassandra operations and queries
- `src/test/java/` - Feature demonstrations and tests

## Key Features Demonstrated

### User-Defined Types
See how to create complex nested structures like a `Publisher` with name and address fields.

### Materialized Views  
Learn how to create efficient query patterns without manual data synchronization.

### Lightweight Transactions
Explore distributed consistency with conditional inserts and updates using `IF EXISTS` and `IF NOT EXISTS`.

## Why This Matters

Cassandra is designed differently from traditional relational databases. This POC helps you understand:
- How to model data for specific query patterns
- When to use different Cassandra features
- The trade-offs between consistency, performance, and complexity

Perfect for developers getting started with Cassandra or wanting to explore its advanced features! 