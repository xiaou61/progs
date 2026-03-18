package com.campus.competition.modules.registration.model;

public record RegistrationSummary(
  Long id,
  Long competitionId,
  Long userId,
  String status,
  String attendanceStatus,
  String remark
) {
}
