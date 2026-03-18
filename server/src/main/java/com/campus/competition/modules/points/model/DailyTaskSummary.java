package com.campus.competition.modules.points.model;

import java.time.LocalDateTime;

public record DailyTaskSummary(
  Long userId,
  boolean dailyCheckinDone,
  boolean competitionShareDone,
  int todayTaskPoints,
  LocalDateTime lastCheckinAt,
  LocalDateTime lastShareAt
) {
}
