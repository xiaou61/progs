package com.campus.competition.modules.registration.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.persistence.UserEntity;
import com.campus.competition.modules.competition.model.CompetitionDetail;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.registration.model.CancelRegistrationCommand;
import com.campus.competition.modules.registration.model.ManualRegistrationCommand;
import com.campus.competition.modules.registration.mapper.RegistrationMapper;
import com.campus.competition.modules.registration.model.RegisterCompetitionCommand;
import com.campus.competition.modules.registration.model.RegistrationAttendanceCommand;
import com.campus.competition.modules.registration.model.RegistrationSummary;
import com.campus.competition.modules.registration.model.RejectRegistrationCommand;
import com.campus.competition.modules.registration.persistence.RegistrationEntity;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

  private static final String PARTICIPANT_TYPE_STUDENT_ONLY = "STUDENT_ONLY";
  private static final String PARTICIPANT_TYPE_TEACHER_ONLY = "TEACHER_ONLY";
  private static final String STATUS_REGISTERED = "REGISTERED";
  private static final String STATUS_CANCELLED = "CANCELLED";
  private static final String STATUS_REJECTED = "REJECTED";
  private static final String ATTENDANCE_PENDING = "PENDING";
  private static final String ATTENDANCE_PRESENT = "PRESENT";
  private static final String ATTENDANCE_ABSENT = "ABSENT";

  private final AtomicLong idGenerator = new AtomicLong(1);
  private final Map<Long, RegistrationSummary> registrations = new ConcurrentHashMap<>();
  private final CompetitionService competitionService;
  private final RegistrationMapper registrationMapper;
  @Autowired(required = false)
  private UserMapper userMapper;

  public RegistrationService(CompetitionService competitionService) {
    this.competitionService = competitionService;
    this.registrationMapper = null;
  }

  @Autowired
  public RegistrationService(CompetitionService competitionService, RegistrationMapper registrationMapper) {
    this.competitionService = competitionService;
    this.registrationMapper = registrationMapper;
  }

  public Long register(RegisterCompetitionCommand command) {
    CompetitionDetail competition = competitionService.getCompetition(command.competitionId());
    validateRegisterCommand(command, competition);
    if (registrationMapper != null) {
      return registerWithDatabase(command, competition);
    }

    long currentCount = registrations.values().stream()
      .filter(item -> item.competitionId().equals(command.competitionId()))
      .filter(this::isActiveRegistration)
      .count();
    if (currentCount >= competition.quota()) {
      throw new IllegalArgumentException("比赛名额已满");
    }

    RegistrationSummary existing = findInMemoryByCompetitionAndUser(command.competitionId(), command.userId());
    if (existing != null) {
      if (isActiveRegistration(existing)) {
        throw new IllegalArgumentException("请勿重复报名");
      }
      registrations.put(existing.id(), new RegistrationSummary(
        existing.id(),
        existing.competitionId(),
        existing.userId(),
        STATUS_REGISTERED,
        ATTENDANCE_PENDING,
        null
      ));
      return existing.id();
    }

    long id = idGenerator.getAndIncrement();
    registrations.put(id, new RegistrationSummary(
      id,
      command.competitionId(),
      command.userId(),
      STATUS_REGISTERED,
      ATTENDANCE_PENDING,
      null
    ));
    return id;
  }

  public List<RegistrationSummary> listByCompetition(Long competitionId) {
    if (registrationMapper != null) {
      return registrationMapper.selectList(Wrappers.<RegistrationEntity>lambdaQuery()
          .eq(RegistrationEntity::getCompetitionId, competitionId)
          .orderByAsc(RegistrationEntity::getId))
        .stream()
        .map(this::toSummary)
        .toList();
    }
    return registrations.values().stream()
      .filter(item -> item.competitionId().equals(competitionId))
      .sorted(Comparator.comparing(RegistrationSummary::id))
      .toList();
  }

  public RegistrationSummary findByCompetitionAndUser(Long competitionId, Long userId) {
    if (competitionId == null || userId == null) {
      throw new IllegalArgumentException("比赛和用户不能为空");
    }
    if (registrationMapper != null) {
      RegistrationEntity entity = registrationMapper.selectOne(Wrappers.<RegistrationEntity>lambdaQuery()
        .eq(RegistrationEntity::getCompetitionId, competitionId)
        .eq(RegistrationEntity::getUserId, userId));
      return entity == null ? null : toSummary(entity);
    }
    return findInMemoryByCompetitionAndUser(competitionId, userId);
  }

  public RegistrationSummary getRequiredRegistration(Long registrationId) {
    if (registrationId == null) {
      throw new IllegalArgumentException("报名记录不能为空");
    }
    if (registrationMapper != null) {
      return toSummary(getRequiredEntity(registrationId));
    }
    return getRequiredInMemoryRegistration(registrationId);
  }

  public List<RegistrationSummary> listByUser(Long userId) {
    if (userId == null) {
      throw new IllegalArgumentException("用户不能为空");
    }
    if (registrationMapper != null) {
      return registrationMapper.selectList(Wrappers.<RegistrationEntity>lambdaQuery()
          .eq(RegistrationEntity::getUserId, userId)
          .orderByDesc(RegistrationEntity::getId))
        .stream()
        .map(this::toSummary)
        .toList();
    }
    return registrations.values().stream()
      .filter(item -> item.userId().equals(userId))
      .sorted(Comparator.comparing(RegistrationSummary::id).reversed())
      .toList();
  }

  public boolean cancel(Long registrationId, CancelRegistrationCommand command) {
    if (registrationId == null) {
      throw new IllegalArgumentException("报名记录不能为空");
    }
    if (command == null || command.userId() == null) {
      throw new IllegalArgumentException("用户不能为空");
    }
    if (registrationMapper != null) {
      RegistrationEntity entity = getRequiredEntity(registrationId);
      validateCancelable(entity, command.userId());
      entity.setStatus(STATUS_CANCELLED);
      entity.setAttendanceStatus(ATTENDANCE_PENDING);
      entity.setUpdatedAt(LocalDateTime.now());
      registrationMapper.updateById(entity);
      return true;
    }

    RegistrationSummary existing = getRequiredInMemoryRegistration(registrationId);
    validateCancelable(existing, command.userId());
    registrations.put(registrationId, new RegistrationSummary(
      existing.id(),
      existing.competitionId(),
      existing.userId(),
      STATUS_CANCELLED,
      ATTENDANCE_PENDING,
      existing.remark()
    ));
    return true;
  }

  public Long manualAdd(ManualRegistrationCommand command) {
    if (command == null || command.competitionId() == null) {
      throw new IllegalArgumentException("比赛不能为空");
    }
    if (command.userId() == null) {
      throw new IllegalArgumentException("用户不能为空");
    }
    CompetitionDetail competition = competitionService.getCompetition(command.competitionId());
    validateParticipantRole(command.userId(), competition.participantType());
    if (registrationMapper != null) {
      return manualAddWithDatabase(command, competition);
    }

    long currentCount = registrations.values().stream()
      .filter(item -> item.competitionId().equals(command.competitionId()))
      .filter(this::isActiveRegistration)
      .count();
    if (currentCount >= competition.quota()) {
      throw new IllegalArgumentException("比赛名额已满");
    }

    RegistrationSummary existing = findInMemoryByCompetitionAndUser(command.competitionId(), command.userId());
    if (existing != null) {
      if (isActiveRegistration(existing)) {
        throw new IllegalArgumentException("该用户已报名");
      }
      registrations.put(existing.id(), new RegistrationSummary(
        existing.id(),
        existing.competitionId(),
        existing.userId(),
        STATUS_REGISTERED,
        ATTENDANCE_PENDING,
        normalizeRemark(command.remark())
      ));
      return existing.id();
    }

    long id = idGenerator.getAndIncrement();
    registrations.put(id, new RegistrationSummary(
      id,
      command.competitionId(),
      command.userId(),
      STATUS_REGISTERED,
      ATTENDANCE_PENDING,
      normalizeRemark(command.remark())
    ));
    return id;
  }

  public boolean reject(Long registrationId, RejectRegistrationCommand command) {
    if (registrationId == null) {
      throw new IllegalArgumentException("报名记录不能为空");
    }
    if (command == null || command.reason() == null || command.reason().isBlank()) {
      throw new IllegalArgumentException("驳回原因不能为空");
    }
    if (registrationMapper != null) {
      RegistrationEntity entity = getRequiredEntity(registrationId);
      entity.setStatus(STATUS_REJECTED);
      entity.setAttendanceStatus(ATTENDANCE_PENDING);
      entity.setRemark(command.reason().trim());
      entity.setUpdatedAt(LocalDateTime.now());
      registrationMapper.updateById(entity);
      return true;
    }

    RegistrationSummary existing = getRequiredInMemoryRegistration(registrationId);
    registrations.put(registrationId, new RegistrationSummary(
      existing.id(),
      existing.competitionId(),
      existing.userId(),
      STATUS_REJECTED,
      ATTENDANCE_PENDING,
      command.reason().trim()
    ));
    return true;
  }

  public boolean markAttendance(Long registrationId, RegistrationAttendanceCommand command) {
    if (registrationId == null) {
      throw new IllegalArgumentException("报名记录不能为空");
    }
    if (command == null || command.attendanceStatus() == null || command.attendanceStatus().isBlank()) {
      throw new IllegalArgumentException("到场状态不能为空");
    }
    String attendanceStatus = command.attendanceStatus().trim();
    if (!ATTENDANCE_PRESENT.equals(attendanceStatus) && !ATTENDANCE_ABSENT.equals(attendanceStatus)) {
      throw new IllegalArgumentException("到场状态不支持");
    }
    if (registrationMapper != null) {
      RegistrationEntity entity = getRequiredEntity(registrationId);
      if (!STATUS_REGISTERED.equals(entity.getStatus())) {
        throw new IllegalArgumentException("仅已报名用户可标记到场");
      }
      entity.setAttendanceStatus(attendanceStatus);
      entity.setUpdatedAt(LocalDateTime.now());
      registrationMapper.updateById(entity);
      return true;
    }

    RegistrationSummary existing = getRequiredInMemoryRegistration(registrationId);
    if (!STATUS_REGISTERED.equals(existing.status())) {
      throw new IllegalArgumentException("仅已报名用户可标记到场");
    }
    registrations.put(registrationId, new RegistrationSummary(
      existing.id(),
      existing.competitionId(),
      existing.userId(),
      existing.status(),
      attendanceStatus,
      existing.remark()
    ));
    return true;
  }

  public boolean hasActiveRegistration(Long competitionId, Long userId) {
    RegistrationSummary registration = findByCompetitionAndUser(competitionId, userId);
    return registration != null && isActiveRegistration(registration);
  }

  private void validateRegisterCommand(RegisterCompetitionCommand command, CompetitionDetail competition) {
    if (command.competitionId() == null) {
      throw new IllegalArgumentException("比赛不能为空");
    }
    if (command.userId() == null) {
      throw new IllegalArgumentException("用户不能为空");
    }
    LocalDateTime now = LocalDateTime.now();
    if (now.isBefore(competition.signupStartAt()) || now.isAfter(competition.signupEndAt())) {
      throw new IllegalArgumentException("当前不在报名时间内");
    }
    validateParticipantRole(command.userId(), competition.participantType());
  }

  private void validateParticipantRole(Long userId, String participantType) {
    if (userMapper == null || userId == null) {
      return;
    }
    UserEntity user = userMapper.selectById(userId);
    if (user == null) {
      throw new IllegalArgumentException("用户不存在");
    }
    String normalizedParticipantType = participantType == null || participantType.isBlank()
      ? PARTICIPANT_TYPE_STUDENT_ONLY
      : participantType.trim().toUpperCase();
    if (PARTICIPANT_TYPE_STUDENT_ONLY.equals(normalizedParticipantType) && !"STUDENT".equals(user.getRoleCode())) {
      throw new IllegalArgumentException("当前比赛仅限学生报名");
    }
    if (PARTICIPANT_TYPE_TEACHER_ONLY.equals(normalizedParticipantType) && !"TEACHER".equals(user.getRoleCode())) {
      throw new IllegalArgumentException("当前比赛仅限老师报名");
    }
  }

  private Long registerWithDatabase(RegisterCompetitionCommand command, CompetitionDetail competition) {
    Long currentCount = registrationMapper.selectCount(Wrappers.<RegistrationEntity>lambdaQuery()
      .eq(RegistrationEntity::getCompetitionId, command.competitionId())
      .eq(RegistrationEntity::getStatus, STATUS_REGISTERED));
    if (currentCount != null && currentCount >= competition.quota()) {
      throw new IllegalArgumentException("比赛名额已满");
    }

    RegistrationEntity existing = registrationMapper.selectOne(Wrappers.<RegistrationEntity>lambdaQuery()
      .eq(RegistrationEntity::getCompetitionId, command.competitionId())
      .eq(RegistrationEntity::getUserId, command.userId()));
    if (existing != null) {
      if (STATUS_REGISTERED.equals(existing.getStatus())) {
        throw new IllegalArgumentException("请勿重复报名");
      }
      existing.setStatus(STATUS_REGISTERED);
      existing.setAttendanceStatus(ATTENDANCE_PENDING);
      existing.setRemark(null);
      existing.setUpdatedAt(LocalDateTime.now());
      registrationMapper.updateById(existing);
      return existing.getId();
    }

    RegistrationEntity entity = new RegistrationEntity();
    entity.setCompetitionId(command.competitionId());
    entity.setUserId(command.userId());
    entity.setStatus(STATUS_REGISTERED);
    entity.setAttendanceStatus(ATTENDANCE_PENDING);
    entity.setUpdatedAt(LocalDateTime.now());
    registrationMapper.insert(entity);
    return entity.getId();
  }

  private Long manualAddWithDatabase(ManualRegistrationCommand command, CompetitionDetail competition) {
    Long currentCount = registrationMapper.selectCount(Wrappers.<RegistrationEntity>lambdaQuery()
      .eq(RegistrationEntity::getCompetitionId, command.competitionId())
      .eq(RegistrationEntity::getStatus, STATUS_REGISTERED));
    if (currentCount != null && currentCount >= competition.quota()) {
      throw new IllegalArgumentException("比赛名额已满");
    }

    RegistrationEntity existing = registrationMapper.selectOne(Wrappers.<RegistrationEntity>lambdaQuery()
      .eq(RegistrationEntity::getCompetitionId, command.competitionId())
      .eq(RegistrationEntity::getUserId, command.userId()));
    if (existing != null) {
      if (STATUS_REGISTERED.equals(existing.getStatus())) {
        throw new IllegalArgumentException("该用户已报名");
      }
      existing.setStatus(STATUS_REGISTERED);
      existing.setAttendanceStatus(ATTENDANCE_PENDING);
      existing.setRemark(normalizeRemark(command.remark()));
      existing.setUpdatedAt(LocalDateTime.now());
      registrationMapper.updateById(existing);
      return existing.getId();
    }

    RegistrationEntity entity = new RegistrationEntity();
    entity.setCompetitionId(command.competitionId());
    entity.setUserId(command.userId());
    entity.setStatus(STATUS_REGISTERED);
    entity.setAttendanceStatus(ATTENDANCE_PENDING);
    entity.setRemark(normalizeRemark(command.remark()));
    entity.setUpdatedAt(LocalDateTime.now());
    registrationMapper.insert(entity);
    return entity.getId();
  }

  private RegistrationSummary toSummary(RegistrationEntity entity) {
    return new RegistrationSummary(
      entity.getId(),
      entity.getCompetitionId(),
      entity.getUserId(),
      entity.getStatus(),
      entity.getAttendanceStatus(),
      entity.getRemark()
    );
  }

  private RegistrationSummary findInMemoryByCompetitionAndUser(Long competitionId, Long userId) {
    return registrations.values().stream()
      .filter(item -> item.competitionId().equals(competitionId) && item.userId().equals(userId))
      .findFirst()
      .orElse(null);
  }

  private RegistrationSummary getRequiredInMemoryRegistration(Long registrationId) {
    RegistrationSummary summary = registrations.get(registrationId);
    if (summary == null) {
      throw new IllegalArgumentException("报名记录不存在");
    }
    return summary;
  }

  private RegistrationEntity getRequiredEntity(Long registrationId) {
    RegistrationEntity entity = registrationMapper.selectById(registrationId);
    if (entity == null) {
      throw new IllegalArgumentException("报名记录不存在");
    }
    return entity;
  }

  private void validateCancelable(RegistrationEntity entity, Long userId) {
    if (!entity.getUserId().equals(userId)) {
      throw new IllegalArgumentException("无权取消该报名记录");
    }
    if (!STATUS_REGISTERED.equals(entity.getStatus())) {
      throw new IllegalArgumentException("当前报名状态不可取消");
    }
    validateCancelDeadline(entity.getCompetitionId());
  }

  private void validateCancelable(RegistrationSummary summary, Long userId) {
    if (!summary.userId().equals(userId)) {
      throw new IllegalArgumentException("无权取消该报名记录");
    }
    if (!STATUS_REGISTERED.equals(summary.status())) {
      throw new IllegalArgumentException("当前报名状态不可取消");
    }
    validateCancelDeadline(summary.competitionId());
  }

  private void validateCancelDeadline(Long competitionId) {
    CompetitionDetail competition = competitionService.getCompetition(competitionId);
    if (competition != null && LocalDateTime.now().isAfter(competition.signupEndAt())) {
      throw new IllegalArgumentException("报名截止后不能取消报名");
    }
  }

  private boolean isActiveRegistration(RegistrationSummary summary) {
    return STATUS_REGISTERED.equals(summary.status());
  }

  private String normalizeRemark(String remark) {
    if (remark == null || remark.isBlank()) {
      return null;
    }
    return remark.trim();
  }
}
