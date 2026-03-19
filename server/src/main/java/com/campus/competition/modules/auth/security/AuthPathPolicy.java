package com.campus.competition.modules.auth.security;

import org.springframework.stereotype.Component;

@Component
public class AuthPathPolicy {

  public boolean isProtectedPath(String method, String path) {
    return path.startsWith("/api/admin/") || path.startsWith("/api/app/");
  }

  public boolean isPublicPath(String method, String path) {
    if (path.startsWith("/api/public/")) {
      return true;
    }
    if ("/api/app/auth/login".equals(path) || "/api/app/auth/register".equals(path)) {
      return true;
    }
    return "GET".equalsIgnoreCase(method)
      && ("/api/app/competitions".equals(path) || path.startsWith("/api/app/competitions/"));
  }

  public boolean requiresAdmin(String path) {
    return path.startsWith("/api/admin/");
  }
}
