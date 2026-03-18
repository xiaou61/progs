package com.campus.competition.modules.competition.controller;

import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.competition.model.CompetitionDetail;
import com.campus.competition.modules.competition.model.CompetitionDraftSummary;
import com.campus.competition.modules.competition.model.CompetitionFeatureCommand;
import com.campus.competition.modules.competition.model.CompetitionSummary;
import com.campus.competition.modules.competition.model.PublishCompetitionCommand;
import com.campus.competition.modules.competition.model.SaveCompetitionDraftCommand;
import com.campus.competition.modules.competition.model.UpdateCompetitionCommand;
import com.campus.competition.modules.competition.service.CompetitionService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/competitions")
public class AdminCompetitionController {

  private final CompetitionService competitionService;

  public AdminCompetitionController(CompetitionService competitionService) {
    this.competitionService = competitionService;
  }

  @PostMapping
  public ApiResponse<Map<String, Long>> publish(@RequestBody PublishCompetitionCommand command) {
    Long competitionId = competitionService.publish(command);
    return ApiResponse.success(Map.of("competitionId", competitionId));
  }

  @PostMapping("/draft")
  public ApiResponse<Map<String, Long>> saveDraft(@RequestBody SaveCompetitionDraftCommand command) {
    Long competitionId = competitionService.saveDraft(command);
    return ApiResponse.success(Map.of("competitionId", competitionId));
  }

  @PutMapping("/{competitionId}")
  public ApiResponse<CompetitionDetail> update(
    @PathVariable Long competitionId,
    @RequestBody UpdateCompetitionCommand command
  ) {
    return ApiResponse.success(competitionService.update(competitionId, command));
  }

  @PostMapping("/{competitionId}/feature")
  public ApiResponse<CompetitionDetail> updateFeature(
    @PathVariable Long competitionId,
    @RequestBody CompetitionFeatureCommand command
  ) {
    return ApiResponse.success(competitionService.updateFeature(competitionId, command));
  }

  @PostMapping("/{competitionId}/offline")
  public ApiResponse<Map<String, Boolean>> offline(@PathVariable Long competitionId) {
    return ApiResponse.success(Map.of("offline", competitionService.offline(competitionId)));
  }

  @GetMapping
  public ApiResponse<List<CompetitionDraftSummary>> list() {
    return ApiResponse.success(competitionService.listManagedCompetitions());
  }
}
