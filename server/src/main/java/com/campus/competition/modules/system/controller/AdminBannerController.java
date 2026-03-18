package com.campus.competition.modules.system.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.system.mapper.BannerMapper;
import com.campus.competition.modules.system.persistence.BannerEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/banners")
public class AdminBannerController {

  private final BannerMapper bannerMapper;

  public AdminBannerController(BannerMapper bannerMapper) {
    this.bannerMapper = bannerMapper;
  }

  @GetMapping
  public ApiResponse<List<BannerView>> list() {
    return ApiResponse.success(bannerMapper.selectList(Wrappers.<BannerEntity>lambdaQuery()
        .orderByAsc(BannerEntity::getId))
      .stream()
      .map(this::toView)
      .toList());
  }

  @PutMapping("/{bannerId}")
  public ApiResponse<BannerView> update(@PathVariable Long bannerId, @RequestBody SaveBannerCommand command) {
    BannerEntity entity = bannerMapper.selectById(bannerId);
    if (entity == null) {
      throw new IllegalArgumentException("轮播图不存在");
    }

    entity.setTitle(normalizeRequired(command == null ? null : command.title(), "轮播图标题不能为空"));
    entity.setStatus(normalizeStatus(command == null ? null : command.status()));
    entity.setJumpPath(normalizeRequired(command == null ? null : command.jumpPath(), "跳转路径不能为空"));
    entity.setUpdatedAt(LocalDateTime.now());
    bannerMapper.updateById(entity);
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
    String normalized = normalizeRequired(status, "轮播图状态不能为空").toUpperCase();
    if (!"ENABLED".equals(normalized) && !"DISABLED".equals(normalized)) {
      throw new IllegalArgumentException("轮播图状态不合法");
    }
    return normalized;
  }

  private BannerView toView(BannerEntity entity) {
    return new BannerView(
      entity.getId(),
      entity.getTitle(),
      entity.getStatus(),
      entity.getJumpPath()
    );
  }

  public record BannerView(
    Long id,
    String title,
    String status,
    String jumpPath
  ) {
  }

  public record SaveBannerCommand(
    String title,
    String status,
    String jumpPath
  ) {
  }
}
