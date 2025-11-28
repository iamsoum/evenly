package com.evenly.services;

import com.evenly.models.User;
import java.sql.*;

public class UserService {
  private static final String DB_URL = "jdbc:sqlite:evenly.db";

  public User login(String username, String password) throws SQLException {
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      String query = "SELECT id, name, email, default_group_id FROM users WHERE name = ? AND password = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          return new User(
              rs.getInt("id"),
              rs.getString("name"),
              rs.getString("email"),
              (Integer) rs.getObject("default_group_id"));
        }
      }
    }
    return null;
  }

  public User register(String username, String email, String password) throws SQLException {
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      // Check if username already exists
      String checkQuery = "SELECT id FROM users WHERE name = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          throw new SQLException("Username already exists");
        }
      }

      // Check if email already exists
      String checkEmailQuery = "SELECT id FROM users WHERE email = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(checkEmailQuery)) {
        pstmt.setString(1, email);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          throw new SQLException("Email already exists");
        }
      }

      // Register new user
      String insert = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
      try (PreparedStatement pstmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
        pstmt.setString(1, username);
        pstmt.setString(2, email);
        pstmt.setString(3, password);
        pstmt.executeUpdate();

        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            return new User(generatedKeys.getInt(1), username, email, null);
          }
        }
      }
    }
    throw new SQLException("Failed to register user.");
  }

  public User getUserByEmail(String email) throws SQLException {
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      String query = "SELECT id, name, email, default_group_id FROM users WHERE email = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setString(1, email);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          return new User(
              rs.getInt("id"),
              rs.getString("name"),
              rs.getString("email"),
              (Integer) rs.getObject("default_group_id"));
        }
      }
    }
    return null;
  }
}
