package com.campus.competition.modules.audit.model;

import java.util.List;

public record AuditRuleSummary(
  List<String> sensitiveWords,
  List<String> allowedSubmissionExtensions,
  List<String> blockedSubmissionExtensions
) {
}
