package com.campus.competition.modules.message.model;

import java.time.LocalDateTime;

public record ChatMessageSummary(
  Long id,
  String conversationType,
  Long senderUserId,
  String senderName,
  Long receiverUserId,
  Long competitionId,
  String content,
  LocalDateTime createdAt,
  boolean mine
) {
}
