package com.evenly.services;

import com.evenly.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MembershipService {
  private static final String DB_URL = "jdbc:sqlite:evenly.db";

  public List<User> getMembersOfGroup(int groupId) throws SQLException {
    List<User> members = new ArrayList<>();
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      String query = "SELECT u.id, u.name, u.email FROM users u " +
          "JOIN memberships m ON u.id = m.user_id " +
          "WHERE m.group_id = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setInt(1, groupId);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
          members.add(new User(
              rs.getInt("id"),
              rs.getString("name"),
              rs.getString("email"),
              null // defaultGroupId
          ));
        }
      }
    }
    return members;
  }

  public void addMemberToGroup(int groupId, int userId) throws SQLException {
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      String insert = "INSERT OR IGNORE INTO memberships (user_id, group_id) VALUES (?, ?)";
      try (PreparedStatement pstmt = conn.prepareStatement(insert)) {
        pstmt.setInt(1, userId);
        pstmt.setInt(2, groupId);
        pstmt.executeUpdate();
      }
    }
  }

  public User getUserByEmail(String email) throws SQLException {
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      String query = "SELECT id, name, email FROM users WHERE email = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setString(1, email);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          return new User(
              rs.getInt("id"),
              rs.getString("name"),
              rs.getString("email"),
              null);
        }
      }
    }
    return null;
  }
}
