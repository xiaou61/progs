package com.campus.competition.modules.user.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.model.UserSummary;
import com.campus.competition.modules.auth.persistence.UserEntity;
import com.campus.competition.modules.log.service.AdminLogService;
import com.campus.competition.modules.role.service.RoleService;
import com.campus.competition.modules.user.model.AssignUserRoleCommand;
import com.campus.competition.modules.user.model.FreezeUserCommand;
import com.campus.competition.modules.user.model.ResetPasswordCommand;
import com.campus.competition.modules.user.model.ViolationGovernanceCommand;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminUserServiceImpl implements AdminUserService {

  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  private final UserMapper userMapper;
  private final RoleService roleService;
  private final AdminLogService adminLogService;

  public AdminUserServiceImpl(UserMapper userMapper, RoleService roleService, AdminLogService adminLogService) {
    this.userMapper = userMapper;
    this.roleService = roleService;
    this.adminLogService = adminLogService;
  }

  @Override
  public List<UserSummary> listUsers() {
    return userMapper.selectList(Wrappers.<UserEntity>lambdaQuery()
        .ne(UserEntity::getStatus, "DELETED")
        .orderByAsc(UserEntity::getId))
      .stream()
      .map(this::toSummary)
      .toList();
  }

  @Override
  public boolean freeze(Long userId, FreezeUserCommand command) {
    UserEntity entity = getRequiredUser(userId);
    if ("DELETED".equals(entity.getStatus())) {
      throw new IllegalArgumentException("账号已注销");
    }
    entity.setStatus("DISABLED");
    entity.setUpdatedAt(LocalDateTime.now());
    userMapper.updateById(entity);
    adminLogService.record("USER_FREEZE", entity.getStudentNo(), command == null ? null : command.reason());
    return true;
  }

  @Override
  public boolean unfreeze(Long userId) {
    UserEntity entity = getRequiredUser(userId);
    if ("DELETED".equals(entity.getStatus())) {
      throw new IllegalArgumentException("账号已注销");
    }
    entity.setStatus("ENABLED");
    entity.setUpdatedAt(LocalDateTime.now());
    userMapper.updateById(entity);
    adminLogService.record("USER_UNFREEZE", entity.getStudentNo(), "恢复账号");
    return true;
  }

  @Override
  public boolean resetPassword(Long userId, ResetPasswordCommand command) {
    UserEntity entity = getRequiredUser(userId);
    String password = command == null ? null : command.newPassword();
    if (password == null || password.length() < 8) {
      throw new IllegalArgumentException("密码长度不能少于 8 位");
    }
    entity.setPasswordHash(passwordEncoder.encode(password));
    entity.setUpdatedAt(LocalDateTime.now());
    userMapper.updateById(entity);
    adminLogService.record("USER_RESET_PASSWORD", entity.getStudentNo(), "后台重置密码");
    return true;
  }

  @Override
  public UserSummary assignRole(Long userId, AssignUserRoleCommand command) {
    if (command == null || command.roleCode() == null || command.roleCode().isBlank()) {
      throw new IllegalArgumentException("角色不能为空");
    }
    UserEntity entity = getRequiredUser(userId);
    entity.setRoleCode(roleService.getRequiredRole(command.roleCode()).getRoleCode());
    entity.setUpdatedAt(LocalDateTime.now());
    userMapper.updateById(entity);
    adminLogService.record("USER_ASSIGN_ROLE", entity.getStudentNo(), "调整为 " + entity.getRoleCode());
    return toSummary(entity);
  }

  @Override
  public boolean governViolation(Long userId, ViolationGovernanceCommand command) {
    if (command == null || command.violating() == null) {
      throw new IllegalArgumentException("违规治理参数不能为空");
    }
    String reason = command.reason() == null ? "" : command.reason().trim();
    if (reason.isBlank()) {
      throw new IllegalArgumentException("治理原因不能为空");
    }

    UserEntity entity = getRequiredUser(userId);
    entity.setViolationMarked(Boolean.TRUE.equals(command.violating()));
    entity.setViolationReason(Boolean.TRUE.equals(command.violating()) ? reason : null);
    entity.setUpdatedAt(LocalDateTime.now());
    userMapper.updateById(entity);

    adminLogService.record(
      Boolean.TRUE.equals(command.violating()) ? "VIOLATION_MARK" : "VIOLATION_CLEAR",
      entity.getStudentNo(),
      reason
    );
    return true;
  }

  private UserEntity getRequiredUser(Long userId) {
    if (userId == null) {
      throw new IllegalArgumentException("用户不能为空");
    }
    UserEntity entity = userMapper.selectById(userId);
    if (entity == null) {
      throw new IllegalArgumentException("用户不存在");
    }
    return entity;
  }

  private UserSummary toSummary(UserEntity user) {
    return new UserSummary(
      user.getId(),
      user.getStudentNo(),
      user.getRealName(),
      user.getPhone(),
      user.getRoleCode(),
      user.getStatus(),
      Boolean.TRUE.equals(user.getViolationMarked()),
      user.getViolationReason()
    );
  }
}
