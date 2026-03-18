package com.campus.competition.modules.common.controller;

import com.campus.competition.modules.common.model.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/health")
public class HealthController {

  @GetMapping
  public ApiResponse<String> health() {
    return ApiResponse.success("UP");
  }
}
