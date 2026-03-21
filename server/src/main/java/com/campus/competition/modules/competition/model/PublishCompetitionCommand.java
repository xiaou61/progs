package com.campus.competition.modules.competition.model;

import java.time.LocalDateTime;

public record PublishCompetitionCommand(
  Long organizerId,
  String title,
  String description,
  LocalDateTime signupStartAt,
  LocalDateTime signupEndAt,
  LocalDateTime startAt,
  LocalDateTime endAt,
  Integer quota,
  String participantType,
  Long advisorTeacherId,
  Boolean recommended,
  Boolean pinned
) {
  public PublishCompetitionCommand(
    Long organizerId,
    String title,
    String description,
    LocalDateTime signupStartAt,
    LocalDateTime signupEndAt,
    LocalDateTime startAt,
    LocalDateTime endAt,
    Integer quota
  ) {
    this(organizerId, title, description, signupStartAt, signupEndAt, startAt, endAt, quota, null, null, null, null);
  }
}
