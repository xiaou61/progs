package com.campus.competition.modules.points.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.points.mapper.PointsAccountMapper;
import com.campus.competition.modules.points.mapper.PointsRecordMapper;
import com.campus.competition.modules.points.model.PointsAccountSummary;
import com.campus.competition.modules.points.model.DailyTaskSummary;
import com.campus.competition.modules.points.model.PointsRecordSummary;
import com.campus.competition.modules.points.persistence.PointsAccountEntity;
import com.campus.competition.modules.points.persistence.PointsRecordEntity;
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
public class PointsService {

  public static final int DAILY_CHECKIN_POINTS = 5;
  public static final int COMPETITION_SHARE_POINTS = 3;
  public static final String BIZ_TYPE_COMPETITION_RESULT = "COMPETITION_RESULT";
  public static final String BIZ_TYPE_DAILY_CHECKIN = "DAILY_CHECKIN";
  public static final String BIZ_TYPE_COMPETITION_SHARE = "COMPETITION_SHARE";

  private final AtomicLong idGenerator = new AtomicLong(1);
  private final Map<Long, PointsAccountSummary> accounts = new ConcurrentHashMap<>();
  private final Map<Long, PointsRecordSummary> records = new ConcurrentHashMap<>();
  @Autowired(required = false)
  private PointsAccountMapper pointsAccountMapper;
  @Autowired(required = false)
  private PointsRecordMapper pointsRecordMapper;

  public void grantPoints(Long userId, int amount, String bizType, Long bizId, String remark) {
    if (userId == null) {
      throw new IllegalArgumentException("积分用户不能为空");
    }
    if (amount <= 0) {
      throw new IllegalArgumentException("积分变更值必须大于 0");
    }
    if (pointsAccountMapper != null && pointsRecordMapper != null) {
      grantPointsWithDatabase(userId, amount, bizType, bizId, remark);
      return;
    }

    PointsAccountSummary existing = accounts.getOrDefault(userId, new PointsAccountSummary(userId, 0, 0));
    accounts.put(userId, new PointsAccountSummary(
      userId,
      existing.availablePoints() + amount,
      existing.totalPoints() + amount
    ));

    long recordId = idGenerator.getAndIncrement();
    records.put(recordId, new PointsRecordSummary(
      recordId,
      userId,
      amount,
      bizType,
      bizId,
      remark,
      LocalDateTime.now()
    ));
  }

  @Transactional
  public int completeDailyCheckin(Long userId) {
    validateUserId(userId);
    if (hasGrantedToday(userId, BIZ_TYPE_DAILY_CHECKIN)) {
      throw new IllegalArgumentException("今日已完成签到");
    }
    grantPoints(userId, DAILY_CHECKIN_POINTS, BIZ_TYPE_DAILY_CHECKIN, userId, "每日签到积分");
    return getAccount(userId).availablePoints();
  }

  @Transactional
  public int completeCompetitionShare(Long userId, Long competitionId) {
    validateUserId(userId);
    if (competitionId == null) {
      throw new IllegalArgumentException("比赛不能为空");
    }
    if (hasGrantedToday(userId, BIZ_TYPE_COMPETITION_SHARE)) {
      throw new IllegalArgumentException("今日已完成比赛分享");
    }
    grantPoints(userId, COMPETITION_SHARE_POINTS, BIZ_TYPE_COMPETITION_SHARE, competitionId, "比赛分享积分");
    return getAccount(userId).availablePoints();
  }

  public int queryAvailablePoints(Long userId) {
    if (pointsAccountMapper != null) {
      return getAccount(userId).availablePoints();
    }
    return accounts.getOrDefault(userId, new PointsAccountSummary(userId, 0, 0)).availablePoints();
  }

  public PointsAccountSummary getAccount(Long userId) {
    if (pointsAccountMapper != null) {
      PointsAccountEntity entity = pointsAccountMapper.selectById(userId);
      if (entity == null) {
        return new PointsAccountSummary(userId, 0, 0);
      }
      return new PointsAccountSummary(entity.getUserId(), entity.getAvailablePoints(), entity.getTotalPoints());
    }
    return accounts.getOrDefault(userId, new PointsAccountSummary(userId, 0, 0));
  }

  public List<PointsRecordSummary> listRecords(Long userId) {
    if (pointsRecordMapper != null) {
      return pointsRecordMapper.selectList(Wrappers.<PointsRecordEntity>lambdaQuery()
          .eq(PointsRecordEntity::getUserId, userId)
          .orderByDesc(PointsRecordEntity::getId))
        .stream()
        .map(this::toSummary)
        .toList();
    }
    return records.values().stream()
      .filter(item -> item.userId().equals(userId))
      .sorted(Comparator.comparing(PointsRecordSummary::id).reversed())
      .toList();
  }

  public DailyTaskSummary getDailyTaskSummary(Long userId) {
    validateUserId(userId);
    LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    List<PointsRecordSummary> todayRecords = listRecordsInWindow(userId, startOfDay, endOfDay);

    PointsRecordSummary checkinRecord = todayRecords.stream()
      .filter(item -> BIZ_TYPE_DAILY_CHECKIN.equals(item.bizType()))
      .findFirst()
      .orElse(null);
    PointsRecordSummary shareRecord = todayRecords.stream()
      .filter(item -> BIZ_TYPE_COMPETITION_SHARE.equals(item.bizType()))
      .findFirst()
      .orElse(null);
    int todayTaskPoints = todayRecords.stream()
      .filter(item -> BIZ_TYPE_DAILY_CHECKIN.equals(item.bizType()) || BIZ_TYPE_COMPETITION_SHARE.equals(item.bizType()))
      .mapToInt(PointsRecordSummary::changeAmount)
      .sum();

    return new DailyTaskSummary(
      userId,
      checkinRecord != null,
      shareRecord != null,
      todayTaskPoints,
      checkinRecord == null ? null : checkinRecord.createdAt(),
      shareRecord == null ? null : shareRecord.createdAt()
    );
  }

  private void grantPointsWithDatabase(Long userId, int amount, String bizType, Long bizId, String remark) {
    PointsAccountEntity account = pointsAccountMapper.selectById(userId);
    if (account == null) {
      account = new PointsAccountEntity();
      account.setUserId(userId);
      account.setAvailablePoints(amount);
      account.setTotalPoints(amount);
      pointsAccountMapper.insert(account);
    } else {
      account.setAvailablePoints(account.getAvailablePoints() + amount);
      account.setTotalPoints(account.getTotalPoints() + amount);
      pointsAccountMapper.updateById(account);
    }

    PointsRecordEntity record = new PointsRecordEntity();
    record.setUserId(userId);
    record.setChangeAmount(amount);
    record.setBizType(bizType);
    record.setBizId(bizId);
    record.setRemark(remark);
    record.setCreatedAt(LocalDateTime.now());
    pointsRecordMapper.insert(record);
  }

  private PointsRecordSummary toSummary(PointsRecordEntity entity) {
    return new PointsRecordSummary(
      entity.getId(),
      entity.getUserId(),
      entity.getChangeAmount(),
      entity.getBizType(),
      entity.getBizId(),
      entity.getRemark(),
      entity.getCreatedAt()
    );
  }

  private void validateUserId(Long userId) {
    if (userId == null) {
      throw new IllegalArgumentException("用户不能为空");
    }
  }

  private boolean hasGrantedToday(Long userId, String bizType) {
    LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    return listRecordsInWindow(userId, startOfDay, endOfDay).stream()
      .anyMatch(item -> bizType.equals(item.bizType()));
  }

  private List<PointsRecordSummary> listRecordsInWindow(Long userId, LocalDateTime startAt, LocalDateTime endAt) {
    if (pointsRecordMapper != null) {
      return pointsRecordMapper.selectList(Wrappers.<PointsRecordEntity>lambdaQuery()
          .eq(PointsRecordEntity::getUserId, userId)
          .ge(PointsRecordEntity::getCreatedAt, startAt)
          .lt(PointsRecordEntity::getCreatedAt, endAt)
          .orderByDesc(PointsRecordEntity::getCreatedAt))
        .stream()
        .map(this::toSummary)
        .toList();
    }

    return records.values().stream()
      .filter(item -> item.userId().equals(userId))
      .filter(item -> !item.createdAt().isBefore(startAt) && item.createdAt().isBefore(endAt))
      .sorted(Comparator.comparing(PointsRecordSummary::createdAt).reversed())
      .toList();
  }
}
