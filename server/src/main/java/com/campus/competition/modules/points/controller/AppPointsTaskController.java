package com.campus.competition.modules.points.controller;

import com.campus.competition.modules.auth.security.AuthContext;
import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.points.model.DailyCheckinCommand;
import com.campus.competition.modules.points.model.DailyTaskSummary;
import com.campus.competition.modules.points.model.PersonalCompetitionOverview;
import com.campus.competition.modules.points.model.ShareCompetitionCommand;
import com.campus.competition.modules.points.service.PointsService;
import com.campus.competition.modules.registration.model.RegistrationSummary;
import com.campus.competition.modules.registration.service.RegistrationService;
import com.campus.competition.modules.score.model.ScoreSummary;
import com.campus.competition.modules.score.service.ScoreService;
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
@RequestMapping("/api/app/points/tasks")
public class AppPointsTaskController {

  private final PointsService pointsService;
  private final CompetitionService competitionService;
  private final RegistrationService registrationService;
  private final SubmissionService submissionService;
  private final ScoreService scoreService;

  public AppPointsTaskController(
    PointsService pointsService,
    CompetitionService competitionService,
    RegistrationService registrationService,
    SubmissionService submissionService,
    ScoreService scoreService
  ) {
    this.pointsService = pointsService;
    this.competitionService = competitionService;
    this.registrationService = registrationService;
    this.submissionService = submissionService;
    this.scoreService = scoreService;
  }

  @GetMapping("/{userId}")
  public ApiResponse<Map<String, Object>> overview(@PathVariable Long userId) {
    AuthContext.requireUser(userId);
    return ApiResponse.success(Map.of(
      "task", pointsService.getDailyTaskSummary(userId),
      "overview", buildOverview(userId)
    ));
  }

  @PostMapping("/checkin")
  public ApiResponse<Map<String, Object>> checkin(@RequestBody DailyCheckinCommand command) {
    AuthContext.requireUser(command == null ? null : command.userId());
    int availablePoints = pointsService.completeDailyCheckin(command.userId());
    return ApiResponse.success(Map.of(
      "granted", true,
      "changeAmount", PointsService.DAILY_CHECKIN_POINTS,
      "availablePoints", availablePoints
    ));
  }

  @PostMapping("/share")
  public ApiResponse<Map<String, Object>> share(@RequestBody ShareCompetitionCommand command) {
    AuthContext.requireUser(command == null ? null : command.userId());
    competitionService.getCompetition(command.competitionId());
    int availablePoints = pointsService.completeCompetitionShare(command.userId(), command.competitionId());
    return ApiResponse.success(Map.of(
      "granted", true,
      "changeAmount", PointsService.COMPETITION_SHARE_POINTS,
      "availablePoints", availablePoints
    ));
  }

  private PersonalCompetitionOverview buildOverview(Long userId) {
    List<RegistrationSummary> registrations = registrationService.listByUser(userId);
    List<ScoreSummary> results = scoreService.listByStudent(userId);
    int totalAwardPoints = results.stream()
      .mapToInt(ScoreSummary::points)
      .sum();
    int totalPoints = pointsService.getAccount(userId).availablePoints();
    long registeredCompetitionCount = registrations.stream()
      .filter(item -> "REGISTERED".equals(item.status()))
      .count();
    int submittedWorkCount = submissionService.listByUser(userId).size();

    return new PersonalCompetitionOverview(
      userId,
      (int) registeredCompetitionCount,
      submittedWorkCount,
      results.size(),
      totalAwardPoints,
      totalPoints
    );
  }
}
