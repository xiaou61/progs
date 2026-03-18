package com.campus.competition.modules.user.model;

public record CreateUserCommand(
  String studentNo,
  String realName,
  String phone,
  String roleCode,
  String password
) {
}
