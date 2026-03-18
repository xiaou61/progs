package com.campus.competition.modules.score.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.points.service.PointsService;
import com.campus.competition.modules.score.mapper.ScoreMapper;
import com.campus.competition.modules.score.model.PublishResultCommand;
import com.campus.competition.modules.score.model.ScoreSummary;
import com.campus.competition.modules.score.persistence.ScoreEntity;
import com.campus.competition.modules.submission.service.SubmissionService;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoreService {

  private final AtomicLong idGenerator = new AtomicLong(1);
  private final Map<Long, ScoreSummary> scores = new ConcurrentHashMap<>();
  private final CompetitionService competitionService;
  private final SubmissionService submissionService;
  private final PointsService pointsService;
  @Autowired(required = false)
  private ScoreMapper scoreMapper;

  public ScoreService(
    CompetitionService competitionService,
    SubmissionService submissionService,
    PointsService pointsService
  ) {
    this.competitionService = competitionService;
    this.submissionService = submissionService;
    this.pointsService = pointsService;
  }

  @Transactional
  public Long publish(PublishResultCommand command) {
    validateCommand(command);
    String certificateNo = buildCertificateNo(command.competitionId(), command.studentId());
    String certificateTitle = buildCertificateTitle(command.competitionId());
    if (scoreMapper != null) {
      ScoreEntity entity = new ScoreEntity();
      entity.setCompetitionId(command.competitionId());
      entity.setUserId(command.studentId());
      entity.setScore(command.score());
      entity.setRankNo(command.rank());
      entity.setAwardName(command.awardName());
      entity.setPoints(command.points());
      entity.setReviewerName(normalizeOptionalText(command.reviewerName()));
      entity.setReviewComment(normalizeOptionalText(command.reviewComment()));
      entity.setCertificateNo(certificateNo);
      entity.setCertificateTitle(certificateTitle);
      entity.setPublishedAt(LocalDateTime.now());
      scoreMapper.insert(entity);
      pointsService.grantPoints(
        command.studentId(),
        command.points(),
        "COMPETITION_RESULT",
        command.competitionId(),
        "比赛获奖积分发放"
      );
      return entity.getId();
    }

    long id = idGenerator.getAndIncrement();
    scores.put(id, new ScoreSummary(
      id,
      command.competitionId(),
      command.studentId(),
      command.score(),
      command.rank(),
      command.awardName(),
      command.points(),
      LocalDateTime.now(),
      normalizeOptionalText(command.reviewerName()),
      normalizeOptionalText(command.reviewComment()),
      certificateNo,
      certificateTitle
    ));
    pointsService.grantPoints(
      command.studentId(),
      command.points(),
      "COMPETITION_RESULT",
      command.competitionId(),
      "比赛获奖积分发放"
    );
    return id;
  }

  public List<ScoreSummary> listByCompetition(Long competitionId) {
    if (scoreMapper != null) {
      return scoreMapper.selectList(Wrappers.<ScoreEntity>lambdaQuery()
          .eq(ScoreEntity::getCompetitionId, competitionId)
          .orderByAsc(ScoreEntity::getRankNo))
        .stream()
        .map(this::toSummary)
        .toList();
    }
    return scores.values().stream()
      .filter(item -> item.competitionId().equals(competitionId))
      .sorted(Comparator.comparing(ScoreSummary::rank))
      .toList();
  }

  public List<ScoreSummary> listByStudent(Long studentId) {
    if (scoreMapper != null) {
      return scoreMapper.selectList(Wrappers.<ScoreEntity>lambdaQuery()
          .eq(ScoreEntity::getUserId, studentId)
          .orderByDesc(ScoreEntity::getPublishedAt))
        .stream()
        .map(this::toSummary)
        .toList();
    }
    return scores.values().stream()
      .filter(item -> item.studentId().equals(studentId))
      .sorted(Comparator.comparing(ScoreSummary::publishedAt).reversed())
      .toList();
  }

  private void validateCommand(PublishResultCommand command) {
    if (command.competitionId() == null) {
      throw new IllegalArgumentException("比赛不能为空");
    }
    competitionService.getCompetition(command.competitionId());
    if (command.studentId() == null) {
      throw new IllegalArgumentException("学生不能为空");
    }
    boolean submitted = submissionService.listByCompetition(command.competitionId()).stream()
      .anyMatch(item -> item.userId().equals(command.studentId()));
    if (!submitted) {
      throw new IllegalArgumentException("学生尚未提交作品");
    }
    if (command.score() < 0) {
      throw new IllegalArgumentException("分数不能为负数");
    }
    if (command.rank() <= 0) {
      throw new IllegalArgumentException("名次必须大于 0");
    }
    if (command.awardName() == null || command.awardName().isBlank()) {
      throw new IllegalArgumentException("奖项不能为空");
    }
    if (command.points() <= 0) {
      throw new IllegalArgumentException("积分必须大于 0");
    }
  }

  private ScoreSummary toSummary(ScoreEntity entity) {
    return new ScoreSummary(
      entity.getId(),
      entity.getCompetitionId(),
      entity.getUserId(),
      entity.getScore(),
      entity.getRankNo(),
      entity.getAwardName(),
      entity.getPoints(),
      entity.getPublishedAt(),
      entity.getReviewerName(),
      entity.getReviewComment(),
      entity.getCertificateNo(),
      entity.getCertificateTitle()
    );
  }

  private String buildCertificateNo(Long competitionId, Long studentId) {
    return "CERT-" + competitionId + "-" + studentId + "-" + System.currentTimeMillis();
  }

  private String buildCertificateTitle(Long competitionId) {
    return competitionService.getCompetition(competitionId).title() + "电子奖状";
  }

  private String normalizeOptionalText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
