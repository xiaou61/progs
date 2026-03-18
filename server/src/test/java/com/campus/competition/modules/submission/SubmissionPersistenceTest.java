package com.campus.competition.modules.submission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.campus.competition.modules.competition.model.PublishCompetitionCommand;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.registration.model.RegisterCompetitionCommand;
import com.campus.competition.modules.registration.service.RegistrationService;
import com.campus.competition.modules.submission.mapper.SubmissionMapper;
import com.campus.competition.modules.submission.model.SubmitWorkCommand;
import com.campus.competition.modules.submission.persistence.SubmissionEntity;
import com.campus.competition.modules.submission.service.SubmissionService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubmissionPersistenceTest {

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private RegistrationService registrationService;

  @Autowired
  private SubmissionService submissionService;

  @Autowired
  private SubmissionMapper submissionMapper;

  @Test
  void shouldPersistSubmissionAndVersionIntoDatabase() {
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
    registrationService.register(new RegisterCompetitionCommand(competitionId, 2001L));

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

    SubmissionEntity entity = submissionMapper.selectById(submissionId);

    assertNotNull(entity);
    assertEquals(2, entity.getVersionNo());
    assertEquals("https://files.example.com/work-v2.pptx", entity.getFileUrl());
  }
}
