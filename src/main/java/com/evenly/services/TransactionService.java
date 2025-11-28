package com.evenly.services;

import com.evenly.models.Transaction;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {
  private static final String DB_URL = "jdbc:sqlite:evenly.db";

  public Transaction createTransaction(int groupId, int payerId, String description, double amount, String currency)
      throws SQLException {
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      String insert = "INSERT INTO transactions (description, amount, currency, payer_id, group_id) VALUES (?, ?, ?, ?, ?)";
      try (PreparedStatement pstmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
        pstmt.setString(1, description);
        pstmt.setDouble(2, amount);
        pstmt.setString(3, currency);
        pstmt.setInt(4, payerId);
        pstmt.setInt(5, groupId);
        pstmt.executeUpdate();

        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            return new Transaction(
                generatedKeys.getInt(1),
                description,
                amount,
                currency,
                payerId,
                groupId,
                LocalDateTime.now());
          }
        }
      }
    }
    throw new SQLException("Failed to create transaction.");
  }

  public List<Transaction> getTransactionsByGroup(int groupId) throws SQLException {
    List<Transaction> transactions = new ArrayList<>();
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      String query = "SELECT id, description, amount, currency, payer_id, group_id FROM transactions WHERE group_id = ? ORDER BY id DESC";
      try (PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setInt(1, groupId);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
          transactions.add(new Transaction(
              rs.getInt("id"),
              rs.getString("description"),
              rs.getDouble("amount"),
              rs.getString("currency"),
              rs.getInt("payer_id"),
              rs.getInt("group_id"),
              LocalDateTime.now() // Using current time as placeholder since schema doesn't have timestamp
          ));
        }
      }
    }
    return transactions;
  }
}
