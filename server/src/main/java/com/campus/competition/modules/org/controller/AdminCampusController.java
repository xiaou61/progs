package com.campus.competition.modules.org.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.org.mapper.CampusMapper;
import com.campus.competition.modules.org.persistence.CampusEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/campuses")
public class AdminCampusController {

  private final CampusMapper campusMapper;

  public AdminCampusController(CampusMapper campusMapper) {
    this.campusMapper = campusMapper;
  }

  @GetMapping
  public ApiResponse<List<CampusView>> list() {
    return ApiResponse.success(campusMapper.selectList(Wrappers.<CampusEntity>lambdaQuery()
        .orderByAsc(CampusEntity::getId))
      .stream()
      .map(this::toView)
      .toList());
  }

  @PostMapping
  public ApiResponse<CampusView> create(@RequestBody SaveCampusCommand command) {
    String campusCode = normalizeRequired(command == null ? null : command.campusCode(), "校区编码不能为空").toUpperCase();
    String campusName = normalizeRequired(command == null ? null : command.campusName(), "校区名称不能为空");
    String status = normalizeStatus(command == null ? null : command.status());
    Long exists = campusMapper.selectCount(Wrappers.<CampusEntity>lambdaQuery()
      .eq(CampusEntity::getCampusCode, campusCode));
    if (exists != null && exists > 0) {
      throw new IllegalArgumentException("校区编码已存在");
    }

    LocalDateTime now = LocalDateTime.now();
    CampusEntity entity = new CampusEntity();
    entity.setCampusCode(campusCode);
    entity.setCampusName(campusName);
    entity.setStatus(status);
    entity.setCreatedAt(now);
    entity.setUpdatedAt(now);
    campusMapper.insert(entity);
    return ApiResponse.success(toView(entity));
  }

  @PutMapping("/{campusId}")
  public ApiResponse<CampusView> update(@PathVariable Long campusId, @RequestBody SaveCampusCommand command) {
    String campusCode = normalizeRequired(command == null ? null : command.campusCode(), "校区编码不能为空").toUpperCase();
    String campusName = normalizeRequired(command == null ? null : command.campusName(), "校区名称不能为空");
    String status = normalizeStatus(command == null ? null : command.status());
    CampusEntity entity = campusMapper.selectById(campusId);
    if (entity == null) {
      throw new IllegalArgumentException("校区不存在");
    }

    Long duplicatedCode = campusMapper.selectCount(Wrappers.<CampusEntity>lambdaQuery()
      .eq(CampusEntity::getCampusCode, campusCode)
      .ne(CampusEntity::getId, campusId));
    if (duplicatedCode != null && duplicatedCode > 0) {
      throw new IllegalArgumentException("校区编码已存在");
    }

    entity.setCampusCode(campusCode);
    entity.setCampusName(campusName);
    entity.setStatus(status);
    entity.setUpdatedAt(LocalDateTime.now());
    campusMapper.updateById(entity);
    return ApiResponse.success(toView(entity));
  }

  private String normalizeRequired(String value, String errorMessage) {
    String normalized = value == null ? "" : value.trim();
    if (normalized.isBlank()) {
      throw new IllegalArgumentException(errorMessage);
    }
    return normalized;
  }

  private String normalizeStatus(String status) {
    String normalized = normalizeRequired(status, "校区状态不能为空").toUpperCase();
    if (!"ENABLED".equals(normalized) && !"DISABLED".equals(normalized)) {
      throw new IllegalArgumentException("校区状态不合法");
    }
    return normalized;
  }

  private CampusView toView(CampusEntity entity) {
    return new CampusView(
      entity.getId(),
      entity.getCampusCode(),
      entity.getCampusName(),
      entity.getStatus()
    );
  }

  public record CampusView(
    Long id,
    String campusCode,
    String campusName,
    String status
  ) {
  }

  public record SaveCampusCommand(
    String campusCode,
    String campusName,
    String status
  ) {
  }
}
