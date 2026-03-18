package com.campus.competition.modules.score.model;

public record PublishResultCommand(
  Long competitionId,
  Long studentId,
  int score,
  int rank,
  String awardName,
  int points,
  String reviewerName,
  String reviewComment
) {
}
