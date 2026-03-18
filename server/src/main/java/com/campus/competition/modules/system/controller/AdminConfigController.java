package com.campus.competition.modules.system.controller;

import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.system.mapper.PlatformConfigMapper;
import com.campus.competition.modules.system.persistence.PlatformConfigEntity;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/configs")
public class AdminConfigController {

  private static final long DEFAULT_CONFIG_ID = 1L;
  private final PlatformConfigMapper platformConfigMapper;

  public AdminConfigController(PlatformConfigMapper platformConfigMapper) {
    this.platformConfigMapper = platformConfigMapper;
  }

  @GetMapping
  public ApiResponse<SystemConfigView> detail() {
    return ApiResponse.success(toView(getRequiredConfig()));
  }

  @PutMapping
  public ApiResponse<SystemConfigView> update(@RequestBody UpdateSystemConfigCommand command) {
    PlatformConfigEntity entity = getRequiredConfig();
    entity.setPlatformName(normalizeRequired(command == null ? null : command.platformName(), "平台名称不能为空"));
    entity.setMvpPhase(normalizeRequired(command == null ? null : command.mvpPhase(), "阶段名称不能为空"));
    entity.setPointsEnabled(requireBoolean(command == null ? null : command.pointsEnabled(), "积分开关不能为空"));
    entity.setSubmissionReuploadEnabled(
      requireBoolean(command == null ? null : command.submissionReuploadEnabled(), "作品重传开关不能为空")
    );
    entity.setUpdatedAt(LocalDateTime.now());
    platformConfigMapper.updateById(entity);
    return ApiResponse.success(toView(entity));
  }

  private String normalizeRequired(String value, String errorMessage) {
    String normalized = value == null ? "" : value.trim();
    if (normalized.isBlank()) {
      throw new IllegalArgumentException(errorMessage);
    }
    return normalized;
  }

  private boolean requireBoolean(Boolean value, String errorMessage) {
    if (value == null) {
      throw new IllegalArgumentException(errorMessage);
    }
    return value;
  }

  private PlatformConfigEntity getRequiredConfig() {
    PlatformConfigEntity entity = platformConfigMapper.selectById(DEFAULT_CONFIG_ID);
    if (entity == null) {
      throw new IllegalArgumentException("系统配置不存在");
    }
    return entity;
  }

  private SystemConfigView toView(PlatformConfigEntity entity) {
    return new SystemConfigView(
      entity.getPlatformName(),
      entity.getMvpPhase(),
      Boolean.TRUE.equals(entity.getPointsEnabled()),
      Boolean.TRUE.equals(entity.getSubmissionReuploadEnabled())
    );
  }

  public record SystemConfigView(
    String platformName,
    String mvpPhase,
    boolean pointsEnabled,
    boolean submissionReuploadEnabled
  ) {
  }

  public record UpdateSystemConfigCommand(
    String platformName,
    String mvpPhase,
    Boolean pointsEnabled,
    Boolean submissionReuploadEnabled
  ) {
  }
}
