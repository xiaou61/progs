package com.campus.competition.modules.competition.model;

public record CompetitionFeatureCommand(
  Boolean recommended,
  Boolean pinned
) {
}
