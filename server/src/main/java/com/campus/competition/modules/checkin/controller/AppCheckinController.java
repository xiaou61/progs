package com.campus.competition.modules.checkin.controller;

import com.campus.competition.modules.checkin.model.CheckInCommand;
import com.campus.competition.modules.checkin.model.CheckinSummary;
import com.campus.competition.modules.checkin.service.CheckinService;
import com.campus.competition.modules.auth.security.AuthContext;
import com.campus.competition.modules.common.model.ApiResponse;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/checkins")
public class AppCheckinController {

  private final CheckinService checkinService;

  public AppCheckinController(CheckinService checkinService) {
    this.checkinService = checkinService;
  }

  @PostMapping
  public ApiResponse<Map<String, Object>> checkIn(@RequestBody CheckInCommand command) {
    AuthContext.requireUser(command == null ? null : command.userId());
    return ApiResponse.success(Map.of(
      "checked", checkinService.checkIn(command),
      "status", "PENDING"
    ));
  }

  @GetMapping("/competition/{competitionId}")
  public ApiResponse<List<CheckinSummary>> listByCompetition(@PathVariable Long competitionId) {
    return ApiResponse.success(checkinService.listByCompetition(competitionId));
  }
}
