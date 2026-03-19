package com.campus.competition.modules.auth.security;

public record AuthPrincipal(
  Long userId,
  String studentNo,
  String roleCode
) {
}
