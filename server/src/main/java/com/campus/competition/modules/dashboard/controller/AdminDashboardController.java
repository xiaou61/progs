package com.campus.competition.modules.dashboard.controller;

import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.dashboard.model.AdminDashboardSummary;
import com.campus.competition.modules.dashboard.service.DashboardService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

  private final DashboardService dashboardService;

  public AdminDashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping("/overview")
  public ApiResponse<AdminDashboardSummary> overview() {
    return ApiResponse.success(dashboardService.getAdminDashboardSummary());
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<String> export() {
    return ResponseEntity.ok()
      .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
      .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("admin-dashboard.csv").build().toString())
      .body(dashboardService.exportAdminDashboardCsv());
  }
}
