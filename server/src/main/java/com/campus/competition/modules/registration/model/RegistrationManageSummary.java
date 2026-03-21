package com.campus.competition.modules.registration.model;

import java.time.LocalDateTime;

public record RegistrationManageSummary(
  Long id,
  Long competitionId,
  Long userId,
  String status,
  String attendanceStatus,
  String remark,
  String checkinStatus,
  String checkinMethod,
  LocalDateTime checkinSubmittedAt,
  LocalDateTime checkinReviewedAt,
  String checkinReviewRemark
) {
}
