package com.campus.competition.modules.message.model;

public record SendGroupMessageCommand(
  Long competitionId,
  Long senderUserId,
  String content
) {
}
