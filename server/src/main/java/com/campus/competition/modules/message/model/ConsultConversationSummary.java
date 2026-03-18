package com.campus.competition.modules.message.model;

public record ConsultConversationSummary(
  String conversationType,
  Long peerUserId,
  String peerName,
  Long competitionId
) {
}
