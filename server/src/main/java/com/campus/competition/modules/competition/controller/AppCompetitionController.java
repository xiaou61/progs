package com.campus.competition.modules.competition.controller;

import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.competition.model.CompetitionDetail;
import com.campus.competition.modules.competition.model.CompetitionSummary;
import com.campus.competition.modules.competition.service.CompetitionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/competitions")
public class AppCompetitionController {

  private final CompetitionService competitionService;

  public AppCompetitionController(CompetitionService competitionService) {
    this.competitionService = competitionService;
  }

  @GetMapping
  public ApiResponse<List<CompetitionSummary>> list() {
    return ApiResponse.success(competitionService.listPublicCompetitions());
  }

  @GetMapping("/{competitionId}")
  public ApiResponse<CompetitionDetail> detail(@PathVariable Long competitionId) {
    return ApiResponse.success(competitionService.getCompetition(competitionId));
  }
}
