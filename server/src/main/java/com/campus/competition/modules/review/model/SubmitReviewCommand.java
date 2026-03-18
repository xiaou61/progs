package com.campus.competition.modules.review.model;

public record SubmitReviewCommand(
  Long competitionId,
  Long submissionId,
  Long studentId,
  String reviewerName,
  String reviewComment,
  Integer suggestedScore
) {
}
