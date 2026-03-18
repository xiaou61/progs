package com.campus.competition.modules.profile.model;

public record ProfileFileUploadResult(
  String fileName,
  String fileUrl,
  long size
) {
}
