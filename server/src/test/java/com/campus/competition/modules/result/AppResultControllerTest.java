package com.campus.competition.modules.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.persistence.UserEntity;
import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.competition.model.PublishCompetitionCommand;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.points.service.PointsService;
import com.campus.competition.modules.registration.model.RegisterCompetitionCommand;
import com.campus.competition.modules.registration.service.RegistrationService;
import com.campus.competition.modules.result.controller.AppResultController;
import com.campus.competition.modules.result.model.CompetitionResultItem;
import com.campus.competition.modules.score.model.PublishResultCommand;
import com.campus.competition.modules.score.service.ScoreService;
import com.campus.competition.modules.submission.model.SubmitWorkCommand;
import com.campus.competition.modules.submission.service.SubmissionService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class AppResultControllerTest {

  @Test
  void shouldListCompetitionLeaderboardWithStudentIdentity() {
    CompetitionService competitionService = new CompetitionService();
    RegistrationService registrationService = new RegistrationService(competitionService);
    SubmissionService submissionService = new SubmissionService(competitionService, registrationService);
    PointsService pointsService = new PointsService();
    ScoreService scoreService = new ScoreService(competitionService, submissionService, pointsService);

    Long competitionId = competitionService.publish(new PublishCompetitionCommand(
      1001L,
      "公开结果验证赛",
      "用于验证比赛结果榜单返回获奖人信息",
      LocalDateTime.now().minusDays(2),
      LocalDateTime.now().plusDays(1),
      LocalDateTime.now().minusHours(2),
      LocalDateTime.now().plusDays(2),
      50
    ));

    registrationService.register(new RegisterCompetitionCommand(competitionId, 2001L));
    registrationService.register(new RegisterCompetitionCommand(competitionId, 2002L));
    submissionService.submit(new SubmitWorkCommand(competitionId, 2001L, "https://files.example.com/work-a.pptx", false));
    submissionService.submit(new SubmitWorkCommand(competitionId, 2002L, "https://files.example.com/work-b.pptx", false));
    scoreService.publish(new PublishResultCommand(competitionId, 2001L, 96, 1, "一等奖", 30, "王老师", "方案完整"));
    scoreService.publish(new PublishResultCommand(competitionId, 2002L, 90, 2, "二等奖", 20, "王老师", "继续完善"));

    UserMapper userMapper = mock(UserMapper.class);
    UserEntity firstUser = new UserEntity();
    firstUser.setId(2001L);
    firstUser.setStudentNo("S20260001");
    firstUser.setRealName("张同学");
    UserEntity secondUser = new UserEntity();
    secondUser.setId(2002L);
    secondUser.setStudentNo("S20260002");
    secondUser.setRealName("李同学");
    when(userMapper.selectBatchIds(any())).thenReturn(List.of(firstUser, secondUser));

    AppResultController controller = new AppResultController(scoreService, pointsService, userMapper);
    ApiResponse<List<CompetitionResultItem>> response = controller.listByCompetition(competitionId);

    assertEquals(0, response.getCode());
    assertEquals(2, response.getData().size());
    assertEquals("张同学", response.getData().get(0).studentName());
    assertEquals("S20260001", response.getData().get(0).studentNo());
    assertEquals("一等奖", response.getData().get(0).awardName());
  }
}
