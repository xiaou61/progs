package com.campus.competition.modules.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.campus.competition.modules.competition.model.PublishCompetitionCommand;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.points.mapper.PointsAccountMapper;
import com.campus.competition.modules.points.mapper.PointsRecordMapper;
import com.campus.competition.modules.points.persistence.PointsAccountEntity;
import com.campus.competition.modules.points.persistence.PointsRecordEntity;
import com.campus.competition.modules.points.service.PointsService;
import com.campus.competition.modules.registration.model.RegisterCompetitionCommand;
import com.campus.competition.modules.registration.service.RegistrationService;
import com.campus.competition.modules.score.mapper.ScoreMapper;
import com.campus.competition.modules.score.model.PublishResultCommand;
import com.campus.competition.modules.score.model.ScoreSummary;
import com.campus.competition.modules.score.persistence.ScoreEntity;
import com.campus.competition.modules.score.service.ScoreService;
import com.campus.competition.modules.submission.model.SubmitWorkCommand;
import com.campus.competition.modules.submission.service.SubmissionService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ScorePointsPersistenceTest {

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private RegistrationService registrationService;

  @Autowired
  private SubmissionService submissionService;

  @Autowired
  private ScoreService scoreService;

  @Autowired
  private PointsService pointsService;

  @Autowired
  private ScoreMapper scoreMapper;

  @Autowired
  private PointsAccountMapper pointsAccountMapper;

  @Autowired
  private PointsRecordMapper pointsRecordMapper;

  @Test
  void shouldPersistScoreAndPointsIntoDatabase() {
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
    registrationService.register(new RegisterCompetitionCommand(competitionId, 2001L));
    submissionService.submit(new SubmitWorkCommand(
      competitionId,
      2001L,
      "https://files.example.com/work-v1.pptx",
      false
    ));

    Long scoreId = scoreService.publish(new PublishResultCommand(
      competitionId,
      2001L,
      95,
      1,
      "一等奖",
      30,
      "王老师",
      "方案完整，建议继续深化"
    ));

    ScoreEntity scoreEntity = scoreMapper.selectById(scoreId);
    PointsAccountEntity accountEntity = pointsAccountMapper.selectById(2001L);
    PointsRecordEntity recordEntity = pointsRecordMapper.selectOne(com.baomidou.mybatisplus.core.toolkit.Wrappers
      .<PointsRecordEntity>lambdaQuery()
      .eq(PointsRecordEntity::getUserId, 2001L)
      .orderByDesc(PointsRecordEntity::getId)
      .last("limit 1"));
    List<ScoreSummary> results = scoreService.listByStudent(2001L);

    assertNotNull(scoreEntity);
    assertNotNull(accountEntity);
    assertNotNull(recordEntity);
    assertEquals(30, accountEntity.getAvailablePoints());
    assertEquals(30, pointsService.queryAvailablePoints(2001L));
    assertEquals(30, results.get(0).points());
    assertEquals("王老师", results.get(0).reviewerName());
    assertEquals("方案完整，建议继续深化", results.get(0).reviewComment());
  }
}
