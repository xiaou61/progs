package com.campus.competition.modules.message.model;

public record ConversationPreferenceCommand(
  Long userId,
  Boolean enabled
) {
}
