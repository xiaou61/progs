package com.campus.competition.modules.competition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.campus.competition.modules.competition.model.PublishCompetitionCommand;
import com.campus.competition.modules.competition.model.SaveCompetitionDraftCommand;
import com.campus.competition.modules.competition.model.UpdateCompetitionCommand;
import com.campus.competition.modules.competition.service.CompetitionService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CompetitionFeatureBehaviorTest {

  @Test
  void shouldPersistFeatureFlagsWithinMainSaveAndSortPinnedCompetitionsFirst() {
    CompetitionService competitionService = new CompetitionService();
    LocalDateTime now = LocalDateTime.of(2026, 3, 21, 12, 0);

    Long olderCompetitionId = competitionService.publish(new PublishCompetitionCommand(
      1001L,
      "较早创建的普通比赛",
      "用于验证置顶排序",
      now.minusDays(3),
      now.plusDays(2),
      now.plusDays(3),
      now.plusDays(4),
      60,
      "STUDENT_ONLY",
      2001L,
      false,
      false
    ));

    Long pinnedCompetitionId = competitionService.saveDraft(new SaveCompetitionDraftCommand(
      1001L,
      "准备置顶的比赛",
      "先保存草稿，再通过主保存更新置顶",
      now.minusDays(2),
      now.plusDays(2),
      now.plusDays(3),
      now.plusDays(4),
      80,
      "STUDENT_ONLY",
      2001L,
      false,
      false
    ));

    competitionService.update(pinnedCompetitionId, new UpdateCompetitionCommand(
      1001L,
      "准备置顶的比赛",
      "通过主保存直接写入推荐和置顶状态",
      now.minusDays(2),
      now.plusDays(2),
      now.plusDays(3),
      now.plusDays(4),
      80,
      "PUBLISHED",
      "STUDENT_ONLY",
      2001L,
      true,
      true
    ));

    Long newerCompetitionId = competitionService.publish(new PublishCompetitionCommand(
      1001L,
      "较晚创建的普通比赛",
      "用于验证非置顶比赛不会压过置顶比赛",
      now.minusDays(1),
      now.plusDays(2),
      now.plusDays(3),
      now.plusDays(4),
      90,
      "STUDENT_ONLY",
      2001L,
      false,
      false
    ));

    var managedCompetitions = competitionService.listManagedCompetitions();
    assertEquals(pinnedCompetitionId, managedCompetitions.get(0).id());
    assertTrue(managedCompetitions.get(0).recommended());
    assertTrue(managedCompetitions.get(0).pinned());
    assertEquals(newerCompetitionId, managedCompetitions.get(1).id());
    assertEquals(olderCompetitionId, managedCompetitions.get(2).id());

    var publicCompetitions = competitionService.listPublicCompetitions();
    assertEquals(pinnedCompetitionId, publicCompetitions.get(0).id());
    assertTrue(publicCompetitions.get(0).recommended());
    assertTrue(publicCompetitions.get(0).pinned());
  }
}
