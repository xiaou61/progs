package com.campus.competition.modules.profile.model;

public record ChangePasswordCommand(
  Long userId,
  String oldPassword,
  String newPassword
) {
}
