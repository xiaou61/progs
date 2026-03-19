package com.campus.competition.modules.dashboard.controller;

import com.campus.competition.modules.auth.security.AuthContext;
import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.dashboard.model.TeacherDashboardSummary;
import com.campus.competition.modules.dashboard.service.DashboardService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/dashboard")
public class AppTeacherDashboardController {

  private final DashboardService dashboardService;

  public AppTeacherDashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping("/teachers/{teacherId}")
  public ApiResponse<TeacherDashboardSummary> teacherOverview(@PathVariable Long teacherId) {
    AuthContext.requireTeacher(teacherId);
    return ApiResponse.success(dashboardService.getTeacherDashboardSummary(teacherId));
  }

  @GetMapping(value = "/teachers/{teacherId}/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<String> export(@PathVariable Long teacherId) {
    AuthContext.requireTeacher(teacherId);
    return ResponseEntity.ok()
      .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
      .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("teacher-dashboard.csv").build().toString())
      .body(dashboardService.exportTeacherDashboardCsv(teacherId));
  }
}
