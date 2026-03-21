package com.campus.competition.modules.competition.model;

import java.time.LocalDateTime;

public record UpdateCompetitionCommand(
  Long organizerId,
  String title,
  String description,
  LocalDateTime signupStartAt,
  LocalDateTime signupEndAt,
  LocalDateTime startAt,
  LocalDateTime endAt,
  Integer quota,
  String status,
  String participantType,
  Long advisorTeacherId,
  Boolean recommended,
  Boolean pinned
) {
  public UpdateCompetitionCommand(
    Long organizerId,
    String title,
    String description,
    LocalDateTime signupStartAt,
    LocalDateTime signupEndAt,
    LocalDateTime startAt,
    LocalDateTime endAt,
    Integer quota,
    String status
  ) {
    this(organizerId, title, description, signupStartAt, signupEndAt, startAt, endAt, quota, status, null, null, null, null);
  }
}
