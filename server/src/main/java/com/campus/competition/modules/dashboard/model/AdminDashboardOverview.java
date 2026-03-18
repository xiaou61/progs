package com.campus.competition.modules.dashboard.model;

public record AdminDashboardOverview(
  int totalCompetitionCount,
  int publishedCompetitionCount,
  int draftCompetitionCount,
  int offlineCompetitionCount,
  int totalRegistrationCount,
  int totalSubmissionCount,
  int totalAwardCount,
  int totalAwardPoints,
  int teacherCount,
  int studentCount
) {
}
