package com.campus.competition.modules.user.controller;

import com.campus.competition.modules.auth.model.UserSummary;
import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.user.model.AssignUserRoleCommand;
import com.campus.competition.modules.user.model.CreateUserCommand;
import com.campus.competition.modules.user.model.FreezeUserCommand;
import com.campus.competition.modules.user.model.ResetPasswordCommand;
import com.campus.competition.modules.user.model.ViolationGovernanceCommand;
import com.campus.competition.modules.user.service.AdminUserService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

  private final AdminUserService adminUserService;

  public AdminUserController(AdminUserService adminUserService) {
    this.adminUserService = adminUserService;
  }

  @GetMapping
  public ApiResponse<List<UserSummary>> list() {
    return ApiResponse.success(adminUserService.listUsers());
  }

  @PostMapping
  public ApiResponse<UserSummary> create(@RequestBody CreateUserCommand command) {
    return ApiResponse.success(adminUserService.createUser(command));
  }

  @PostMapping("/{userId}/freeze")
  public ApiResponse<Map<String, Boolean>> freeze(@PathVariable Long userId, @RequestBody FreezeUserCommand command) {
    return ApiResponse.success(Map.of("updated", adminUserService.freeze(userId, command)));
  }

  @PostMapping("/{userId}/unfreeze")
  public ApiResponse<Map<String, Boolean>> unfreeze(@PathVariable Long userId) {
    return ApiResponse.success(Map.of("updated", adminUserService.unfreeze(userId)));
  }

  @PostMapping("/{userId}/reset-password")
  public ApiResponse<Map<String, Boolean>> resetPassword(@PathVariable Long userId, @RequestBody ResetPasswordCommand command) {
    return ApiResponse.success(Map.of("updated", adminUserService.resetPassword(userId, command)));
  }

  @PostMapping("/{userId}/role")
  public ApiResponse<UserSummary> assignRole(@PathVariable Long userId, @RequestBody AssignUserRoleCommand command) {
    return ApiResponse.success(adminUserService.assignRole(userId, command));
  }

  @PostMapping("/{userId}/violation")
  public ApiResponse<Map<String, Boolean>> governViolation(
    @PathVariable Long userId,
    @RequestBody ViolationGovernanceCommand command
  ) {
    return ApiResponse.success(Map.of("updated", adminUserService.governViolation(userId, command)));
  }
}
