package com.campus.competition.modules.auth.model;

public record RegisterCommand(
  String studentNo,
  String realName,
  String phone,
  String roleCode,
  String password
) {
}
