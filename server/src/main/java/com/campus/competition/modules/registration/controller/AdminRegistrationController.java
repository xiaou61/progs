package com.campus.competition.modules.registration.controller;

import com.campus.competition.modules.checkin.model.CheckinReviewCommand;
import com.campus.competition.modules.checkin.model.CheckinSummary;
import com.campus.competition.modules.checkin.service.CheckinService;
import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.registration.model.ManualRegistrationCommand;
import com.campus.competition.modules.registration.model.RegistrationManageSummary;
import com.campus.competition.modules.registration.model.RegistrationAttendanceCommand;
import com.campus.competition.modules.registration.model.RegistrationSummary;
import com.campus.competition.modules.registration.model.RejectRegistrationCommand;
import com.campus.competition.modules.registration.service.RegistrationService;
import java.util.function.Function;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
  private final CheckinService checkinService;

  public AdminRegistrationController(RegistrationService registrationService, CheckinService checkinService) {
    this.registrationService = registrationService;
    this.checkinService = checkinService;
  }

  @GetMapping("/competition/{competitionId}")
  public ApiResponse<List<RegistrationManageSummary>> listByCompetition(@PathVariable Long competitionId) {
    List<RegistrationSummary> registrations = registrationService.listByCompetition(competitionId);
    Map<Long, CheckinSummary> checkinMap = checkinService.listByCompetition(competitionId).stream()
      .collect(Collectors.toMap(CheckinSummary::userId, Function.identity(), (left, right) -> right));
    return ApiResponse.success(registrations.stream()
      .map(item -> toManageSummary(item, checkinMap.get(item.userId())))
      .toList());
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
    boolean marked = registrationService.markAttendance(registrationId, command);
    checkinService.syncWithAttendance(registrationId, command == null ? null : command.attendanceStatus());
    return ApiResponse.success(Map.of("marked", marked));
  }

  @PostMapping("/{registrationId}/checkin-review")
  public ApiResponse<Map<String, Boolean>> reviewCheckin(
    @PathVariable Long registrationId,
    @RequestBody CheckinReviewCommand command
  ) {
    return ApiResponse.success(Map.of("reviewed", checkinService.reviewCheckinByRegistration(registrationId, command)));
  }

  private RegistrationManageSummary toManageSummary(RegistrationSummary registration, CheckinSummary checkin) {
    return new RegistrationManageSummary(
      registration.id(),
      registration.competitionId(),
      registration.userId(),
      registration.status(),
      registration.attendanceStatus(),
      registration.remark(),
      checkin == null ? null : checkin.status(),
      checkin == null ? null : checkin.method(),
      checkin == null ? null : checkin.checkedAt(),
      checkin == null ? null : checkin.reviewedAt(),
      checkin == null ? null : checkin.reviewRemark()
    );
  }
}
