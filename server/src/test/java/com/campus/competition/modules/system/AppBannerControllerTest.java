package com.campus.competition.modules.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.system.controller.AppBannerController;
import com.campus.competition.modules.system.mapper.BannerMapper;
import com.campus.competition.modules.system.persistence.BannerEntity;
import java.util.List;
import org.junit.jupiter.api.Test;

class AppBannerControllerTest {

  @Test
  void shouldListMiniProgramBanners() {
    BannerMapper bannerMapper = mock(BannerMapper.class);
    BannerEntity entity = new BannerEntity();
    entity.setId(1L);
    entity.setTitle("春季比赛季主视觉");
    entity.setStatus("ENABLED");
    entity.setJumpPath("");
    entity.setImageUrl("/uploads/banners/banner-1.png");
    when(bannerMapper.selectList(any())).thenReturn(List.of(entity));

    AppBannerController controller = new AppBannerController(bannerMapper);
    ApiResponse<List<AppBannerController.BannerView>> response = controller.list();

    assertEquals(0, response.getCode());
    assertEquals(1, response.getData().size());
    assertEquals("春季比赛季主视觉", response.getData().get(0).title());
    assertEquals("", response.getData().get(0).jumpPath());
    assertEquals("/uploads/banners/banner-1.png", response.getData().get(0).imageUrl());
  }
}
