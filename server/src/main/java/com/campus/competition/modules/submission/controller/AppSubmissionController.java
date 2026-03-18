package com.campus.competition.modules.submission.controller;

import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.submission.model.SubmissionSummary;
import com.campus.competition.modules.submission.model.SubmitWorkCommand;
import com.campus.competition.modules.submission.service.SubmissionService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/submissions")
public class AppSubmissionController {

  private final SubmissionService submissionService;

  public AppSubmissionController(SubmissionService submissionService) {
    this.submissionService = submissionService;
  }

  @PostMapping
  public ApiResponse<Map<String, Long>> submit(@RequestBody SubmitWorkCommand command) {
    return ApiResponse.success(Map.of("submissionId", submissionService.submit(command)));
  }

  @GetMapping("/competition/{competitionId}")
  public ApiResponse<List<SubmissionSummary>> listByCompetition(@PathVariable Long competitionId) {
    return ApiResponse.success(submissionService.listByCompetition(competitionId));
  }
}
