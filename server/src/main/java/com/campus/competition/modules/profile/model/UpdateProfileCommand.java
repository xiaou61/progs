package com.campus.competition.modules.profile.model;

public record UpdateProfileCommand(
  Long userId,
  String realName,
  String phone,
  String avatarUrl,
  String campusName,
  String gradeName,
  String majorName,
  String departmentName,
  String bio,
  Boolean notifyResult,
  Boolean notifyPoints,
  Boolean allowPrivateMessage,
  Boolean publicCompetition,
  Boolean publicPoints,
  Boolean publicSubmission
) {
}
