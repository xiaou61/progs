package com.campus.competition.modules.competition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.campus.competition.modules.competition.mapper.CompetitionMapper;
import com.campus.competition.modules.competition.model.PublishCompetitionCommand;
import com.campus.competition.modules.competition.persistence.CompetitionEntity;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.registration.mapper.RegistrationMapper;
import com.campus.competition.modules.registration.model.RegisterCompetitionCommand;
import com.campus.competition.modules.registration.persistence.RegistrationEntity;
import com.campus.competition.modules.registration.service.RegistrationService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CompetitionRegistrationPersistenceTest {

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private RegistrationService registrationService;

  @Autowired
  private CompetitionMapper competitionMapper;

  @Autowired
  private RegistrationMapper registrationMapper;

  @Test
  void shouldPersistCompetitionAndRegistrationIntoDatabase() {
    Long competitionId = competitionService.publish(new PublishCompetitionCommand(
      1001L,
      "校园创新挑战赛",
      "围绕校园问题提出数字化创新方案",
      LocalDateTime.now().minusDays(1),
      LocalDateTime.now().plusDays(2),
      LocalDateTime.now().plusDays(3),
      LocalDateTime.now().plusDays(3).plusHours(8),
      50
    ));

    Long registrationId = registrationService.register(new RegisterCompetitionCommand(
      competitionId,
      2001L
    ));

    CompetitionEntity competitionEntity = competitionMapper.selectById(competitionId);
    RegistrationEntity registrationEntity = registrationMapper.selectById(registrationId);

    assertNotNull(competitionEntity);
    assertNotNull(registrationEntity);
    assertEquals(1, registrationService.listByCompetition(competitionId).size());
  }
}
