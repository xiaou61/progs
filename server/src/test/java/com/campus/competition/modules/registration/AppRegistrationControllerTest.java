package com.campus.competition.modules.registration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.campus.competition.modules.auth.security.AuthContext;
import com.campus.competition.modules.auth.security.AuthPrincipal;
import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.competition.model.PublishCompetitionCommand;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.registration.controller.AppRegistrationController;
import com.campus.competition.modules.registration.model.RegisterCompetitionCommand;
import com.campus.competition.modules.registration.model.RegistrationSummary;
import com.campus.competition.modules.registration.service.RegistrationService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class AppRegistrationControllerTest {

  @AfterEach
  void clearRequestContext() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  void shouldListCurrentUserRegistrations() {
    CompetitionService competitionService = new CompetitionService();
    Long firstCompetitionId = competitionService.publish(new PublishCompetitionCommand(
      1001L,
      "校园创新挑战赛",
      "用于验证我的比赛列表",
      LocalDateTime.now().minusDays(2),
      LocalDateTime.now().plusDays(1),
      LocalDateTime.now().minusHours(1),
      LocalDateTime.now().plusDays(2),
      50
    ));
    Long secondCompetitionId = competitionService.publish(new PublishCompetitionCommand(
      1001L,
      "蓝桥杯",
      "用于验证只返回当前用户报名记录",
      LocalDateTime.now().minusDays(2),
      LocalDateTime.now().plusDays(1),
      LocalDateTime.now().minusHours(1),
      LocalDateTime.now().plusDays(2),
      50
    ));

    RegistrationService registrationService = new RegistrationService(competitionService);
    registrationService.register(new RegisterCompetitionCommand(firstCompetitionId, 2001L));
    registrationService.register(new RegisterCompetitionCommand(secondCompetitionId, 2002L));

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(AuthContext.REQUEST_ATTRIBUTE, new AuthPrincipal(2001L, "S20260001", "STUDENT"));
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    AppRegistrationController controller = new AppRegistrationController(registrationService);
    ApiResponse<List<RegistrationSummary>> response = controller.listByUser(2001L);

    assertEquals(0, response.getCode());
    assertEquals(1, response.getData().size());
    assertEquals(firstCompetitionId, response.getData().get(0).competitionId());
    assertEquals(2001L, response.getData().get(0).userId());
  }
}
