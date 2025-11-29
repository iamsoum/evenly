package com.evenly.models;

public class Membership {
  private int id;
  private int userId;
  private int groupId;

  public Membership(int id, int userId, int groupId) {
    this.id = id;
    this.userId = userId;
    this.groupId = groupId;
  }

  public int getId() {
    return id;
  }

  public int getUserId() {
    return userId;
  }

  public int getGroupId() {
    return groupId;
  }
}
