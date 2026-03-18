package com.campus.competition.modules.checkin.model;

public record CheckInCommand(
  Long competitionId,
  Long userId,
  String method
) {
}
