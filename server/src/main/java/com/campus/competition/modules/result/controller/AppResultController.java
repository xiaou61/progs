package com.campus.competition.modules.result.controller;

import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.points.model.PointsAccountSummary;
import com.campus.competition.modules.points.model.PointsRecordSummary;
import com.campus.competition.modules.points.service.PointsService;
import com.campus.competition.modules.score.model.ScoreSummary;
import com.campus.competition.modules.score.service.ScoreService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/results")
public class AppResultController {

  private final ScoreService scoreService;
  private final PointsService pointsService;

  public AppResultController(ScoreService scoreService, PointsService pointsService) {
    this.scoreService = scoreService;
    this.pointsService = pointsService;
  }

  @GetMapping("/competition/{competitionId}")
  public ApiResponse<List<ScoreSummary>> listByCompetition(@PathVariable Long competitionId) {
    return ApiResponse.success(scoreService.listByCompetition(competitionId));
  }

  @GetMapping("/student/{studentId}")
  public ApiResponse<Map<String, Object>> studentOverview(@PathVariable Long studentId) {
    PointsAccountSummary account = pointsService.getAccount(studentId);
    List<ScoreSummary> results = scoreService.listByStudent(studentId);
    List<PointsRecordSummary> records = pointsService.listRecords(studentId);
    return ApiResponse.success(Map.of(
      "account", account,
      "results", results,
      "records", records
    ));
  }

  @GetMapping("/points")
  public ApiResponse<PointsAccountSummary> points(@RequestParam Long userId) {
    return ApiResponse.success(pointsService.getAccount(userId));
  }
}
