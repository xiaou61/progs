package com.campus.competition.modules.competition.model;

import java.time.LocalDateTime;

public record CompetitionSummary(
  Long id,
  String title,
  String description,
  LocalDateTime signupStartAt,
  LocalDateTime signupEndAt,
  LocalDateTime startAt,
  LocalDateTime endAt,
  Integer quota,
  String status,
  boolean recommended,
  boolean pinned,
  String participantType,
  Long advisorTeacherId,
  String advisorTeacherName
) {
  public CompetitionSummary(
    Long id,
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
    this(id, title, description, signupStartAt, signupEndAt, startAt, endAt, quota, status, recommended, pinned, "STUDENT_ONLY", null, null);
  }
}
