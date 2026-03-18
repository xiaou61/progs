package com.campus.competition.modules.competition.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.audit.service.ContentAuditService;
import com.campus.competition.modules.competition.mapper.CompetitionMapper;
import com.campus.competition.modules.competition.model.CompetitionDetail;
import com.campus.competition.modules.competition.model.CompetitionDraftSummary;
import com.campus.competition.modules.competition.model.CompetitionFeatureCommand;
import com.campus.competition.modules.competition.model.CompetitionSummary;
import com.campus.competition.modules.competition.model.PublishCompetitionCommand;
import com.campus.competition.modules.competition.model.SaveCompetitionDraftCommand;
import com.campus.competition.modules.competition.model.UpdateCompetitionCommand;
import com.campus.competition.modules.competition.persistence.CompetitionEntity;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompetitionService {

  private final AtomicLong idGenerator = new AtomicLong(1);
  private final Map<Long, CompetitionDetail> competitions = new ConcurrentHashMap<>();
  private final CompetitionMapper competitionMapper;
  @Autowired(required = false)
  private ContentAuditService contentAuditService;

  public CompetitionService() {
    this.competitionMapper = null;
  }

  @Autowired
  public CompetitionService(CompetitionMapper competitionMapper) {
    this.competitionMapper = competitionMapper;
  }

  public Long publish(PublishCompetitionCommand command) {
    validateCommand(
      command.organizerId(),
      command.title(),
      command.description(),
      command.signupStartAt(),
      command.signupEndAt(),
      command.startAt(),
      command.endAt(),
      command.quota()
    );
    if (competitionMapper != null) {
      CompetitionEntity entity = buildEntity(command.organizerId(), command.title(), command.description(),
        command.signupStartAt(), command.signupEndAt(), command.startAt(), command.endAt(), command.quota(), "PUBLISHED");
      competitionMapper.insert(entity);
      return entity.getId();
    }
    long id = idGenerator.getAndIncrement();
    CompetitionDetail detail = buildDetail(id, command.organizerId(), command.title(), command.description(),
      command.signupStartAt(), command.signupEndAt(), command.startAt(), command.endAt(), command.quota(), "PUBLISHED", false, false);
    competitions.put(id, detail);
    return id;
  }

  public List<CompetitionSummary> listPublicCompetitions() {
    if (competitionMapper != null) {
      return competitionMapper.selectList(Wrappers.<CompetitionEntity>lambdaQuery()
          .eq(CompetitionEntity::getStatus, "PUBLISHED")
          .orderByAsc(CompetitionEntity::getId))
        .stream()
        .map(this::toSummary)
        .toList();
    }
    return competitions.values().stream()
      .filter(item -> "PUBLISHED".equals(item.status()))
      .sorted(Comparator.comparing(CompetitionDetail::id))
      .map(this::toSummary)
      .toList();
  }

  public List<CompetitionDraftSummary> listManagedCompetitions() {
    if (competitionMapper != null) {
      return competitionMapper.selectList(Wrappers.<CompetitionEntity>lambdaQuery().orderByDesc(CompetitionEntity::getId))
        .stream()
        .map(this::toManageSummary)
        .toList();
    }
    return competitions.values().stream()
      .sorted(Comparator.comparing(CompetitionDetail::id).reversed())
      .map(this::toManageSummary)
      .toList();
  }

  public Long saveDraft(SaveCompetitionDraftCommand command) {
    validateCommand(
      command.organizerId(),
      command.title(),
      command.description(),
      command.signupStartAt(),
      command.signupEndAt(),
      command.startAt(),
      command.endAt(),
      command.quota()
    );
    if (competitionMapper != null) {
      CompetitionEntity entity = buildEntity(command.organizerId(), command.title(), command.description(),
        command.signupStartAt(), command.signupEndAt(), command.startAt(), command.endAt(), command.quota(), "DRAFT");
      competitionMapper.insert(entity);
      return entity.getId();
    }
    long id = idGenerator.getAndIncrement();
    CompetitionDetail detail = buildDetail(id, command.organizerId(), command.title(), command.description(),
      command.signupStartAt(), command.signupEndAt(), command.startAt(), command.endAt(), command.quota(), "DRAFT", false, false);
    competitions.put(id, detail);
    return id;
  }

  public CompetitionDetail update(Long competitionId, UpdateCompetitionCommand command) {
    validateCommand(
      command.organizerId(),
      command.title(),
      command.description(),
      command.signupStartAt(),
      command.signupEndAt(),
      command.startAt(),
      command.endAt(),
      command.quota()
    );
    String status = normalizeStatus(command.status(), "PUBLISHED");
    if (competitionMapper != null) {
      CompetitionEntity entity = getEntity(competitionId);
      entity.setOrganizerId(command.organizerId());
      entity.setTitle(command.title());
      entity.setDescription(command.description());
      entity.setSignupStartAt(command.signupStartAt());
      entity.setSignupEndAt(command.signupEndAt());
      entity.setStartAt(command.startAt());
      entity.setEndAt(command.endAt());
      entity.setQuota(command.quota());
      entity.setStatus(status);
      entity.setUpdatedAt(LocalDateTime.now());
      competitionMapper.updateById(entity);
      return toDetail(entity);
    }
    CompetitionDetail existing = getCompetition(competitionId);
    CompetitionDetail updated = buildDetail(
      competitionId,
      command.organizerId(),
      command.title(),
      command.description(),
      command.signupStartAt(),
      command.signupEndAt(),
      command.startAt(),
      command.endAt(),
      command.quota(),
      status,
      existing.recommended(),
      existing.pinned()
    );
    competitions.put(competitionId, updated);
    return updated;
  }

  public CompetitionDetail updateFeature(Long competitionId, CompetitionFeatureCommand command) {
    if (competitionMapper != null) {
      CompetitionEntity entity = getEntity(competitionId);
      entity.setIsRecommended(command.recommended() != null ? command.recommended() : entity.getIsRecommended());
      entity.setIsPinned(command.pinned() != null ? command.pinned() : entity.getIsPinned());
      entity.setUpdatedAt(LocalDateTime.now());
      competitionMapper.updateById(entity);
      return toDetail(entity);
    }
    CompetitionDetail existing = getCompetition(competitionId);
    CompetitionDetail updated = buildDetail(
      existing.id(),
      existing.organizerId(),
      existing.title(),
      existing.description(),
      existing.signupStartAt(),
      existing.signupEndAt(),
      existing.startAt(),
      existing.endAt(),
      existing.quota(),
      existing.status(),
      command.recommended() != null ? command.recommended() : existing.recommended(),
      command.pinned() != null ? command.pinned() : existing.pinned()
    );
    competitions.put(competitionId, updated);
    return updated;
  }

  public boolean offline(Long competitionId) {
    if (competitionMapper != null) {
      CompetitionEntity entity = getEntity(competitionId);
      entity.setStatus("OFFLINE");
      entity.setUpdatedAt(LocalDateTime.now());
      competitionMapper.updateById(entity);
      return true;
    }
    CompetitionDetail existing = getCompetition(competitionId);
    competitions.put(competitionId, buildDetail(
      existing.id(),
      existing.organizerId(),
      existing.title(),
      existing.description(),
      existing.signupStartAt(),
      existing.signupEndAt(),
      existing.startAt(),
      existing.endAt(),
      existing.quota(),
      "OFFLINE",
      existing.recommended(),
      existing.pinned()
    ));
    return true;
  }

  public CompetitionDetail getCompetition(Long competitionId) {
    if (competitionMapper != null) {
      CompetitionEntity entity = competitionMapper.selectById(competitionId);
      if (entity == null) {
        throw new IllegalArgumentException("比赛不存在");
      }
      return toDetail(entity);
    }
    CompetitionDetail detail = competitions.get(competitionId);
    if (detail == null) {
      throw new IllegalArgumentException("比赛不存在");
    }
    return detail;
  }

  private void validateCommand(
    Long organizerId,
    String title,
    String description,
    LocalDateTime signupStartAt,
    LocalDateTime signupEndAt,
    LocalDateTime startAt,
    LocalDateTime endAt,
    Integer quota
  ) {
    if (organizerId == null) {
      throw new IllegalArgumentException("发起人不能为空");
    }
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("比赛名称不能为空");
    }
    if (description == null || description.isBlank()) {
      throw new IllegalArgumentException("比赛说明不能为空");
    }
    if (contentAuditService != null) {
      contentAuditService.auditCompetition(organizerId, title, description);
    }
    if (signupStartAt == null || signupEndAt == null || startAt == null || endAt == null) {
      throw new IllegalArgumentException("比赛时间不能为空");
    }
    if (signupEndAt.isBefore(signupStartAt)) {
      throw new IllegalArgumentException("报名截止时间不能早于报名开始时间");
    }
    if (endAt.isBefore(startAt)) {
      throw new IllegalArgumentException("比赛结束时间不能早于开始时间");
    }
    if (quota == null || quota <= 0) {
      throw new IllegalArgumentException("比赛名额必须大于 0");
    }
  }

  private CompetitionSummary toSummary(CompetitionDetail detail) {
    return new CompetitionSummary(
      detail.id(),
      detail.title(),
      detail.description(),
      detail.signupStartAt(),
      detail.signupEndAt(),
      detail.startAt(),
      detail.endAt(),
      detail.quota(),
      detail.status(),
      detail.recommended(),
      detail.pinned()
    );
  }

  private CompetitionSummary toSummary(CompetitionEntity entity) {
    return new CompetitionSummary(
      entity.getId(),
      entity.getTitle(),
      entity.getDescription(),
      entity.getSignupStartAt(),
      entity.getSignupEndAt(),
      entity.getStartAt(),
      entity.getEndAt(),
      entity.getQuota(),
      entity.getStatus(),
      Boolean.TRUE.equals(entity.getIsRecommended()),
      Boolean.TRUE.equals(entity.getIsPinned())
    );
  }

  private CompetitionDetail toDetail(CompetitionEntity entity) {
    return new CompetitionDetail(
      entity.getId(),
      entity.getOrganizerId(),
      entity.getTitle(),
      entity.getDescription(),
      entity.getSignupStartAt(),
      entity.getSignupEndAt(),
      entity.getStartAt(),
      entity.getEndAt(),
      entity.getQuota(),
      entity.getStatus(),
      Boolean.TRUE.equals(entity.getIsRecommended()),
      Boolean.TRUE.equals(entity.getIsPinned())
    );
  }

  private CompetitionDraftSummary toManageSummary(CompetitionEntity entity) {
    return new CompetitionDraftSummary(
      entity.getId(),
      entity.getOrganizerId(),
      entity.getTitle(),
      entity.getDescription(),
      entity.getSignupStartAt(),
      entity.getSignupEndAt(),
      entity.getStartAt(),
      entity.getEndAt(),
      entity.getQuota(),
      entity.getStatus(),
      Boolean.TRUE.equals(entity.getIsRecommended()),
      Boolean.TRUE.equals(entity.getIsPinned())
    );
  }

  private CompetitionDraftSummary toManageSummary(CompetitionDetail detail) {
    return new CompetitionDraftSummary(
      detail.id(),
      detail.organizerId(),
      detail.title(),
      detail.description(),
      detail.signupStartAt(),
      detail.signupEndAt(),
      detail.startAt(),
      detail.endAt(),
      detail.quota(),
      detail.status(),
      detail.recommended(),
      detail.pinned()
    );
  }

  private CompetitionEntity buildEntity(
    Long organizerId,
    String title,
    String description,
    LocalDateTime signupStartAt,
    LocalDateTime signupEndAt,
    LocalDateTime startAt,
    LocalDateTime endAt,
    Integer quota,
    String status
  ) {
    CompetitionEntity entity = new CompetitionEntity();
    entity.setOrganizerId(organizerId);
    entity.setTitle(title.trim());
    entity.setDescription(description.trim());
    entity.setSignupStartAt(signupStartAt);
    entity.setSignupEndAt(signupEndAt);
    entity.setStartAt(startAt);
    entity.setEndAt(endAt);
    entity.setQuota(quota);
    entity.setStatus(status);
    entity.setIsRecommended(false);
    entity.setIsPinned(false);
    entity.setUpdatedAt(LocalDateTime.now());
    return entity;
  }

  private CompetitionDetail buildDetail(
    Long id,
    Long organizerId,
    String title,
    String description,
    LocalDateTime signupStartAt,
    LocalDateTime signupEndAt,
    LocalDateTime startAt,
    LocalDateTime endAt,
    Integer quota,
    String status,
    boolean recommended,
    boolean pinned
  ) {
    return new CompetitionDetail(
      id,
      organizerId,
      title.trim(),
      description.trim(),
      signupStartAt,
      signupEndAt,
      startAt,
      endAt,
      quota,
      status,
      recommended,
      pinned
    );
  }

  private CompetitionEntity getEntity(Long competitionId) {
    CompetitionEntity entity = competitionMapper.selectById(competitionId);
    if (entity == null) {
      throw new IllegalArgumentException("比赛不存在");
    }
    return entity;
  }

  private String normalizeStatus(String incoming, String fallback) {
    String status = incoming == null || incoming.isBlank() ? fallback : incoming.trim().toUpperCase();
    if (!status.equals("DRAFT") && !status.equals("PUBLISHED") && !status.equals("OFFLINE")) {
      throw new IllegalArgumentException("比赛状态不合法");
    }
    return status;
  }
}
