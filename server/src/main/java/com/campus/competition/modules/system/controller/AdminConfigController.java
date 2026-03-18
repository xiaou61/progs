package com.campus.competition.modules.system.controller;

import com.campus.competition.modules.common.model.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/configs")
public class AdminConfigController {

  @GetMapping
  public ApiResponse<Map<String, Object>> detail() {
    return ApiResponse.success(Map.of(
      "platformName", "校园师生比赛管理平台",
      "mvpPhase", "Phase 1",
      "pointsEnabled", true,
      "submissionReuploadEnabled", true
    ));
  }
}
