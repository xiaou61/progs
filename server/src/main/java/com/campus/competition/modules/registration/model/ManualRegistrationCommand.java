package com.campus.competition.modules.registration.model;

public record ManualRegistrationCommand(
  Long competitionId,
  Long userId,
  String remark
) {
}
