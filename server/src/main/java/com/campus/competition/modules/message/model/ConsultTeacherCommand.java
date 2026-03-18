package com.campus.competition.modules.message.model;

public record ConsultTeacherCommand(
  Long competitionId,
  Long userId,
  String content
) {
}
