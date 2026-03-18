package com.campus.competition.modules.review.model;

import java.time.LocalDateTime;

public record ReviewTaskSummary(
  Long competitionId,
  Long submissionId,
  Long studentId,
  String reviewerName,
  String status,
  String reviewComment,
  Integer suggestedScore,
  LocalDateTime reviewedAt
) {
}
