package com.campus.competition.modules.points.model;

import java.time.LocalDateTime;

public record PointsRecordSummary(
  Long id,
  Long userId,
  int changeAmount,
  String bizType,
  Long bizId,
  String remark,
  LocalDateTime createdAt
) {
}
