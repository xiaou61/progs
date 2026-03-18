package com.campus.competition.modules.registration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.campus.competition.modules.competition.model.PublishCompetitionCommand;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.registration.model.RegisterCompetitionCommand;
import com.campus.competition.modules.registration.service.RegistrationService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class RegistrationServiceTest {

  @Test
  void shouldRegisterWhenQuotaAvailable() {
    CompetitionService competitionService = new CompetitionService();
    Long competitionId = competitionService.publish(new PublishCompetitionCommand(
      1001L,
      "校园创新挑战赛",
      "围绕校园问题提出数字化创新方案",
      LocalDateTime.now().minusDays(1),
      LocalDateTime.now().plusDays(3),
      LocalDateTime.now().plusDays(5),
      LocalDateTime.now().plusDays(5).plusHours(8),
      50
    ));
    RegistrationService registrationService = new RegistrationService(competitionService);

    Long registrationId = registrationService.register(new RegisterCompetitionCommand(
      competitionId,
      2001L
    ));

    assertNotNull(registrationId);
  }
}
