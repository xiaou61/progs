package com.campus.competition.modules.checkin.model;

import java.time.LocalDateTime;

public record CheckinSummary(
  Long id,
  Long competitionId,
  Long userId,
  String method,
  LocalDateTime checkedAt,
  String status,
  String reviewRemark,
  LocalDateTime reviewedAt
) {
}
