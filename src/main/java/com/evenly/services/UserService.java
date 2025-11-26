package com.evenly.services;

import com.evenly.models.User;
import java.sql.*;

public class UserService {
  private static final String DB_URL = "jdbc:sqlite:evenly.db";

  public User loginOrRegister(String name, String email) throws SQLException {
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      // Check if user exists
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

      // Register new user
      String insert = "INSERT INTO users (name, email) VALUES (?, ?)";
      try (PreparedStatement pstmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
        pstmt.setString(1, name);
        pstmt.setString(2, email);
        pstmt.executeUpdate();

        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            return new User(generatedKeys.getInt(1), name, email, null);
          }
        }
      }
    }
    throw new SQLException("Failed to login or register user.");
  }
}
