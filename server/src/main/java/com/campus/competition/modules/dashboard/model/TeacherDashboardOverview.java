package com.campus.competition.modules.dashboard.model;

public record TeacherDashboardOverview(
  int competitionCount,
  int publishedCompetitionCount,
  int draftCompetitionCount,
  int offlineCompetitionCount,
  int totalRegistrationCount,
  int totalSubmissionCount,
  int totalAwardCount,
  int totalAwardPoints
) {
}
