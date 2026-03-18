package com.campus.competition.modules.message.model;

import java.time.LocalDateTime;

public record ConversationSummary(
  Long id,
  String conversationType,
  Long peerUserId,
  Long competitionId,
  String title,
  String lastMessage,
  LocalDateTime lastMessageAt,
  int unreadCount,
  boolean pinned,
  boolean muted
) {
}
