package com.campus.competition.modules.points.model;

public record PointsAccountSummary(
  Long userId,
  int availablePoints,
  int totalPoints
) {
}
