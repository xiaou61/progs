package com.campus.competition.modules.auth.model;

public record LoginResult(
  Long userId,
  String roleCode,
  String token,
  String studentNo,
  String realName
) {
}
