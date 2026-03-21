package com.campus.competition.modules.system.model;

public record BannerFileUploadResult(
  String originalFileName,
  String imageUrl,
  long fileSize
) {
}
