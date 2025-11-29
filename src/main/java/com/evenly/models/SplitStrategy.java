package com.evenly.models;

public class SplitStrategy {
  private int id;
  private String type; // "EQUAL" or "PERCENTAGE"
  private String params; // JSON string

  public SplitStrategy(int id, String type, String params) {
    this.id = id;
    this.type = type;
    this.params = params;
  }

  public int getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public String getParams() {
    return params;
  }

  public void setId(int id) {
    this.id = id;
  }
}
