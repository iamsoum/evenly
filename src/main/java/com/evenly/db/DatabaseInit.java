package com.evenly.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseInit {

  private static final String DB_URL = "jdbc:sqlite:evenly.db";
  private static final String SCHEMA_FILE = "/schema.sql";

  public static void main(String[] args) {
    System.out.println("Initializing database...");
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      if (conn != null) {
        System.out.println("Connected to SQLite database.");
        initializeSchema(conn);
      }
    } catch (SQLException e) {
      System.err.println("Database connection error: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void initializeSchema(Connection conn) {
    try (InputStream inputStream = DatabaseInit.class.getResourceAsStream(SCHEMA_FILE)) {
      if (inputStream == null) {
        System.err.println("Schema file not found: " + SCHEMA_FILE);
        return;
      }

      String schemaSql;
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
        schemaSql = reader.lines().collect(Collectors.joining("\n"));
      }

      // Split by semicolon to execute statements individually, as SQLite JDBC might
      // not support bulk execution well in one go
      String[] statements = schemaSql.split(";");

      try (Statement stmt = conn.createStatement()) {
        for (String sql : statements) {
          if (!sql.trim().isEmpty()) {
            stmt.execute(sql);
          }
        }
        System.out.println("Database schema initialized successfully.");
      }

    } catch (IOException e) {
      System.err.println("Error reading schema file: " + e.getMessage());
      e.printStackTrace();
    } catch (SQLException e) {
      System.err.println("Error executing schema SQL: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
