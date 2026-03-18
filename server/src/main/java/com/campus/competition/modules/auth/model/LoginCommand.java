package com.campus.competition.modules.auth.model;

public record LoginCommand(
  String studentNo,
  String password,
  String roleCode
) {
}
