package com.campus.competition.modules.checkin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.campus.competition.modules.checkin.mapper.CheckinMapper;
import com.campus.competition.modules.checkin.model.CheckInCommand;
import com.campus.competition.modules.checkin.persistence.CheckinEntity;
import com.campus.competition.modules.checkin.service.CheckinService;
import com.campus.competition.modules.competition.model.PublishCompetitionCommand;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.registration.model.RegisterCompetitionCommand;
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
class CheckinPersistenceTest {

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private RegistrationService registrationService;

  @Autowired
  private CheckinService checkinService;

  @Autowired
  private CheckinMapper checkinMapper;

  @Test
  void shouldPersistCheckinIntoDatabase() {
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
    registrationService.register(new RegisterCompetitionCommand(competitionId, 2001L));

    boolean checked = checkinService.checkIn(new CheckInCommand(competitionId, 2001L, "QR_CODE"));

    CheckinEntity entity = checkinMapper.selectOne(com.baomidou.mybatisplus.core.toolkit.Wrappers
      .<CheckinEntity>lambdaQuery()
      .eq(CheckinEntity::getCompetitionId, competitionId)
      .eq(CheckinEntity::getUserId, 2001L));

    assertEquals(true, checked);
    assertNotNull(entity);
  }
}
