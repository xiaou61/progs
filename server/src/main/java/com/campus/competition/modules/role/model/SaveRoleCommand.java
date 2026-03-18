package com.campus.competition.modules.role.model;

import java.util.List;

public record SaveRoleCommand(
  String roleCode,
  String roleName,
  String description,
  List<String> permissionCodes
) {
}
