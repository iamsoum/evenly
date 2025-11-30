package com.evenly.services;

import com.evenly.models.User;

public class SessionManager {
  private static User currentUser;

  public static void setCurrentUser(User user) {
    currentUser = user;
  }

  public static User getCurrentUser() {
    return currentUser;
  }

  public static void clearSession() {
    currentUser = null;
  }
}
