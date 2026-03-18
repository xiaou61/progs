package com.campus.competition.modules.user.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.model.UserSummary;
import com.campus.competition.modules.auth.persistence.UserEntity;
import com.campus.competition.modules.log.service.AdminLogService;
import com.campus.competition.modules.role.service.RoleService;
import com.campus.competition.modules.user.model.AssignUserRoleCommand;
import com.campus.competition.modules.user.model.CreateUserCommand;
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
  public UserSummary createUser(CreateUserCommand command) {
    if (command == null) {
      throw new IllegalArgumentException("创建用户参数不能为空");
    }

    String studentNo = normalizeRequired(command.studentNo(), "学号不能为空");
    String realName = normalizeRequired(command.realName(), "姓名不能为空");
    String phone = normalizeRequired(command.phone(), "手机号不能为空");
    String password = normalizeRequired(command.password(), "密码不能为空");
    if (password.length() < 8) {
      throw new IllegalArgumentException("密码长度不能少于 8 位");
    }

    String roleCode = roleService.getRequiredRole(command.roleCode()).getRoleCode();
    Long exists = userMapper.selectCount(Wrappers.<UserEntity>lambdaQuery()
      .eq(UserEntity::getStudentNo, studentNo));
    if (exists != null && exists > 0) {
      throw new IllegalArgumentException("学号已存在");
    }

    LocalDateTime now = LocalDateTime.now();
    UserEntity entity = new UserEntity();
    entity.setStudentNo(studentNo);
    entity.setRealName(realName);
    entity.setPhone(phone);
    entity.setRoleCode(roleCode);
    entity.setPasswordHash(passwordEncoder.encode(password));
    entity.setStatus("ENABLED");
    entity.setViolationMarked(false);
    entity.setViolationReason(null);
    entity.setNotifyResult(true);
    entity.setNotifyPoints(true);
    entity.setAllowPrivateMessage(true);
    entity.setPublicCompetition(true);
    entity.setPublicPoints(true);
    entity.setPublicSubmission(true);
    entity.setCreatedAt(now);
    entity.setUpdatedAt(now);
    userMapper.insert(entity);

    adminLogService.record("USER_CREATE", entity.getStudentNo(), "创建角色 " + entity.getRoleCode());
    return toSummary(entity);
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

  private String normalizeRequired(String value, String errorMessage) {
    String normalized = value == null ? "" : value.trim();
    if (normalized.isBlank()) {
      throw new IllegalArgumentException(errorMessage);
    }
    return normalized;
  }
}
