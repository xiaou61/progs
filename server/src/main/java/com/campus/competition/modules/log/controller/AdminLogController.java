package com.campus.competition.modules.log.controller;

import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.log.model.OperationLogSummary;
import com.campus.competition.modules.log.service.AdminLogService;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/logs")
public class AdminLogController {

  private final AdminLogService adminLogService;

  public AdminLogController(AdminLogService adminLogService) {
    this.adminLogService = adminLogService;
  }

  @GetMapping
  public ApiResponse<List<OperationLogSummary>> list(
    @RequestParam(required = false) String operatorName,
    @RequestParam(required = false) String action,
    @RequestParam(required = false) String target
  ) {
    return ApiResponse.success(adminLogService.listLogs(operatorName, action, target));
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<String> export(
    @RequestParam(required = false) String operatorName,
    @RequestParam(required = false) String action,
    @RequestParam(required = false) String target
  ) {
    return ResponseEntity.ok()
      .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
      .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("admin-logs.csv").build().toString())
      .body(adminLogService.exportCsv(operatorName, action, target));
  }
}
