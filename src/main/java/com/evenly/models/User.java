package com.evenly.models;

public class User {
  private int id;
  private String name;
  private String email;
  private Integer defaultGroupId;

  public User(int id, String name, String email, Integer defaultGroupId) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.defaultGroupId = defaultGroupId;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public Integer getDefaultGroupId() {
    return defaultGroupId;
  }
}
