package com.campus.competition.modules.competition.controller;

import com.campus.competition.modules.auth.security.AuthContext;
import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.competition.model.CompetitionDetail;
import com.campus.competition.modules.competition.model.CompetitionDraftSummary;
import com.campus.competition.modules.competition.model.CompetitionFeatureCommand;
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
@RequestMapping("/api/app/teachers/{teacherId}/competitions")
public class AppTeacherCompetitionController {

  private final CompetitionService competitionService;

  public AppTeacherCompetitionController(CompetitionService competitionService) {
    this.competitionService = competitionService;
  }

  @GetMapping
  public ApiResponse<List<CompetitionDraftSummary>> list(@PathVariable Long teacherId) {
    AuthContext.requireTeacher(teacherId);
    return ApiResponse.success(competitionService.listManagedCompetitionsByOrganizer(teacherId));
  }

  @PostMapping
  public ApiResponse<Map<String, Long>> publish(
    @PathVariable Long teacherId,
    @RequestBody PublishCompetitionCommand command
  ) {
    AuthContext.requireTeacher(teacherId);
    Long competitionId = competitionService.publishForOrganizer(teacherId, command);
    return ApiResponse.success(Map.of("competitionId", competitionId));
  }

  @PostMapping("/draft")
  public ApiResponse<Map<String, Long>> saveDraft(
    @PathVariable Long teacherId,
    @RequestBody SaveCompetitionDraftCommand command
  ) {
    AuthContext.requireTeacher(teacherId);
    Long competitionId = competitionService.saveDraftForOrganizer(teacherId, command);
    return ApiResponse.success(Map.of("competitionId", competitionId));
  }

  @PutMapping("/{competitionId}")
  public ApiResponse<CompetitionDetail> update(
    @PathVariable Long teacherId,
    @PathVariable Long competitionId,
    @RequestBody UpdateCompetitionCommand command
  ) {
    AuthContext.requireTeacher(teacherId);
    return ApiResponse.success(competitionService.updateForOrganizer(teacherId, competitionId, command));
  }

  @PostMapping("/{competitionId}/feature")
  public ApiResponse<CompetitionDetail> updateFeature(
    @PathVariable Long teacherId,
    @PathVariable Long competitionId,
    @RequestBody CompetitionFeatureCommand command
  ) {
    AuthContext.requireTeacher(teacherId);
    return ApiResponse.success(competitionService.updateFeatureForOrganizer(teacherId, competitionId, command));
  }

  @PostMapping("/{competitionId}/offline")
  public ApiResponse<Map<String, Boolean>> offline(
    @PathVariable Long teacherId,
    @PathVariable Long competitionId
  ) {
    AuthContext.requireTeacher(teacherId);
    return ApiResponse.success(Map.of("offline", competitionService.offlineForOrganizer(teacherId, competitionId)));
  }
}
