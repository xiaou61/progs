package com.campus.competition.modules.points.model;

public record ShareCompetitionCommand(
  Long userId,
  Long competitionId
) {
}
