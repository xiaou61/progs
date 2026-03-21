package com.campus.competition.modules.checkin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.checkin.mapper.CheckinMapper;
import com.campus.competition.modules.checkin.model.CheckInCommand;
import com.campus.competition.modules.checkin.model.CheckinReviewCommand;
import com.campus.competition.modules.checkin.model.CheckinSummary;
import com.campus.competition.modules.checkin.persistence.CheckinEntity;
import com.campus.competition.modules.competition.model.CompetitionDetail;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.registration.model.RegistrationAttendanceCommand;
import com.campus.competition.modules.registration.model.RegistrationSummary;
import com.campus.competition.modules.registration.service.RegistrationService;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheckinService {

  private static final String CHECKIN_PENDING = "PENDING";
  private static final String CHECKIN_APPROVED = "APPROVED";
  private static final String CHECKIN_REJECTED = "REJECTED";
  private static final String ATTENDANCE_PENDING = "PENDING";
  private static final String ATTENDANCE_PRESENT = "PRESENT";
  private static final String ATTENDANCE_ABSENT = "ABSENT";
  private static final String REGISTRATION_REGISTERED = "REGISTERED";

  private final AtomicLong idGenerator = new AtomicLong(1);
  private final Map<Long, CheckinSummary> checkins = new ConcurrentHashMap<>();
  private final CompetitionService competitionService;
  private final RegistrationService registrationService;
  @Autowired(required = false)
  private CheckinMapper checkinMapper;

  public CheckinService(CompetitionService competitionService, RegistrationService registrationService) {
    this.competitionService = competitionService;
    this.registrationService = registrationService;
  }

  public boolean checkIn(CheckInCommand command) {
    CompetitionDetail competition = competitionService.getCompetition(command.competitionId());
    RegistrationSummary registration = validateCommand(command, competition);
    LocalDateTime now = LocalDateTime.now();
    if (checkinMapper != null) {
      CheckinEntity existing = checkinMapper.selectOne(Wrappers.<CheckinEntity>lambdaQuery()
        .eq(CheckinEntity::getCompetitionId, command.competitionId())
        .eq(CheckinEntity::getUserId, command.userId()));
      if (existing != null) {
        ensureCheckinCanBeSubmitted(existing, registration);
        existing.setMethod(command.method());
        existing.setCheckedAt(now);
        existing.setStatus(CHECKIN_PENDING);
        existing.setReviewRemark(null);
        existing.setReviewedAt(null);
        checkinMapper.updateById(existing);
        return true;
      }

      CheckinEntity entity = new CheckinEntity();
      entity.setCompetitionId(command.competitionId());
      entity.setUserId(command.userId());
      entity.setMethod(command.method());
      entity.setCheckedAt(now);
      entity.setStatus(CHECKIN_PENDING);
      entity.setReviewRemark(null);
      entity.setReviewedAt(null);
      checkinMapper.insert(entity);
      return true;
    }

    CheckinSummary existing = findByCompetitionAndUser(command.competitionId(), command.userId());
    if (existing != null) {
      ensureCheckinCanBeSubmitted(existing, registration);
      checkins.put(existing.id(), new CheckinSummary(
        existing.id(),
        command.competitionId(),
        command.userId(),
        command.method(),
        now,
        CHECKIN_PENDING,
        null,
        null
      ));
      return true;
    }

    long id = idGenerator.getAndIncrement();
    checkins.put(id, new CheckinSummary(
      id,
      command.competitionId(),
      command.userId(),
      command.method(),
      now,
      CHECKIN_PENDING,
      null,
      null
    ));
    return true;
  }

  public List<CheckinSummary> listByCompetition(Long competitionId) {
    if (checkinMapper != null) {
      return checkinMapper.selectList(Wrappers.<CheckinEntity>lambdaQuery()
          .eq(CheckinEntity::getCompetitionId, competitionId)
          .orderByAsc(CheckinEntity::getId))
        .stream()
        .map(this::toSummary)
        .toList();
    }
    return checkins.values().stream()
      .filter(item -> item.competitionId().equals(competitionId))
      .sorted(Comparator.comparing(CheckinSummary::id))
      .toList();
  }

  public boolean reviewCheckinByRegistration(Long registrationId, CheckinReviewCommand command) {
    RegistrationSummary registration = registrationService.getRequiredRegistration(registrationId);
    if (!REGISTRATION_REGISTERED.equals(registration.status())) {
      throw new IllegalArgumentException("仅已报名用户可处理签到申请");
    }

    String reviewStatus = normalizeReviewStatus(command);
    CheckinSummary checkinSummary = getRequiredPendingCheckin(registration.competitionId(), registration.userId());
    LocalDateTime reviewedAt = LocalDateTime.now();
    if (CHECKIN_APPROVED.equals(reviewStatus)) {
      registrationService.markAttendance(registrationId, new RegistrationAttendanceCommand(ATTENDANCE_PRESENT));
      updateCheckinReview(checkinSummary.id(), reviewStatus, null, reviewedAt);
      return true;
    }

    String reviewRemark = normalizeRejectReason(command.reason());
    updateCheckinReview(checkinSummary.id(), reviewStatus, reviewRemark, reviewedAt);
    return true;
  }

  public void syncWithAttendance(Long registrationId, String attendanceStatus) {
    RegistrationSummary registration = registrationService.getRequiredRegistration(registrationId);
    CheckinSummary existing = findByCompetitionAndUser(registration.competitionId(), registration.userId());
    if (existing == null) {
      return;
    }

    if (ATTENDANCE_PRESENT.equals(attendanceStatus)) {
      updateCheckinReview(existing.id(), CHECKIN_APPROVED, null, LocalDateTime.now());
      return;
    }
    if (ATTENDANCE_ABSENT.equals(attendanceStatus)) {
      String reviewRemark = existing.reviewRemark() == null || existing.reviewRemark().isBlank()
        ? "老师已标记缺席"
        : existing.reviewRemark();
      updateCheckinReview(existing.id(), CHECKIN_REJECTED, reviewRemark, LocalDateTime.now());
    }
  }

  public CheckinSummary findByCompetitionAndUser(Long competitionId, Long userId) {
    if (competitionId == null || userId == null) {
      return null;
    }
    if (checkinMapper != null) {
      CheckinEntity entity = checkinMapper.selectOne(Wrappers.<CheckinEntity>lambdaQuery()
        .eq(CheckinEntity::getCompetitionId, competitionId)
        .eq(CheckinEntity::getUserId, userId));
      return entity == null ? null : toSummary(entity);
    }
    return checkins.values().stream()
      .filter(item -> item.competitionId().equals(competitionId) && item.userId().equals(userId))
      .findFirst()
      .orElse(null);
  }

  private RegistrationSummary validateCommand(CheckInCommand command, CompetitionDetail competition) {
    if (command.competitionId() == null) {
      throw new IllegalArgumentException("比赛不能为空");
    }
    if (command.userId() == null) {
      throw new IllegalArgumentException("用户不能为空");
    }
    if (command.method() == null || command.method().isBlank()) {
      throw new IllegalArgumentException("签到方式不能为空");
    }

    RegistrationSummary registration = registrationService.findByCompetitionAndUser(command.competitionId(), command.userId());
    if (registration == null || !REGISTRATION_REGISTERED.equals(registration.status())) {
      throw new IllegalArgumentException("请先完成报名再签到");
    }
    if (ATTENDANCE_PRESENT.equals(registration.attendanceStatus())) {
      throw new IllegalArgumentException("老师已确认到场，无需重复签到");
    }
    if (ATTENDANCE_ABSENT.equals(registration.attendanceStatus())) {
      throw new IllegalArgumentException("当前已被标记缺席，请联系老师处理");
    }

    LocalDateTime now = LocalDateTime.now();
    if (now.isBefore(competition.startAt()) || now.isAfter(competition.endAt())) {
      throw new IllegalArgumentException("当前不在签到时间内");
    }
    return registration;
  }

  private void ensureCheckinCanBeSubmitted(CheckinSummary existing, RegistrationSummary registration) {
    if (CHECKIN_PENDING.equals(existing.status())) {
      throw new IllegalArgumentException("已提交签到申请，请等待老师确认");
    }
    if (CHECKIN_APPROVED.equals(existing.status()) || ATTENDANCE_PRESENT.equals(registration.attendanceStatus())) {
      throw new IllegalArgumentException("老师已确认到场，无需重复签到");
    }
    if (ATTENDANCE_ABSENT.equals(registration.attendanceStatus())) {
      throw new IllegalArgumentException("当前已被标记缺席，请联系老师处理");
    }
  }

  private void ensureCheckinCanBeSubmitted(CheckinEntity existing, RegistrationSummary registration) {
    if (CHECKIN_PENDING.equals(existing.getStatus())) {
      throw new IllegalArgumentException("已提交签到申请，请等待老师确认");
    }
    if (CHECKIN_APPROVED.equals(existing.getStatus()) || ATTENDANCE_PRESENT.equals(registration.attendanceStatus())) {
      throw new IllegalArgumentException("老师已确认到场，无需重复签到");
    }
    if (ATTENDANCE_ABSENT.equals(registration.attendanceStatus())) {
      throw new IllegalArgumentException("当前已被标记缺席，请联系老师处理");
    }
  }

  private CheckinSummary getRequiredPendingCheckin(Long competitionId, Long userId) {
    CheckinSummary summary = findByCompetitionAndUser(competitionId, userId);
    if (summary == null) {
      throw new IllegalArgumentException("当前还没有签到申请");
    }
    if (!CHECKIN_PENDING.equals(summary.status())) {
      throw new IllegalArgumentException("该签到申请已处理");
    }
    return summary;
  }

  private String normalizeReviewStatus(CheckinReviewCommand command) {
    if (command == null || command.status() == null || command.status().isBlank()) {
      throw new IllegalArgumentException("审核状态不能为空");
    }
    String reviewStatus = command.status().trim().toUpperCase();
    if (!CHECKIN_APPROVED.equals(reviewStatus) && !CHECKIN_REJECTED.equals(reviewStatus)) {
      throw new IllegalArgumentException("审核状态不支持");
    }
    return reviewStatus;
  }

  private String normalizeRejectReason(String reason) {
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("驳回原因不能为空");
    }
    return reason.trim();
  }

  private void updateCheckinReview(Long checkinId, String status, String reviewRemark, LocalDateTime reviewedAt) {
    if (checkinMapper != null) {
      CheckinEntity entity = checkinMapper.selectById(checkinId);
      if (entity == null) {
        throw new IllegalArgumentException("签到申请不存在");
      }
      entity.setStatus(status);
      entity.setReviewRemark(reviewRemark);
      entity.setReviewedAt(reviewedAt);
      checkinMapper.updateById(entity);
      return;
    }
    CheckinSummary existing = checkins.get(checkinId);
    if (existing == null) {
      throw new IllegalArgumentException("签到申请不存在");
    }
    checkins.put(checkinId, new CheckinSummary(
      existing.id(),
      existing.competitionId(),
      existing.userId(),
      existing.method(),
      existing.checkedAt(),
      status,
      reviewRemark,
      reviewedAt
    ));
  }

  private CheckinSummary toSummary(CheckinEntity entity) {
    return new CheckinSummary(
      entity.getId(),
      entity.getCompetitionId(),
      entity.getUserId(),
      entity.getMethod(),
      entity.getCheckedAt(),
      entity.getStatus() == null || entity.getStatus().isBlank() ? CHECKIN_PENDING : entity.getStatus(),
      entity.getReviewRemark(),
      entity.getReviewedAt()
    );
  }
}
