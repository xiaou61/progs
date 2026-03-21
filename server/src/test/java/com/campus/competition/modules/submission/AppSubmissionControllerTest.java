package com.campus.competition.modules.submission;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.campus.competition.modules.auth.security.AuthContext;
import com.campus.competition.modules.auth.security.AuthPrincipal;
import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.competition.model.PublishCompetitionCommand;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.registration.model.RegisterCompetitionCommand;
import com.campus.competition.modules.registration.service.RegistrationService;
import com.campus.competition.modules.submission.controller.AppSubmissionController;
import com.campus.competition.modules.submission.model.SubmissionSummary;
import com.campus.competition.modules.submission.model.SubmitWorkCommand;
import com.campus.competition.modules.submission.service.SubmissionService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class AppSubmissionControllerTest {

  @AfterEach
  void clearRequestContext() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  void shouldListCurrentUserSubmissions() {
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
      "用于验证个人作品列表",
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
    submissionService.submit(new SubmitWorkCommand(firstCompetitionId, 2001L, "https://files.example.com/work-a.pptx", false));
    submissionService.submit(new SubmitWorkCommand(secondCompetitionId, 2001L, "https://files.example.com/work-b.pptx", false));
    submissionService.submit(new SubmitWorkCommand(firstCompetitionId, 2002L, "https://files.example.com/work-c.pptx", false));

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(AuthContext.REQUEST_ATTRIBUTE, new AuthPrincipal(2001L, "S20260001", "STUDENT"));
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    AppSubmissionController controller = new AppSubmissionController(submissionService);
    ApiResponse<List<SubmissionSummary>> response = controller.listByUser(2001L);

    assertEquals(0, response.getCode());
    assertEquals(2, response.getData().size());
    assertEquals(2001L, response.getData().get(0).userId());
    assertEquals(2001L, response.getData().get(1).userId());
  }
}
