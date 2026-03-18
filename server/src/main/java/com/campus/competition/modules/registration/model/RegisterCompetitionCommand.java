package com.campus.competition.modules.registration.model;

public record RegisterCompetitionCommand(
  Long competitionId,
  Long userId
) {
}
