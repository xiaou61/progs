package com.campus.competition.modules.checkin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.checkin.mapper.CheckinMapper;
import com.campus.competition.modules.checkin.model.CheckInCommand;
import com.campus.competition.modules.checkin.model.CheckinSummary;
import com.campus.competition.modules.checkin.persistence.CheckinEntity;
import com.campus.competition.modules.competition.model.CompetitionDetail;
import com.campus.competition.modules.competition.service.CompetitionService;
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
    validateCommand(command, competition);
    if (checkinMapper != null) {
      Long duplicateCount = checkinMapper.selectCount(Wrappers.<CheckinEntity>lambdaQuery()
        .eq(CheckinEntity::getCompetitionId, command.competitionId())
        .eq(CheckinEntity::getUserId, command.userId()));
      if (duplicateCount != null && duplicateCount > 0) {
        throw new IllegalArgumentException("请勿重复签到");
      }

      CheckinEntity entity = new CheckinEntity();
      entity.setCompetitionId(command.competitionId());
      entity.setUserId(command.userId());
      entity.setMethod(command.method());
      entity.setCheckedAt(LocalDateTime.now());
      checkinMapper.insert(entity);
      return true;
    }

    boolean alreadyCheckedIn = checkins.values().stream()
      .anyMatch(item -> item.competitionId().equals(command.competitionId()) && item.userId().equals(command.userId()));
    if (alreadyCheckedIn) {
      throw new IllegalArgumentException("请勿重复签到");
    }

    long id = idGenerator.getAndIncrement();
    checkins.put(id, new CheckinSummary(
      id,
      command.competitionId(),
      command.userId(),
      command.method(),
      LocalDateTime.now()
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

  private void validateCommand(CheckInCommand command, CompetitionDetail competition) {
    if (command.competitionId() == null) {
      throw new IllegalArgumentException("比赛不能为空");
    }
    if (command.userId() == null) {
      throw new IllegalArgumentException("用户不能为空");
    }
    if (command.method() == null || command.method().isBlank()) {
      throw new IllegalArgumentException("签到方式不能为空");
    }

    if (!registrationService.hasActiveRegistration(command.competitionId(), command.userId())) {
      throw new IllegalArgumentException("请先完成报名再签到");
    }

    LocalDateTime now = LocalDateTime.now();
    if (now.isBefore(competition.startAt()) || now.isAfter(competition.endAt())) {
      throw new IllegalArgumentException("当前不在签到时间内");
    }
  }

  private CheckinSummary toSummary(CheckinEntity entity) {
    return new CheckinSummary(
      entity.getId(),
      entity.getCompetitionId(),
      entity.getUserId(),
      entity.getMethod(),
      entity.getCheckedAt()
    );
  }
}
