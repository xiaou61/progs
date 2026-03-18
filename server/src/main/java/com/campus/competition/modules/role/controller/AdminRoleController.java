package com.campus.competition.modules.role.controller;

import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.role.model.RoleSummary;
import com.campus.competition.modules.role.model.SaveRoleCommand;
import com.campus.competition.modules.role.service.RoleService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/roles")
public class AdminRoleController {

  private final RoleService roleService;

  public AdminRoleController(RoleService roleService) {
    this.roleService = roleService;
  }

  @GetMapping
  public ApiResponse<List<RoleSummary>> list() {
    return ApiResponse.success(roleService.listRoles());
  }

  @PostMapping
  public ApiResponse<RoleSummary> create(@RequestBody SaveRoleCommand command) {
    return ApiResponse.success(roleService.createRole(command));
  }

  @PutMapping("/{roleCode}")
  public ApiResponse<RoleSummary> update(@PathVariable String roleCode, @RequestBody SaveRoleCommand command) {
    return ApiResponse.success(roleService.updateRole(roleCode, command));
  }
}
