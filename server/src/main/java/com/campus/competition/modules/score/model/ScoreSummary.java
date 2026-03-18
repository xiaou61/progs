package com.campus.competition.modules.score.model;

import java.time.LocalDateTime;

public record ScoreSummary(
  Long id,
  Long competitionId,
  Long studentId,
  int score,
  int rank,
  String awardName,
  int points,
  LocalDateTime publishedAt,
  String reviewerName,
  String reviewComment,
  String certificateNo,
  String certificateTitle
) {
}
