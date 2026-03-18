package com.campus.competition.modules.message.model;

import java.time.LocalDateTime;

public record SystemMessageSummary(
  Long id,
  String title,
  String content,
  String bizType,
  Long bizId,
  boolean readFlag,
  LocalDateTime createdAt
) {
}
