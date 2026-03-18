package com.campus.competition.modules.system.controller;

import com.campus.competition.modules.common.model.ApiResponse;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/banners")
public class AdminBannerController {

  @GetMapping
  public ApiResponse<List<Map<String, Object>>> list() {
    return ApiResponse.success(List.of(
      Map.of(
        "id", 1L,
        "title", "春季比赛季主视觉",
        "status", "ENABLED",
        "jumpPath", "/pages/competition/list/index"
      )
    ));
  }
}
