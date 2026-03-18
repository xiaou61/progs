package com.campus.competition.modules.profile.model;

public record CancelAccountCommand(
  Long userId,
  String confirmText
) {
}
