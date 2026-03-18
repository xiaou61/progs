package com.campus.competition.modules.role.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.persistence.UserEntity;
import com.campus.competition.modules.log.service.AdminLogService;
import com.campus.competition.modules.role.mapper.RoleMapper;
import com.campus.competition.modules.role.model.RoleSummary;
import com.campus.competition.modules.role.model.SaveRoleCommand;
import com.campus.competition.modules.role.persistence.RoleEntity;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

  private static final Pattern ROLE_CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{1,31}$");
  private static final Map<String, BuiltInRole> BUILT_IN_ROLES = createBuiltInRoles();

  private final RoleMapper roleMapper;
  private final UserMapper userMapper;
  private final AdminLogService adminLogService;

  public RoleService(RoleMapper roleMapper, UserMapper userMapper, AdminLogService adminLogService) {
    this.roleMapper = roleMapper;
    this.userMapper = userMapper;
    this.adminLogService = adminLogService;
  }

  public List<RoleSummary> listRoles() {
    ensureBuiltInRoles();
    return roleMapper.selectList(Wrappers.<RoleEntity>lambdaQuery().orderByAsc(RoleEntity::getId))
      .stream()
      .sorted((left, right) -> Integer.compare(resolveSortOrder(left.getRoleCode()), resolveSortOrder(right.getRoleCode())))
      .map(this::toSummary)
      .toList();
  }

  public RoleSummary createRole(SaveRoleCommand command) {
    validateCreateCommand(command);
    ensureBuiltInRoles();
    String roleCode = normalizeRoleCode(command.roleCode());
    Long exists = roleMapper.selectCount(Wrappers.<RoleEntity>lambdaQuery().eq(RoleEntity::getRoleCode, roleCode));
    if (exists != null && exists > 0) {
      throw new IllegalArgumentException("角色编码已存在");
    }

    RoleEntity entity = new RoleEntity();
    entity.setRoleCode(roleCode);
    entity.setRoleName(normalizeRequired(command.roleName(), "角色名称不能为空"));
    entity.setDescription(normalizeOptional(command.description()));
    entity.setPermissionCodes(joinPermissionCodes(command.permissionCodes()));
    entity.setBuiltIn(false);
    entity.setStatus("ENABLED");
    entity.setUpdatedAt(LocalDateTime.now());
    roleMapper.insert(entity);
    adminLogService.record("ROLE_CREATE", entity.getRoleCode(), entity.getRoleName());
    return toSummary(entity);
  }

  public RoleSummary updateRole(String roleCode, SaveRoleCommand command) {
    RoleEntity entity = getRequiredRole(roleCode);
    entity.setRoleName(normalizeRequired(command.roleName(), "角色名称不能为空"));
    entity.setDescription(normalizeOptional(command.description()));
    entity.setPermissionCodes(joinPermissionCodes(command.permissionCodes()));
    entity.setUpdatedAt(LocalDateTime.now());
    roleMapper.updateById(entity);
    adminLogService.record("ROLE_UPDATE", entity.getRoleCode(), entity.getRoleName());
    return toSummary(entity);
  }

  public RoleEntity getRequiredRole(String roleCode) {
    ensureBuiltInRoles();
    String normalized = normalizeRoleCode(roleCode);
    RoleEntity entity = roleMapper.selectOne(Wrappers.<RoleEntity>lambdaQuery().eq(RoleEntity::getRoleCode, normalized));
    if (entity == null) {
      throw new IllegalArgumentException("角色不存在");
    }
    if (!"ENABLED".equals(entity.getStatus())) {
      throw new IllegalArgumentException("角色已停用");
    }
    return entity;
  }

  private void ensureBuiltInRoles() {
    for (Map.Entry<String, BuiltInRole> entry : BUILT_IN_ROLES.entrySet()) {
      String roleCode = entry.getKey();
      BuiltInRole builtInRole = entry.getValue();
      RoleEntity existing = roleMapper.selectOne(Wrappers.<RoleEntity>lambdaQuery().eq(RoleEntity::getRoleCode, roleCode));
      if (existing != null) {
        continue;
      }
      RoleEntity entity = new RoleEntity();
      entity.setRoleCode(roleCode);
      entity.setRoleName(builtInRole.roleName());
      entity.setDescription(builtInRole.description());
      entity.setPermissionCodes(String.join(",", builtInRole.permissionCodes()));
      entity.setBuiltIn(true);
      entity.setStatus("ENABLED");
      entity.setUpdatedAt(LocalDateTime.now());
      roleMapper.insert(entity);
    }
  }

  private RoleSummary toSummary(RoleEntity entity) {
    Long userCount = userMapper.selectCount(Wrappers.<UserEntity>lambdaQuery()
      .eq(UserEntity::getRoleCode, entity.getRoleCode())
      .ne(UserEntity::getStatus, "DELETED"));
    return new RoleSummary(
      entity.getId(),
      entity.getRoleCode(),
      entity.getRoleName(),
      entity.getDescription(),
      splitPermissionCodes(entity.getPermissionCodes()),
      Boolean.TRUE.equals(entity.getBuiltIn()),
      entity.getStatus(),
      userCount == null ? 0 : userCount
    );
  }

  private void validateCreateCommand(SaveRoleCommand command) {
    if (command == null) {
      throw new IllegalArgumentException("角色参数不能为空");
    }
    normalizeRoleCode(command.roleCode());
    normalizeRequired(command.roleName(), "角色名称不能为空");
    joinPermissionCodes(command.permissionCodes());
  }

  private String normalizeRoleCode(String roleCode) {
    String normalized = normalizeRequired(roleCode, "角色编码不能为空").toUpperCase();
    if (!ROLE_CODE_PATTERN.matcher(normalized).matches()) {
      throw new IllegalArgumentException("角色编码格式不合法");
    }
    return normalized;
  }

  private String normalizeRequired(String value, String errorMessage) {
    String normalized = value == null ? "" : value.trim();
    if (normalized.isBlank()) {
      throw new IllegalArgumentException(errorMessage);
    }
    return normalized;
  }

  private String normalizeOptional(String value) {
    String normalized = value == null ? "" : value.trim();
    return normalized.isBlank() ? null : normalized;
  }

  private String joinPermissionCodes(List<String> permissionCodes) {
    if (permissionCodes == null || permissionCodes.isEmpty()) {
      throw new IllegalArgumentException("权限编码不能为空");
    }
    List<String> normalizedCodes = permissionCodes.stream()
      .filter(Objects::nonNull)
      .map(String::trim)
      .filter(code -> !code.isBlank())
      .map(String::toUpperCase)
      .distinct()
      .toList();
    if (normalizedCodes.isEmpty()) {
      throw new IllegalArgumentException("权限编码不能为空");
    }
    return String.join(",", normalizedCodes);
  }

  private List<String> splitPermissionCodes(String permissionCodes) {
    if (permissionCodes == null || permissionCodes.isBlank()) {
      return List.of();
    }
    return List.of(permissionCodes.split(",")).stream()
      .map(String::trim)
      .filter(code -> !code.isBlank())
      .toList();
  }

  private int resolveSortOrder(String roleCode) {
    int builtInIndex = 0;
    for (String builtInCode : BUILT_IN_ROLES.keySet()) {
      if (builtInCode.equals(roleCode)) {
        return builtInIndex;
      }
      builtInIndex += 1;
    }
    return 100 + Math.abs(roleCode.hashCode());
  }

  private static Map<String, BuiltInRole> createBuiltInRoles() {
    Map<String, BuiltInRole> roles = new LinkedHashMap<>();
    roles.put("STUDENT", new BuiltInRole(
      "学生",
      "浏览比赛、报名参赛、签到上传、查看结果和积分。",
      List.of("COMPETITION_VIEW", "REGISTRATION_CREATE", "CHECKIN_SUBMIT", "SUBMISSION_UPLOAD", "RESULT_VIEW", "POINTS_VIEW", "MESSAGE_USE")
    ));
    roles.put("TEACHER", new BuiltInRole(
      "老师",
      "发布比赛、管理报名、组织评审和发布结果。",
      List.of("COMPETITION_VIEW", "COMPETITION_MANAGE", "REGISTRATION_MANAGE", "REVIEW_MANAGE", "SCORE_PUBLISH", "RESULT_VIEW", "MESSAGE_USE")
    ));
    roles.put("ADMIN", new BuiltInRole(
      "管理员",
      "负责用户治理、角色权限、系统配置和全局运营。",
      List.of("USER_MANAGE", "ROLE_MANAGE", "CAMPUS_MANAGE", "COMPETITION_MANAGE", "REVIEW_MANAGE", "SYSTEM_MANAGE", "LOG_VIEW")
    ));
    return roles;
  }

  private record BuiltInRole(
    String roleName,
    String description,
    List<String> permissionCodes
  ) {
  }
}
