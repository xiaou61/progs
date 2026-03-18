package com.campus.competition.modules.registration.controller;

import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.registration.model.ManualRegistrationCommand;
import com.campus.competition.modules.registration.model.RegistrationAttendanceCommand;
import com.campus.competition.modules.registration.model.RegistrationSummary;
import com.campus.competition.modules.registration.model.RejectRegistrationCommand;
import com.campus.competition.modules.registration.service.RegistrationService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/registrations")
public class AdminRegistrationController {

  private final RegistrationService registrationService;

  public AdminRegistrationController(RegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  @GetMapping("/competition/{competitionId}")
  public ApiResponse<List<RegistrationSummary>> listByCompetition(@PathVariable Long competitionId) {
    return ApiResponse.success(registrationService.listByCompetition(competitionId));
  }

  @PostMapping("/manual")
  public ApiResponse<Map<String, Long>> manualAdd(@RequestBody ManualRegistrationCommand command) {
    return ApiResponse.success(Map.of("registrationId", registrationService.manualAdd(command)));
  }

  @PostMapping("/{registrationId}/reject")
  public ApiResponse<Map<String, Boolean>> reject(
    @PathVariable Long registrationId,
    @RequestBody RejectRegistrationCommand command
  ) {
    return ApiResponse.success(Map.of("rejected", registrationService.reject(registrationId, command)));
  }

  @PostMapping("/{registrationId}/attendance")
  public ApiResponse<Map<String, Boolean>> markAttendance(
    @PathVariable Long registrationId,
    @RequestBody RegistrationAttendanceCommand command
  ) {
    return ApiResponse.success(Map.of("marked", registrationService.markAttendance(registrationId, command)));
  }
}
