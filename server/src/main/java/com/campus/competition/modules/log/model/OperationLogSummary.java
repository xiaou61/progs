package com.campus.competition.modules.log.model;

import java.time.LocalDateTime;

public record OperationLogSummary(
  Long id,
  String operatorName,
  String action,
  String target,
  String detail,
  LocalDateTime createdAt
) {
}
