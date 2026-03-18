package com.campus.competition.modules.dashboard.model;

import java.util.List;

public record TeacherDashboardSummary(
  Long teacherId,
  String teacherName,
  TeacherDashboardOverview overview,
  List<DashboardDistributionItem> statusDistribution,
  List<DashboardCompetitionItem> competitions
) {
}
