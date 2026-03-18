package com.campus.competition.modules.submission.model;

public record SubmissionFileUploadResult(
  String fileName,
  String fileUrl,
  long size
) {
}
