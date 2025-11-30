package com.evenly.services;

import com.evenly.models.Group;
import com.evenly.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupService {
  private static final String DB_URL = "jdbc:sqlite:evenly.db";

  public Group createGroup(String name, String description) throws SQLException {
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      String insert = "INSERT INTO groups (name, description, expense, expense_currency) VALUES (?, ?, 0.0, 'CAD')";
      try (PreparedStatement pstmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
        pstmt.setString(1, name);
        pstmt.setString(2, description);
        pstmt.executeUpdate();

        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            return new Group(generatedKeys.getInt(1), name, 0.0, "CAD", description, "📁");
          }
        }
      }
    }
    throw new SQLException("Failed to create group.");
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

  public void addMemberToGroup(int groupId, int userId) throws SQLException {
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      String insert = "INSERT INTO memberships (user_id, group_id) VALUES (?, ?)";
      try (PreparedStatement pstmt = conn.prepareStatement(insert)) {
        pstmt.setInt(1, userId);
        pstmt.setInt(2, groupId);
        pstmt.executeUpdate();
      }
    }
  }

  public List<User> getGroupMembers(int groupId) throws SQLException {
    List<User> members = new ArrayList<>();
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      String query = "SELECT u.id, u.name, u.email, u.default_group_id FROM users u " +
          "JOIN memberships m ON u.id = m.user_id WHERE m.group_id = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setInt(1, groupId);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
          members.add(new User(
              rs.getInt("id"),
              rs.getString("name"),
              rs.getString("email"),
              (Integer) rs.getObject("default_group_id")));
        }
      }
    }
    return members;
  }

  public List<Group> getAllGroups() throws SQLException {
    List<Group> groups = new ArrayList<>();
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      String query = "SELECT id, name, expense, expense_currency, description, icon FROM groups";
      try (PreparedStatement pstmt = conn.prepareStatement(query)) {
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
          groups.add(new Group(
              rs.getInt("id"),
              rs.getString("name"),
              rs.getDouble("expense"),
              rs.getString("expense_currency"),
              rs.getString("description"),
              rs.getString("icon")));
        }
      }
    }
    return groups;
  }

  public List<Group> getGroupsByUser(int userId) throws SQLException {
    List<Group> groups = new ArrayList<>();
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      String query = "SELECT g.id, g.name, g.expense, g.expense_currency, g.description, g.icon " +
          "FROM groups g " +
          "JOIN memberships m ON g.id = m.group_id " +
          "WHERE m.user_id = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setInt(1, userId);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
          groups.add(new Group(
              rs.getInt("id"),
              rs.getString("name"),
              rs.getDouble("expense"),
              rs.getString("expense_currency"),
              rs.getString("description"),
              rs.getString("icon")));
        }
      }
    }
    return groups;
  }

  public Group getGroupById(int groupId) throws SQLException {
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      String query = "SELECT id, name, expense, expense_currency, description, icon FROM groups WHERE id = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setInt(1, groupId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          return new Group(
              rs.getInt("id"),
              rs.getString("name"),
              rs.getDouble("expense"),
              rs.getString("expense_currency"),
              rs.getString("description"),
              rs.getString("icon"));
        }
      }
    }
    return null;
  }

  public void updateGroupIcon(int groupId, String icon) throws SQLException {
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      String update = "UPDATE groups SET icon = ? WHERE id = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(update)) {
        pstmt.setString(1, icon);
        pstmt.setInt(2, groupId);
        pstmt.executeUpdate();
      }
    }
  }
}
