package com.campus.competition.modules.dashboard.model;

import java.util.List;

public record AdminDashboardSummary(
  AdminDashboardOverview overview,
  List<DashboardDistributionItem> statusDistribution,
  List<DashboardCompetitionItem> topCompetitions
) {
}
