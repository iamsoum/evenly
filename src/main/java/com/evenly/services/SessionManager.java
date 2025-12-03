package com.evenly.services;

import com.evenly.models.User;

public class SessionManager {
  private static User currentUser;
  private static long lastActivity;
  private static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000; // 30 minutes

  public static void setCurrentUser(User user) {
    currentUser = user;
    lastActivity = System.currentTimeMillis();
  }

  public static User getCurrentUser() {
    return currentUser;
  }

  public static boolean isSessionValid() {
    if (currentUser == null) {
      return false;
    }
    long currentTime = System.currentTimeMillis();
    return (currentTime - lastActivity) < SESSION_TIMEOUT_MS;
  }

  public static void refreshSession() {
    if (currentUser != null) {
      lastActivity = System.currentTimeMillis();
    }
  }

  public static void clearSession() {
    currentUser = null;
    lastActivity = 0;
  }
}
