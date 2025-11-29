package com.evenly.services;

import com.evenly.models.SplitStrategy;
import java.sql.*;

public class SplitStrategyService {
  private static final String DB_URL = "jdbc:sqlite:evenly.db";

  public SplitStrategy createSplitStrategy(String type, String params) throws SQLException {
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      String insert = "INSERT INTO split_strategies (type, params) VALUES (?, ?)";
      try (PreparedStatement pstmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
        pstmt.setString(1, type);
        pstmt.setString(2, params);
        pstmt.executeUpdate();

        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            return new SplitStrategy(
                generatedKeys.getInt(1),
                type,
                params);
          }
        }
      }
    }
    throw new SQLException("Failed to create split strategy.");
  }
}
