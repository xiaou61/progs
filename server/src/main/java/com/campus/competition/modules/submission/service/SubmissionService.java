package com.campus.competition.modules.submission.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.audit.service.ContentAuditService;
import com.campus.competition.modules.competition.model.CompetitionDetail;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.registration.service.RegistrationService;
import com.campus.competition.modules.submission.mapper.SubmissionMapper;
import com.campus.competition.modules.submission.model.SubmissionSummary;
import com.campus.competition.modules.submission.model.SubmitWorkCommand;
import com.campus.competition.modules.submission.persistence.SubmissionEntity;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubmissionService {

  private final AtomicLong idGenerator = new AtomicLong(1);
  private final Map<Long, SubmissionSummary> submissions = new ConcurrentHashMap<>();
  private final CompetitionService competitionService;
  private final RegistrationService registrationService;
  @Autowired(required = false)
  private ContentAuditService contentAuditService;
  @Autowired(required = false)
  private SubmissionMapper submissionMapper;

  public SubmissionService(CompetitionService competitionService, RegistrationService registrationService) {
    this.competitionService = competitionService;
    this.registrationService = registrationService;
  }

  public Long submit(SubmitWorkCommand command) {
    CompetitionDetail competition = competitionService.getCompetition(command.competitionId());
    validateCommand(command, competition);
    if (submissionMapper != null) {
      return submitWithDatabase(command);
    }

    SubmissionSummary existing = submissions.values().stream()
      .filter(item -> item.competitionId().equals(command.competitionId()) && item.userId().equals(command.userId()))
      .findFirst()
      .orElse(null);
    if (existing == null) {
      long id = idGenerator.getAndIncrement();
      submissions.put(id, new SubmissionSummary(
        id,
        command.competitionId(),
        command.userId(),
        command.fileUrl(),
        1,
        LocalDateTime.now()
      ));
      return id;
    }
    if (!command.reuploadAllowed()) {
      throw new IllegalArgumentException("当前作品已提交，如需覆盖请开启重新上传");
    }

    submissions.put(existing.id(), new SubmissionSummary(
      existing.id(),
      existing.competitionId(),
      existing.userId(),
      command.fileUrl(),
      existing.versionNo() + 1,
      LocalDateTime.now()
    ));
    return existing.id();
  }

  public List<SubmissionSummary> listByCompetition(Long competitionId) {
    if (submissionMapper != null) {
      return submissionMapper.selectList(Wrappers.<SubmissionEntity>lambdaQuery()
          .eq(SubmissionEntity::getCompetitionId, competitionId)
          .orderByAsc(SubmissionEntity::getId))
        .stream()
        .map(this::toSummary)
        .toList();
    }
    return submissions.values().stream()
      .filter(item -> item.competitionId().equals(competitionId))
      .sorted(Comparator.comparing(SubmissionSummary::id))
      .toList();
  }

  public List<SubmissionSummary> listByUser(Long userId) {
    if (userId == null) {
      throw new IllegalArgumentException("用户不能为空");
    }
    if (submissionMapper != null) {
      return submissionMapper.selectList(Wrappers.<SubmissionEntity>lambdaQuery()
          .eq(SubmissionEntity::getUserId, userId)
          .orderByDesc(SubmissionEntity::getSubmittedAt)
          .orderByDesc(SubmissionEntity::getId))
        .stream()
        .map(this::toSummary)
        .toList();
    }
    return submissions.values().stream()
      .filter(item -> item.userId().equals(userId))
      .sorted(Comparator
        .comparing(SubmissionSummary::submittedAt, Comparator.reverseOrder())
        .thenComparing(SubmissionSummary::id, Comparator.reverseOrder()))
      .toList();
  }

  private void validateCommand(SubmitWorkCommand command, CompetitionDetail competition) {
    if (command.competitionId() == null) {
      throw new IllegalArgumentException("比赛不能为空");
    }
    if (command.userId() == null) {
      throw new IllegalArgumentException("用户不能为空");
    }
    if (command.fileUrl() == null || command.fileUrl().isBlank()) {
      throw new IllegalArgumentException("作品文件不能为空");
    }

    if (!registrationService.hasActiveRegistration(command.competitionId(), command.userId())) {
      throw new IllegalArgumentException("请先完成报名再提交作品");
    }

    LocalDateTime now = LocalDateTime.now();
    if (now.isBefore(competition.startAt()) || now.isAfter(competition.endAt())) {
      throw new IllegalArgumentException("当前不在作品提交时间内");
    }
    if (contentAuditService != null) {
      contentAuditService.auditSubmission(command.competitionId(), command.userId(), command.fileUrl());
    }
  }

  private Long submitWithDatabase(SubmitWorkCommand command) {
    SubmissionEntity existing = submissionMapper.selectOne(Wrappers.<SubmissionEntity>lambdaQuery()
      .eq(SubmissionEntity::getCompetitionId, command.competitionId())
      .eq(SubmissionEntity::getUserId, command.userId()));
    if (existing == null) {
      SubmissionEntity entity = new SubmissionEntity();
      entity.setCompetitionId(command.competitionId());
      entity.setUserId(command.userId());
      entity.setFileUrl(command.fileUrl());
      entity.setVersionNo(1);
      entity.setSubmittedAt(LocalDateTime.now());
      submissionMapper.insert(entity);
      return entity.getId();
    }
    if (!command.reuploadAllowed()) {
      throw new IllegalArgumentException("当前作品已提交，如需覆盖请开启重新上传");
    }

    existing.setFileUrl(command.fileUrl());
    existing.setVersionNo(existing.getVersionNo() + 1);
    existing.setSubmittedAt(LocalDateTime.now());
    submissionMapper.updateById(existing);
    return existing.getId();
  }

  private SubmissionSummary toSummary(SubmissionEntity entity) {
    return new SubmissionSummary(
      entity.getId(),
      entity.getCompetitionId(),
      entity.getUserId(),
      entity.getFileUrl(),
      entity.getVersionNo(),
      entity.getSubmittedAt()
    );
  }
}
