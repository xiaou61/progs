package com.campus.competition.modules.checkin.model;

public record CheckinReviewCommand(
  String status,
  String reason
) {
}
