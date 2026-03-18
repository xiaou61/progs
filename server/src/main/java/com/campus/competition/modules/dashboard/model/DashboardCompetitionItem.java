package com.campus.competition.modules.dashboard.model;

import java.time.LocalDateTime;

public record DashboardCompetitionItem(
  Long competitionId,
  Long organizerId,
  String title,
  String status,
  int registrationCount,
  int submissionCount,
  int awardCount,
  int awardPoints,
  boolean recommended,
  boolean pinned,
  LocalDateTime startAt,
  LocalDateTime endAt
) {
}
