package com.campus.competition.modules.submission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.campus.competition.modules.competition.model.PublishCompetitionCommand;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.registration.model.RegisterCompetitionCommand;
import com.campus.competition.modules.registration.service.RegistrationService;
import com.campus.competition.modules.submission.model.SubmissionSummary;
import com.campus.competition.modules.submission.model.SubmitWorkCommand;
import com.campus.competition.modules.submission.service.SubmissionService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class SubmissionServiceTest {

  @Test
  void shouldReplaceOldSubmissionWhenReuploadAllowed() {
    CompetitionService competitionService = new CompetitionService();
    Long competitionId = competitionService.publish(new PublishCompetitionCommand(
      1001L,
      "校园创新挑战赛",
      "围绕校园问题提出数字化创新方案",
      LocalDateTime.now().minusDays(3),
      LocalDateTime.now().plusHours(1),
      LocalDateTime.now().minusHours(2),
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
    Long submissionId = submissionService.submit(new SubmitWorkCommand(
      competitionId,
      2001L,
      "https://files.example.com/work-v2.pptx",
      true
    ));

    List<SubmissionSummary> submissions = submissionService.listByCompetition(competitionId);

    assertNotNull(submissionId);
    assertEquals(1, submissions.size());
    assertEquals(2, submissions.get(0).versionNo());
  }

  @Test
  void shouldListSubmissionsByUserInReverseSubmittedOrder() {
    CompetitionService competitionService = new CompetitionService();
    Long firstCompetitionId = competitionService.publish(new PublishCompetitionCommand(
      1001L,
      "校园创新挑战赛",
      "围绕校园问题提出数字化创新方案",
      LocalDateTime.now().minusDays(3),
      LocalDateTime.now().plusHours(1),
      LocalDateTime.now().minusHours(2),
      LocalDateTime.now().plusHours(4),
      50
    ));
    Long secondCompetitionId = competitionService.publish(new PublishCompetitionCommand(
      1001L,
      "蓝桥杯",
      "用于验证用户作品排序",
      LocalDateTime.now().minusDays(3),
      LocalDateTime.now().plusHours(1),
      LocalDateTime.now().minusHours(2),
      LocalDateTime.now().plusHours(4),
      50
    ));
    RegistrationService registrationService = new RegistrationService(competitionService);
    registrationService.register(new RegisterCompetitionCommand(firstCompetitionId, 2001L));
    registrationService.register(new RegisterCompetitionCommand(secondCompetitionId, 2001L));
    registrationService.register(new RegisterCompetitionCommand(firstCompetitionId, 2002L));
    SubmissionService submissionService = new SubmissionService(competitionService, registrationService);

    submissionService.submit(new SubmitWorkCommand(
      firstCompetitionId,
      2001L,
      "https://files.example.com/work-a.pptx",
      false
    ));
    submissionService.submit(new SubmitWorkCommand(
      secondCompetitionId,
      2001L,
      "https://files.example.com/work-b.pptx",
      false
    ));
    submissionService.submit(new SubmitWorkCommand(
      firstCompetitionId,
      2002L,
      "https://files.example.com/work-c.pptx",
      false
    ));

    List<SubmissionSummary> submissions = submissionService.listByUser(2001L);

    assertEquals(2, submissions.size());
    assertEquals(secondCompetitionId, submissions.get(0).competitionId());
    assertEquals(firstCompetitionId, submissions.get(1).competitionId());
    assertTrue(submissions.stream().allMatch(item -> item.userId().equals(2001L)));
  }
}
