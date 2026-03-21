package com.campus.competition.modules.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.campus.competition.modules.competition.model.PublishCompetitionCommand;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.registration.model.RegisterCompetitionCommand;
import com.campus.competition.modules.registration.service.RegistrationService;
import com.campus.competition.modules.review.model.ReviewTaskSummary;
import com.campus.competition.modules.review.service.ReviewService;
import com.campus.competition.modules.submission.model.SubmitWorkCommand;
import com.campus.competition.modules.submission.service.SubmissionService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReviewServiceTest {

  @Test
  void shouldExposeSubmissionFileMetadataInReviewTasks() {
    CompetitionService competitionService = new CompetitionService();
    Long competitionId = competitionService.publish(new PublishCompetitionCommand(
      1001L,
      "评审文件下载验证赛",
      "用于验证评审任务中带出作品文件下载地址",
      LocalDateTime.now().minusDays(1),
      LocalDateTime.now().plusDays(1),
      LocalDateTime.now().minusHours(2),
      LocalDateTime.now().plusHours(4),
      50
    ));

    RegistrationService registrationService = new RegistrationService(competitionService);
    registrationService.register(new RegisterCompetitionCommand(competitionId, 2001L));

    SubmissionService submissionService = new SubmissionService(competitionService, registrationService);
    submissionService.submit(new SubmitWorkCommand(
      competitionId,
      2001L,
      "/uploads/submissions/review-work-v1.pptx",
      false
    ));

    ReviewService reviewService = new ReviewService(submissionService);
    List<ReviewTaskSummary> tasks = reviewService.listTasks(competitionId);

    assertEquals(1, tasks.size());
    assertEquals("/uploads/submissions/review-work-v1.pptx", tasks.get(0).fileUrl());
    assertEquals(1, tasks.get(0).versionNo());
    assertNotNull(tasks.get(0).submittedAt());
  }
}
