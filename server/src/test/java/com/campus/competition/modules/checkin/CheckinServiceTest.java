package com.campus.competition.modules.checkin;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.campus.competition.modules.checkin.model.CheckInCommand;
import com.campus.competition.modules.checkin.service.CheckinService;
import com.campus.competition.modules.competition.model.PublishCompetitionCommand;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.registration.model.RegisterCompetitionCommand;
import com.campus.competition.modules.registration.service.RegistrationService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CheckinServiceTest {

  @Test
  void shouldCheckInWithinTimeWindow() {
    CompetitionService competitionService = new CompetitionService();
    Long competitionId = competitionService.publish(new PublishCompetitionCommand(
      1001L,
      "校园创新挑战赛",
      "围绕校园问题提出数字化创新方案",
      LocalDateTime.now().minusDays(3),
      LocalDateTime.now().plusHours(1),
      LocalDateTime.now().minusHours(1),
      LocalDateTime.now().plusHours(2),
      50
    ));
    RegistrationService registrationService = new RegistrationService(competitionService);
    registrationService.register(new RegisterCompetitionCommand(competitionId, 2001L));
    CheckinService checkinService = new CheckinService(competitionService, registrationService);

    boolean checked = checkinService.checkIn(new CheckInCommand(competitionId, 2001L, "QR_CODE"));

    assertTrue(checked);
  }
}
