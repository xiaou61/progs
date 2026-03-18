package com.campus.competition.modules.audit.model;

import java.time.LocalDateTime;
import java.util.List;

public record ViolationRecordSummary(
  Long id,
  String scene,
  Long bizId,
  Long userId,
  String userName,
  String reason,
  List<String> hitWords,
  String contentSnippet,
  LocalDateTime createdAt
) {
}
