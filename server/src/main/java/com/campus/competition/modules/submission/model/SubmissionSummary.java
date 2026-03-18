package com.campus.competition.modules.submission.model;

import java.time.LocalDateTime;

public record SubmissionSummary(
  Long id,
  Long competitionId,
  Long userId,
  String fileUrl,
  int versionNo,
  LocalDateTime submittedAt
) {
}
