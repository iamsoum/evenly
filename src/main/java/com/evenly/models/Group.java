package com.evenly.models;

public class Group {
  private int id;
  private String name;
  private double expense;
  private String expenseCurrency;
  private String description;

  public Group(int id, String name, double expense, String expenseCurrency, String description) {
    this.id = id;
    this.name = name;
    this.expense = expense;
    this.expenseCurrency = expenseCurrency;
    this.description = description;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public double getExpense() {
    return expense;
  }

  public String getExpenseCurrency() {
    return expenseCurrency;
  }

  public String getDescription() {
    return description;
  }
}
