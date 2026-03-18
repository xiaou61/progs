package com.campus.competition.modules.score.controller;

import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.score.model.PublishResultCommand;
import com.campus.competition.modules.score.model.ScoreSummary;
import com.campus.competition.modules.score.service.ScoreService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/scores")
public class AdminScoreController {

  private final ScoreService scoreService;

  public AdminScoreController(ScoreService scoreService) {
    this.scoreService = scoreService;
  }

  @PostMapping("/publish")
  public ApiResponse<Map<String, Long>> publish(@RequestBody PublishResultCommand command) {
    return ApiResponse.success(Map.of("scoreId", scoreService.publish(command)));
  }

  @GetMapping("/competition/{competitionId}")
  public ApiResponse<List<ScoreSummary>> listByCompetition(@PathVariable Long competitionId) {
    return ApiResponse.success(scoreService.listByCompetition(competitionId));
  }
}
