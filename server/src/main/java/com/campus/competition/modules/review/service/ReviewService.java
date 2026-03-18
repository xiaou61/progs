package com.campus.competition.modules.review.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.review.mapper.ReviewTaskMapper;
import com.campus.competition.modules.review.model.ReviewTaskSummary;
import com.campus.competition.modules.review.model.SubmitReviewCommand;
import com.campus.competition.modules.review.persistence.ReviewTaskEntity;
import com.campus.competition.modules.submission.model.SubmissionSummary;
import com.campus.competition.modules.submission.service.SubmissionService;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

  private static final String STATUS_PENDING = "PENDING";
  private static final String STATUS_COMPLETED = "COMPLETED";

  private final AtomicLong idGenerator = new AtomicLong(1);
  private final Map<Long, ReviewTaskSummary> reviews = new ConcurrentHashMap<>();
  private final SubmissionService submissionService;
  private final ReviewTaskMapper reviewTaskMapper;

  public ReviewService(SubmissionService submissionService) {
    this.submissionService = submissionService;
    this.reviewTaskMapper = null;
  }

  @Autowired
  public ReviewService(SubmissionService submissionService, ReviewTaskMapper reviewTaskMapper) {
    this.submissionService = submissionService;
    this.reviewTaskMapper = reviewTaskMapper;
  }

  public List<ReviewTaskSummary> listTasks(Long competitionId) {
    if (competitionId == null) {
      throw new IllegalArgumentException("比赛不能为空");
    }

    List<SubmissionSummary> submissions = submissionService.listByCompetition(competitionId);
    if (reviewTaskMapper != null) {
      Map<Long, ReviewTaskEntity> existingTaskMap = reviewTaskMapper.selectList(Wrappers.<ReviewTaskEntity>lambdaQuery()
          .eq(ReviewTaskEntity::getCompetitionId, competitionId))
        .stream()
        .collect(Collectors.toMap(ReviewTaskEntity::getSubmissionId, Function.identity(), (left, right) -> right));
      return submissions.stream()
        .map(submission -> toSummary(submission, existingTaskMap.get(submission.id())))
        .sorted(Comparator.comparing(ReviewTaskSummary::submissionId))
        .toList();
    }

    return submissions.stream()
      .map(submission -> toSummary(submission, reviews.get(submission.id())))
      .sorted(Comparator.comparing(ReviewTaskSummary::submissionId))
      .toList();
  }

  public boolean submitReview(SubmitReviewCommand command) {
    validateCommand(command);
    SubmissionSummary submission = submissionService.listByCompetition(command.competitionId()).stream()
      .filter(item -> item.id().equals(command.submissionId()) && item.userId().equals(command.studentId()))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("作品提交记录不存在"));

    if (reviewTaskMapper != null) {
      ReviewTaskEntity existing = reviewTaskMapper.selectOne(Wrappers.<ReviewTaskEntity>lambdaQuery()
        .eq(ReviewTaskEntity::getCompetitionId, command.competitionId())
        .eq(ReviewTaskEntity::getSubmissionId, command.submissionId()));
      ReviewTaskEntity entity = existing == null ? new ReviewTaskEntity() : existing;
      if (entity.getCreatedAt() == null) {
        entity.setCreatedAt(LocalDateTime.now());
      }
      entity.setCompetitionId(command.competitionId());
      entity.setSubmissionId(submission.id());
      entity.setStudentId(command.studentId());
      entity.setReviewerName(command.reviewerName().trim());
      entity.setStatus(STATUS_COMPLETED);
      entity.setReviewComment(command.reviewComment().trim());
      entity.setSuggestedScore(command.suggestedScore());
      entity.setReviewedAt(LocalDateTime.now());
      entity.setUpdatedAt(LocalDateTime.now());
      if (existing == null) {
        reviewTaskMapper.insert(entity);
      } else {
        reviewTaskMapper.updateById(entity);
      }
      return true;
    }

    long id = reviews.entrySet().stream()
      .filter(entry -> entry.getValue().submissionId().equals(command.submissionId()))
      .map(Map.Entry::getKey)
      .findFirst()
      .orElseGet(idGenerator::getAndIncrement);
    reviews.put(id, new ReviewTaskSummary(
      command.competitionId(),
      command.submissionId(),
      command.studentId(),
      command.reviewerName().trim(),
      STATUS_COMPLETED,
      command.reviewComment().trim(),
      command.suggestedScore(),
      LocalDateTime.now()
    ));
    return true;
  }

  private void validateCommand(SubmitReviewCommand command) {
    if (command == null || command.competitionId() == null) {
      throw new IllegalArgumentException("比赛不能为空");
    }
    if (command.submissionId() == null) {
      throw new IllegalArgumentException("作品不能为空");
    }
    if (command.studentId() == null) {
      throw new IllegalArgumentException("学生不能为空");
    }
    if (command.reviewerName() == null || command.reviewerName().isBlank()) {
      throw new IllegalArgumentException("评审老师不能为空");
    }
    if (command.reviewComment() == null || command.reviewComment().isBlank()) {
      throw new IllegalArgumentException("评审意见不能为空");
    }
    if (command.suggestedScore() == null || command.suggestedScore() < 0) {
      throw new IllegalArgumentException("建议分数不能为空且不能为负数");
    }
  }

  private ReviewTaskSummary toSummary(SubmissionSummary submission, ReviewTaskEntity entity) {
    if (entity == null) {
      return new ReviewTaskSummary(
        submission.competitionId(),
        submission.id(),
        submission.userId(),
        null,
        STATUS_PENDING,
        null,
        null,
        null
      );
    }
    return new ReviewTaskSummary(
      entity.getCompetitionId(),
      entity.getSubmissionId(),
      entity.getStudentId(),
      entity.getReviewerName(),
      entity.getStatus(),
      entity.getReviewComment(),
      entity.getSuggestedScore(),
      entity.getReviewedAt()
    );
  }

  private ReviewTaskSummary toSummary(SubmissionSummary submission, ReviewTaskSummary summary) {
    if (summary != null) {
      return summary;
    }
    return new ReviewTaskSummary(
      submission.competitionId(),
      submission.id(),
      submission.userId(),
      null,
      STATUS_PENDING,
      null,
      null,
      null
    );
  }
}
