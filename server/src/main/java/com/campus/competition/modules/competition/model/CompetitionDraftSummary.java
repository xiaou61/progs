package com.campus.competition.modules.competition.model;

import java.time.LocalDateTime;

public record CompetitionDraftSummary(
  Long id,
  Long organizerId,
  String title,
  String description,
  LocalDateTime signupStartAt,
  LocalDateTime signupEndAt,
  LocalDateTime startAt,
  LocalDateTime endAt,
  Integer quota,
  String status,
  boolean recommended,
  boolean pinned
) {
}
