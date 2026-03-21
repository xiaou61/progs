package com.campus.competition.modules.registration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.campus.competition.modules.competition.model.PublishCompetitionCommand;
import com.campus.competition.modules.competition.model.UpdateCompetitionCommand;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.registration.model.CancelRegistrationCommand;
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

  @Test
  void shouldRejectCancelAfterSignupDeadline() {
    CompetitionService competitionService = new CompetitionService();
    LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
    Long competitionId = competitionService.publish(new PublishCompetitionCommand(
      1001L,
      "截止后不可取消报名验证赛",
      "用于验证报名截止后不能取消报名",
      now.minusDays(1),
      now.plusHours(1),
      now.plusDays(1),
      now.plusDays(1).plusHours(2),
      20
    ));
    RegistrationService registrationService = new RegistrationService(competitionService);
    Long registrationId = registrationService.register(new RegisterCompetitionCommand(
      competitionId,
      2001L
    ));

    competitionService.update(competitionId, new UpdateCompetitionCommand(
      1001L,
      "截止后不可取消报名验证赛",
      "用于验证报名截止后不能取消报名",
      now.minusDays(1),
      now.minusMinutes(1),
      now.plusDays(1),
      now.plusDays(1).plusHours(2),
      20,
      "PUBLISHED"
    ));

    IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
      registrationService.cancel(registrationId, new CancelRegistrationCommand(2001L))
    );

    assertEquals("报名截止后不能取消报名", error.getMessage());
  }
}
