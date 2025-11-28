package com.evenly.models;

import java.time.LocalDateTime;

public class Transaction {
  private int id;
  private String description;
  private double amount;
  private String currency;
  private int payerId;
  private int groupId;
  private LocalDateTime createdAt;

  public Transaction(int id, String description, double amount, String currency, int payerId, int groupId,
      LocalDateTime createdAt) {
    this.id = id;
    this.description = description;
    this.amount = amount;
    this.currency = currency;
    this.payerId = payerId;
    this.groupId = groupId;
    this.createdAt = createdAt;
  }

  public int getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public double getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  public int getPayerId() {
    return payerId;
  }

  public int getGroupId() {
    return groupId;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
