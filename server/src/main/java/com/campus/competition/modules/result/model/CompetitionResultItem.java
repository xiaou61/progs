package com.campus.competition.modules.result.model;

import java.time.LocalDateTime;

public record CompetitionResultItem(
  Long id,
  Long competitionId,
  Long studentId,
  String studentNo,
  String studentName,
  int score,
  int rank,
  String awardName,
  int points,
  LocalDateTime publishedAt,
  String reviewerName,
  String reviewComment
) {
}
