package com.campus.competition.modules.user.model;

public record ViolationGovernanceCommand(
  Boolean violating,
  String reason
) {
}
