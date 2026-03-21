package com.campus.competition.modules.competition.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.persistence.UserEntity;
import com.campus.competition.modules.auth.security.ForbiddenException;
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

  private static final String PARTICIPANT_TYPE_STUDENT_ONLY = "STUDENT_ONLY";
  private static final String PARTICIPANT_TYPE_TEACHER_ONLY = "TEACHER_ONLY";

  private final AtomicLong idGenerator = new AtomicLong(1);
  private final Map<Long, CompetitionDetail> competitions = new ConcurrentHashMap<>();
  private final CompetitionMapper competitionMapper;
  @Autowired(required = false)
  private ContentAuditService contentAuditService;
  @Autowired(required = false)
  private UserMapper userMapper;

  public CompetitionService() {
    this.competitionMapper = null;
  }

  @Autowired
  public CompetitionService(CompetitionMapper competitionMapper) {
    this.competitionMapper = competitionMapper;
  }

  public Long publish(PublishCompetitionCommand command) {
    CompetitionParticipantSettings settings = resolveParticipantSettingsForCreate(
      command.organizerId(),
      command.participantType(),
      command.advisorTeacherId()
    );
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
        command.signupStartAt(), command.signupEndAt(), command.startAt(), command.endAt(), command.quota(), "PUBLISHED",
        settings.participantType(), settings.advisorTeacherId(), command.recommended(), command.pinned());
      competitionMapper.insert(entity);
      return entity.getId();
    }
    long id = idGenerator.getAndIncrement();
    CompetitionDetail detail = buildDetail(id, command.organizerId(), command.title(), command.description(),
      command.signupStartAt(), command.signupEndAt(), command.startAt(), command.endAt(), command.quota(), "PUBLISHED",
      normalizeFeatureFlag(command.recommended()), normalizeFeatureFlag(command.pinned()),
      settings.participantType(), settings.advisorTeacherId(), settings.advisorTeacherName());
    competitions.put(id, detail);
    return id;
  }

  public List<CompetitionSummary> listPublicCompetitions() {
    if (competitionMapper != null) {
      return competitionMapper.selectList(Wrappers.<CompetitionEntity>lambdaQuery()
          .eq(CompetitionEntity::getStatus, "PUBLISHED")
          .orderByDesc(CompetitionEntity::getIsPinned)
          .orderByDesc(CompetitionEntity::getIsRecommended)
          .orderByDesc(CompetitionEntity::getId))
        .stream()
        .map(this::toSummary)
        .toList();
    }
    return competitions.values().stream()
      .filter(item -> "PUBLISHED".equals(item.status()))
      .sorted(managedCompetitionComparator())
      .map(this::toSummary)
      .toList();
  }

  public List<CompetitionDraftSummary> listManagedCompetitions() {
    if (competitionMapper != null) {
      return competitionMapper.selectList(Wrappers.<CompetitionEntity>lambdaQuery()
          .orderByDesc(CompetitionEntity::getIsPinned)
          .orderByDesc(CompetitionEntity::getIsRecommended)
          .orderByDesc(CompetitionEntity::getId))
        .stream()
        .map(this::toManageSummary)
        .toList();
    }
    return competitions.values().stream()
      .sorted(managedCompetitionComparator())
      .map(this::toManageSummary)
      .toList();
  }

  public List<CompetitionDraftSummary> listManagedCompetitionsByOrganizer(Long organizerId) {
    if (organizerId == null) {
      throw new IllegalArgumentException("发起人不能为空");
    }
    if (competitionMapper != null) {
      return competitionMapper.selectList(Wrappers.<CompetitionEntity>lambdaQuery()
          .eq(CompetitionEntity::getOrganizerId, organizerId)
          .orderByDesc(CompetitionEntity::getIsPinned)
          .orderByDesc(CompetitionEntity::getIsRecommended)
          .orderByDesc(CompetitionEntity::getId))
        .stream()
        .map(this::toManageSummary)
        .toList();
    }
    return competitions.values().stream()
      .filter(item -> organizerId.equals(item.organizerId()))
      .sorted(managedCompetitionComparator())
      .map(this::toManageSummary)
      .toList();
  }

  public Long saveDraft(SaveCompetitionDraftCommand command) {
    CompetitionParticipantSettings settings = resolveParticipantSettingsForCreate(
      command.organizerId(),
      command.participantType(),
      command.advisorTeacherId()
    );
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
        command.signupStartAt(), command.signupEndAt(), command.startAt(), command.endAt(), command.quota(), "DRAFT",
        settings.participantType(), settings.advisorTeacherId(), command.recommended(), command.pinned());
      competitionMapper.insert(entity);
      return entity.getId();
    }
    long id = idGenerator.getAndIncrement();
    CompetitionDetail detail = buildDetail(id, command.organizerId(), command.title(), command.description(),
      command.signupStartAt(), command.signupEndAt(), command.startAt(), command.endAt(), command.quota(), "DRAFT",
      normalizeFeatureFlag(command.recommended()), normalizeFeatureFlag(command.pinned()),
      settings.participantType(), settings.advisorTeacherId(), settings.advisorTeacherName());
    competitions.put(id, detail);
    return id;
  }

  public Long publishForOrganizer(Long organizerId, PublishCompetitionCommand command) {
    if (command == null) {
      throw new IllegalArgumentException("比赛信息不能为空");
    }
    return publish(new PublishCompetitionCommand(
      organizerId,
      command.title(),
      command.description(),
      command.signupStartAt(),
      command.signupEndAt(),
      command.startAt(),
      command.endAt(),
      command.quota(),
      command.participantType(),
      command.advisorTeacherId(),
      command.recommended(),
      command.pinned()
    ));
  }

  public Long saveDraftForOrganizer(Long organizerId, SaveCompetitionDraftCommand command) {
    if (command == null) {
      throw new IllegalArgumentException("比赛信息不能为空");
    }
    return saveDraft(new SaveCompetitionDraftCommand(
      organizerId,
      command.title(),
      command.description(),
      command.signupStartAt(),
      command.signupEndAt(),
      command.startAt(),
      command.endAt(),
      command.quota(),
      command.participantType(),
      command.advisorTeacherId(),
      command.recommended(),
      command.pinned()
    ));
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
      CompetitionParticipantSettings settings = resolveParticipantSettingsForUpdate(
        command.organizerId(),
        command.participantType(),
        command.advisorTeacherId(),
        entity.getParticipantType(),
        entity.getAdvisorTeacherId()
      );
      entity.setOrganizerId(command.organizerId());
      entity.setTitle(command.title());
      entity.setDescription(command.description());
      entity.setSignupStartAt(command.signupStartAt());
      entity.setSignupEndAt(command.signupEndAt());
      entity.setStartAt(command.startAt());
      entity.setEndAt(command.endAt());
      entity.setQuota(command.quota());
      entity.setParticipantType(settings.participantType());
      entity.setAdvisorTeacherId(settings.advisorTeacherId());
      entity.setStatus(status);
      entity.setIsRecommended(command.recommended() != null ? command.recommended() : entity.getIsRecommended());
      entity.setIsPinned(command.pinned() != null ? command.pinned() : entity.getIsPinned());
      entity.setUpdatedAt(LocalDateTime.now());
      competitionMapper.updateById(entity);
      return toDetail(entity);
    }
    CompetitionDetail existing = getCompetition(competitionId);
    CompetitionParticipantSettings settings = resolveParticipantSettingsForUpdate(
      command.organizerId(),
      command.participantType(),
      command.advisorTeacherId(),
      existing.participantType(),
      existing.advisorTeacherId()
    );
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
      command.recommended() != null ? command.recommended() : existing.recommended(),
      command.pinned() != null ? command.pinned() : existing.pinned(),
      settings.participantType(),
      settings.advisorTeacherId(),
      settings.advisorTeacherName()
    );
    competitions.put(competitionId, updated);
    return updated;
  }

  public CompetitionDetail updateForOrganizer(Long organizerId, Long competitionId, UpdateCompetitionCommand command) {
    if (command == null) {
      throw new IllegalArgumentException("比赛信息不能为空");
    }
    assertOrganizerOwnsCompetition(organizerId, competitionId);
    return update(competitionId, new UpdateCompetitionCommand(
      organizerId,
      command.title(),
      command.description(),
      command.signupStartAt(),
      command.signupEndAt(),
      command.startAt(),
      command.endAt(),
      command.quota(),
      command.status(),
      command.participantType(),
      command.advisorTeacherId(),
      command.recommended(),
      command.pinned()
    ));
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
      command.pinned() != null ? command.pinned() : existing.pinned(),
      existing.participantType(),
      existing.advisorTeacherId(),
      existing.advisorTeacherName()
    );
    competitions.put(competitionId, updated);
    return updated;
  }

  public CompetitionDetail updateFeatureForOrganizer(Long organizerId, Long competitionId, CompetitionFeatureCommand command) {
    assertOrganizerOwnsCompetition(organizerId, competitionId);
    return updateFeature(competitionId, command);
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
      existing.pinned(),
      existing.participantType(),
      existing.advisorTeacherId(),
      existing.advisorTeacherName()
    ));
    return true;
  }

  public boolean offlineForOrganizer(Long organizerId, Long competitionId) {
    assertOrganizerOwnsCompetition(organizerId, competitionId);
    return offline(competitionId);
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
      detail.pinned(),
      detail.participantType(),
      detail.advisorTeacherId(),
      detail.advisorTeacherName()
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
      Boolean.TRUE.equals(entity.getIsPinned()),
      normalizeParticipantTypeOrDefault(entity.getParticipantType()),
      entity.getAdvisorTeacherId(),
      resolveAdvisorTeacherName(entity.getAdvisorTeacherId())
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
      Boolean.TRUE.equals(entity.getIsPinned()),
      normalizeParticipantTypeOrDefault(entity.getParticipantType()),
      entity.getAdvisorTeacherId(),
      resolveAdvisorTeacherName(entity.getAdvisorTeacherId())
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
      Boolean.TRUE.equals(entity.getIsPinned()),
      normalizeParticipantTypeOrDefault(entity.getParticipantType()),
      entity.getAdvisorTeacherId(),
      resolveAdvisorTeacherName(entity.getAdvisorTeacherId())
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
      detail.pinned(),
      detail.participantType(),
      detail.advisorTeacherId(),
      detail.advisorTeacherName()
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
    String status,
    String participantType,
    Long advisorTeacherId,
    Boolean recommended,
    Boolean pinned
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
    entity.setParticipantType(participantType);
    entity.setAdvisorTeacherId(advisorTeacherId);
    entity.setStatus(status);
    entity.setIsRecommended(normalizeFeatureFlag(recommended));
    entity.setIsPinned(normalizeFeatureFlag(pinned));
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
    boolean pinned,
    String participantType,
    Long advisorTeacherId,
    String advisorTeacherName
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
      pinned,
      participantType,
      advisorTeacherId,
      advisorTeacherName
    );
  }

  private boolean normalizeFeatureFlag(Boolean value) {
    return Boolean.TRUE.equals(value);
  }

  private Comparator<CompetitionDetail> managedCompetitionComparator() {
    return Comparator
      .comparing(CompetitionDetail::pinned, Comparator.reverseOrder())
      .thenComparing(CompetitionDetail::recommended, Comparator.reverseOrder())
      .thenComparing(CompetitionDetail::id, Comparator.reverseOrder());
  }

  private CompetitionParticipantSettings resolveParticipantSettingsForCreate(
    Long organizerId,
    String participantType,
    Long advisorTeacherId
  ) {
    String normalizedType = isBlank(participantType)
      ? PARTICIPANT_TYPE_STUDENT_ONLY
      : normalizeParticipantType(participantType);
    Long resolvedAdvisorTeacherId = advisorTeacherId;
    if (PARTICIPANT_TYPE_STUDENT_ONLY.equals(normalizedType) && resolvedAdvisorTeacherId == null && !hasExplicitParticipantType(participantType)) {
      resolvedAdvisorTeacherId = organizerId;
    }
    return validateParticipantSettings(normalizedType, resolvedAdvisorTeacherId);
  }

  private CompetitionParticipantSettings resolveParticipantSettingsForUpdate(
    Long organizerId,
    String participantType,
    Long advisorTeacherId,
    String currentParticipantType,
    Long currentAdvisorTeacherId
  ) {
    String normalizedType = isBlank(participantType)
      ? normalizeParticipantTypeOrDefault(currentParticipantType)
      : normalizeParticipantType(participantType);
    Long resolvedAdvisorTeacherId = advisorTeacherId;
    if (PARTICIPANT_TYPE_STUDENT_ONLY.equals(normalizedType) && resolvedAdvisorTeacherId == null
      && PARTICIPANT_TYPE_STUDENT_ONLY.equals(normalizeParticipantTypeOrDefault(currentParticipantType))) {
      resolvedAdvisorTeacherId = currentAdvisorTeacherId;
    }
    if (PARTICIPANT_TYPE_STUDENT_ONLY.equals(normalizedType) && resolvedAdvisorTeacherId == null) {
      resolvedAdvisorTeacherId = organizerId;
    }
    return validateParticipantSettings(normalizedType, resolvedAdvisorTeacherId);
  }

  private CompetitionParticipantSettings validateParticipantSettings(String participantType, Long advisorTeacherId) {
    if (PARTICIPANT_TYPE_STUDENT_ONLY.equals(participantType)) {
      if (advisorTeacherId == null) {
        throw new IllegalArgumentException("学生赛必须指定指导老师");
      }
      validateAdvisorTeacher(advisorTeacherId);
      return new CompetitionParticipantSettings(
        participantType,
        advisorTeacherId,
        resolveAdvisorTeacherName(advisorTeacherId)
      );
    }
    if (advisorTeacherId != null) {
      throw new IllegalArgumentException("老师赛不能指定指导老师");
    }
    return new CompetitionParticipantSettings(participantType, null, null);
  }

  private void validateAdvisorTeacher(Long advisorTeacherId) {
    if (userMapper == null) {
      return;
    }
    UserEntity advisorTeacher = userMapper.selectById(advisorTeacherId);
    if (advisorTeacher == null) {
      throw new IllegalArgumentException("指导老师不存在");
    }
    if (!"TEACHER".equals(advisorTeacher.getRoleCode())) {
      throw new IllegalArgumentException("指导老师必须是老师账号");
    }
    if (!"ENABLED".equals(advisorTeacher.getStatus())) {
      throw new IllegalArgumentException("指导老师已停用");
    }
  }

  private String resolveAdvisorTeacherName(Long advisorTeacherId) {
    if (advisorTeacherId == null || userMapper == null) {
      return null;
    }
    UserEntity advisorTeacher = userMapper.selectById(advisorTeacherId);
    return advisorTeacher == null ? null : advisorTeacher.getRealName();
  }

  private CompetitionEntity getEntity(Long competitionId) {
    CompetitionEntity entity = competitionMapper.selectById(competitionId);
    if (entity == null) {
      throw new IllegalArgumentException("比赛不存在");
    }
    return entity;
  }

  private void assertOrganizerOwnsCompetition(Long organizerId, Long competitionId) {
    if (organizerId == null) {
      throw new IllegalArgumentException("发起人不能为空");
    }
    CompetitionDetail detail = getCompetition(competitionId);
    if (!organizerId.equals(detail.organizerId())) {
      throw new ForbiddenException("无权管理其他老师发起的比赛");
    }
  }

  private String normalizeStatus(String incoming, String fallback) {
    String status = incoming == null || incoming.isBlank() ? fallback : incoming.trim().toUpperCase();
    if (!status.equals("DRAFT") && !status.equals("PUBLISHED") && !status.equals("OFFLINE")) {
      throw new IllegalArgumentException("比赛状态不合法");
    }
    return status;
  }

  private String normalizeParticipantType(String participantType) {
    String normalized = participantType == null ? null : participantType.trim().toUpperCase();
    if (!PARTICIPANT_TYPE_STUDENT_ONLY.equals(normalized) && !PARTICIPANT_TYPE_TEACHER_ONLY.equals(normalized)) {
      throw new IllegalArgumentException("参赛类型不合法");
    }
    return normalized;
  }

  private String normalizeParticipantTypeOrDefault(String participantType) {
    if (isBlank(participantType)) {
      return PARTICIPANT_TYPE_STUDENT_ONLY;
    }
    return normalizeParticipantType(participantType);
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private boolean hasExplicitParticipantType(String participantType) {
    return !isBlank(participantType);
  }

  private record CompetitionParticipantSettings(
    String participantType,
    Long advisorTeacherId,
    String advisorTeacherName
  ) {
  }
}
