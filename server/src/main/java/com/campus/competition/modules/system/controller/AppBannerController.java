package com.campus.competition.modules.system.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.system.mapper.BannerMapper;
import com.campus.competition.modules.system.persistence.BannerEntity;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/banners")
public class AppBannerController {

  private final BannerMapper bannerMapper;

  public AppBannerController(BannerMapper bannerMapper) {
    this.bannerMapper = bannerMapper;
  }

  @GetMapping
  public ApiResponse<List<BannerView>> list() {
    return ApiResponse.success(bannerMapper.selectList(Wrappers.<BannerEntity>lambdaQuery()
        .eq(BannerEntity::getStatus, "ENABLED")
        .orderByAsc(BannerEntity::getId))
      .stream()
      .map(this::toView)
      .toList());
  }

  private BannerView toView(BannerEntity entity) {
    return new BannerView(
      entity.getId(),
      entity.getTitle(),
      entity.getJumpPath() == null ? "" : entity.getJumpPath(),
      entity.getImageUrl()
    );
  }

  public record BannerView(
    Long id,
    String title,
    String jumpPath,
    String imageUrl
  ) {
  }
}
