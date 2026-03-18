package com.campus.competition.modules.profile.model;

public record ProfileSummary(
  Long userId,
  String studentNo,
  String realName,
  String phone,
  String avatarUrl,
  String campusName,
  String gradeName,
  String majorName,
  String departmentName,
  String bio,
  String roleCode,
  boolean notifyResult,
  boolean notifyPoints,
  boolean allowPrivateMessage,
  boolean publicCompetition,
  boolean publicPoints,
  boolean publicSubmission,
  String status
) {
}
