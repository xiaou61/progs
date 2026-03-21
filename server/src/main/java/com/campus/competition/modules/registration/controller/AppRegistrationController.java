package com.campus.competition.modules.registration.controller;

import com.campus.competition.modules.auth.security.AuthContext;
import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.registration.model.CancelRegistrationCommand;
import com.campus.competition.modules.registration.model.RegisterCompetitionCommand;
import com.campus.competition.modules.registration.model.RegistrationSummary;
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
@RequestMapping("/api/app/registrations")
public class AppRegistrationController {

  private final RegistrationService registrationService;

  public AppRegistrationController(RegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  @PostMapping
  public ApiResponse<Map<String, Long>> register(@RequestBody RegisterCompetitionCommand command) {
    AuthContext.requireUser(command == null ? null : command.userId());
    Long registrationId = registrationService.register(command);
    return ApiResponse.success(Map.of("registrationId", registrationId));
  }

  @GetMapping("/competition/{competitionId}")
  public ApiResponse<List<RegistrationSummary>> listByCompetition(@PathVariable Long competitionId) {
    return ApiResponse.success(registrationService.listByCompetition(competitionId));
  }

  @GetMapping("/competition/{competitionId}/user/{userId}")
  public ApiResponse<RegistrationSummary> getByCompetitionAndUser(
    @PathVariable Long competitionId,
    @PathVariable Long userId
  ) {
    AuthContext.requireUser(userId);
    return ApiResponse.success(registrationService.findByCompetitionAndUser(competitionId, userId));
  }

  @GetMapping("/user/{userId}")
  public ApiResponse<List<RegistrationSummary>> listByUser(@PathVariable Long userId) {
    AuthContext.requireUser(userId);
    return ApiResponse.success(registrationService.listByUser(userId));
  }

  @PostMapping("/{registrationId}/cancel")
  public ApiResponse<Map<String, Boolean>> cancel(
    @PathVariable Long registrationId,
    @RequestBody CancelRegistrationCommand command
  ) {
    AuthContext.requireUser(command == null ? null : command.userId());
    return ApiResponse.success(Map.of("cancelled", registrationService.cancel(registrationId, command)));
  }
}
