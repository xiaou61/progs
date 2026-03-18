package com.campus.competition.modules.points.model;

public record PersonalCompetitionOverview(
  Long userId,
  int registeredCompetitionCount,
  int submittedWorkCount,
  int awardCount,
  int totalAwardPoints,
  int totalPoints
) {
}
