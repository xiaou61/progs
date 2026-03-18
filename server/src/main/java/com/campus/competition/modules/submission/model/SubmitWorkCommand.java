package com.campus.competition.modules.submission.model;

public record SubmitWorkCommand(
  Long competitionId,
  Long userId,
  String fileUrl,
  boolean reuploadAllowed
) {
}
