package com.campus.competition.modules.auth.model;

public record UserSummary(
  Long id,
  String studentNo,
  String realName,
  String phone,
  String roleCode,
  String status,
  boolean violationMarked,
  String violationReason
) {
}
