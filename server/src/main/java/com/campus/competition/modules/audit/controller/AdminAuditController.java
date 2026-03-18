package com.campus.competition.modules.audit.controller;

import com.campus.competition.modules.audit.model.AuditRuleSummary;
import com.campus.competition.modules.audit.model.ViolationRecordSummary;
import com.campus.competition.modules.audit.service.ContentAuditService;
import com.campus.competition.modules.common.model.ApiResponse;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit")
public class AdminAuditController {

  private final ContentAuditService contentAuditService;

  public AdminAuditController(ContentAuditService contentAuditService) {
    this.contentAuditService = contentAuditService;
  }

  @GetMapping(value = "/rules", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
  public ApiResponse<AuditRuleSummary> rules() {
    return ApiResponse.success(contentAuditService.getRuleSummary());
  }

  @GetMapping(value = "/violations", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
  public ApiResponse<List<ViolationRecordSummary>> violations() {
    return ApiResponse.success(contentAuditService.listViolationRecords());
  }
}
