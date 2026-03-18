package com.campus.competition.modules.competition;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.campus.competition.modules.competition.model.PublishCompetitionCommand;
import com.campus.competition.modules.competition.service.CompetitionService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CompetitionPublishTest {

  @Test
  void shouldPublishCompetitionWhenRequiredFieldsPresent() {
    CompetitionService competitionService = new CompetitionService();
    PublishCompetitionCommand command = new PublishCompetitionCommand(
      1001L,
      "校园创新挑战赛",
      "围绕校园问题提出数字化创新方案",
      LocalDateTime.of(2026, 4, 1, 8, 0),
      LocalDateTime.of(2026, 4, 10, 18, 0),
      LocalDateTime.of(2026, 4, 15, 9, 0),
      LocalDateTime.of(2026, 4, 15, 18, 0),
      200
    );

    Long competitionId = competitionService.publish(command);

    assertNotNull(competitionId);
  }
}
