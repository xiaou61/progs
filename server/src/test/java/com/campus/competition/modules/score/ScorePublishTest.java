package com.campus.competition.modules.score;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.campus.competition.modules.competition.model.PublishCompetitionCommand;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.points.service.PointsService;
import com.campus.competition.modules.registration.model.RegisterCompetitionCommand;
import com.campus.competition.modules.registration.service.RegistrationService;
import com.campus.competition.modules.score.model.PublishResultCommand;
import com.campus.competition.modules.score.service.ScoreService;
import com.campus.competition.modules.submission.model.SubmitWorkCommand;
import com.campus.competition.modules.submission.service.SubmissionService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ScorePublishTest {

  @Test
  void shouldPublishResultsAndGrantPoints() {
    CompetitionService competitionService = new CompetitionService();
    Long competitionId = competitionService.publish(new PublishCompetitionCommand(
      1001L,
      "校园创新挑战赛",
      "围绕校园问题提出数字化创新方案",
      LocalDateTime.now().minusDays(5),
      LocalDateTime.now().plusHours(1),
      LocalDateTime.now().minusHours(4),
      LocalDateTime.now().plusHours(4),
      50
    ));
    RegistrationService registrationService = new RegistrationService(competitionService);
    registrationService.register(new RegisterCompetitionCommand(competitionId, 2001L));
    SubmissionService submissionService = new SubmissionService(competitionService, registrationService);
    submissionService.submit(new SubmitWorkCommand(
      competitionId,
      2001L,
      "https://files.example.com/work-v1.pptx",
      false
    ));
    PointsService pointsService = new PointsService();
    ScoreService scoreService = new ScoreService(competitionService, submissionService, pointsService);

    scoreService.publish(new PublishResultCommand(
      competitionId,
      2001L,
      95,
      1,
      "一等奖",
      30,
      "王老师",
      "作品完成度高"
    ));

    assertEquals(30, pointsService.queryAvailablePoints(2001L));
  }
}
