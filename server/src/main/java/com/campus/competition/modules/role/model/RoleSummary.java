package com.campus.competition.modules.role.model;

import java.util.List;

public record RoleSummary(
  Long id,
  String roleCode,
  String roleName,
  String description,
  List<String> permissionCodes,
  boolean builtIn,
  String status,
  long userCount
) {
}
