package com.campus.competition.modules.dashboard.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.persistence.UserEntity;
import com.campus.competition.modules.competition.mapper.CompetitionMapper;
import com.campus.competition.modules.competition.persistence.CompetitionEntity;
import com.campus.competition.modules.dashboard.model.AdminDashboardOverview;
import com.campus.competition.modules.dashboard.model.AdminDashboardSummary;
import com.campus.competition.modules.dashboard.model.DashboardCompetitionItem;
import com.campus.competition.modules.dashboard.model.DashboardDistributionItem;
import com.campus.competition.modules.dashboard.model.TeacherDashboardOverview;
import com.campus.competition.modules.dashboard.model.TeacherDashboardSummary;
import com.campus.competition.modules.registration.mapper.RegistrationMapper;
import com.campus.competition.modules.registration.persistence.RegistrationEntity;
import com.campus.competition.modules.score.mapper.ScoreMapper;
import com.campus.competition.modules.score.persistence.ScoreEntity;
import com.campus.competition.modules.submission.mapper.SubmissionMapper;
import com.campus.competition.modules.submission.persistence.SubmissionEntity;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

  private final CompetitionMapper competitionMapper;
  private final RegistrationMapper registrationMapper;
  private final SubmissionMapper submissionMapper;
  private final ScoreMapper scoreMapper;
  private final UserMapper userMapper;

  public DashboardService(
    CompetitionMapper competitionMapper,
    RegistrationMapper registrationMapper,
    SubmissionMapper submissionMapper,
    ScoreMapper scoreMapper,
    UserMapper userMapper
  ) {
    this.competitionMapper = competitionMapper;
    this.registrationMapper = registrationMapper;
    this.submissionMapper = submissionMapper;
    this.scoreMapper = scoreMapper;
    this.userMapper = userMapper;
  }

  public AdminDashboardSummary getAdminDashboardSummary() {
    List<CompetitionEntity> competitions = loadCompetitions();
    Map<Long, Integer> registrationCountMap = buildCountMap(loadActiveRegistrations(), RegistrationEntity::getCompetitionId);
    Map<Long, Integer> submissionCountMap = buildCountMap(loadSubmissions(), SubmissionEntity::getCompetitionId);
    Map<Long, Integer> awardCountMap = buildCountMap(loadScores(), ScoreEntity::getCompetitionId);
    Map<Long, Integer> awardPointsMap = buildAwardPointsMap(loadScores());
    List<DashboardCompetitionItem> competitionItems = buildCompetitionItems(
      competitions,
      registrationCountMap,
      submissionCountMap,
      awardCountMap,
      awardPointsMap
    );

    return new AdminDashboardSummary(
      buildAdminOverview(competitions, competitionItems),
      buildStatusDistribution(competitions),
      competitionItems.stream().limit(6).toList()
    );
  }

  public TeacherDashboardSummary getTeacherDashboardSummary(Long teacherId) {
    UserEntity teacher = getRequiredTeacher(teacherId);
    List<CompetitionEntity> teacherCompetitions = loadCompetitions().stream()
      .filter(item -> Objects.equals(item.getOrganizerId(), teacherId))
      .toList();
    Map<Long, Integer> registrationCountMap = buildCountMap(loadActiveRegistrations(), RegistrationEntity::getCompetitionId);
    Map<Long, Integer> submissionCountMap = buildCountMap(loadSubmissions(), SubmissionEntity::getCompetitionId);
    Map<Long, Integer> awardCountMap = buildCountMap(loadScores(), ScoreEntity::getCompetitionId);
    Map<Long, Integer> awardPointsMap = buildAwardPointsMap(loadScores());
    List<DashboardCompetitionItem> competitionItems = buildCompetitionItems(
      teacherCompetitions,
      registrationCountMap,
      submissionCountMap,
      awardCountMap,
      awardPointsMap
    );

    return new TeacherDashboardSummary(
      teacher.getId(),
      teacher.getRealName(),
      buildTeacherOverview(competitionItems),
      buildStatusDistribution(teacherCompetitions),
      competitionItems
    );
  }

  public String exportAdminDashboardCsv() {
    return buildCompetitionCsv(
      "title,status,registrations,submissions,awards,points",
      getAdminDashboardSummary().topCompetitions()
    );
  }

  public String exportTeacherDashboardCsv(Long teacherId) {
    TeacherDashboardSummary summary = getTeacherDashboardSummary(teacherId);
    String header = "teacherName,title,status,registrations,submissions,awards,points";
    StringBuilder builder = new StringBuilder(header).append('\n');
    for (DashboardCompetitionItem item : summary.competitions()) {
      builder.append(escapeCsv(summary.teacherName())).append(',')
        .append(escapeCsv(item.title())).append(',')
        .append(escapeCsv(item.status())).append(',')
        .append(item.registrationCount()).append(',')
        .append(item.submissionCount()).append(',')
        .append(item.awardCount()).append(',')
        .append(item.awardPoints()).append('\n');
    }
    return builder.toString();
  }

  private List<CompetitionEntity> loadCompetitions() {
    return competitionMapper.selectList(Wrappers.<CompetitionEntity>lambdaQuery()
      .orderByDesc(CompetitionEntity::getIsPinned)
      .orderByDesc(CompetitionEntity::getIsRecommended)
      .orderByDesc(CompetitionEntity::getId));
  }

  private List<RegistrationEntity> loadActiveRegistrations() {
    return registrationMapper.selectList(Wrappers.<RegistrationEntity>lambdaQuery()
      .eq(RegistrationEntity::getStatus, "REGISTERED"));
  }

  private List<SubmissionEntity> loadSubmissions() {
    return submissionMapper.selectList(Wrappers.<SubmissionEntity>lambdaQuery());
  }

  private List<ScoreEntity> loadScores() {
    return scoreMapper.selectList(Wrappers.<ScoreEntity>lambdaQuery());
  }

  private AdminDashboardOverview buildAdminOverview(
    List<CompetitionEntity> competitions,
    List<DashboardCompetitionItem> competitionItems
  ) {
    return new AdminDashboardOverview(
      competitions.size(),
      countCompetitionStatus(competitions, "PUBLISHED"),
      countCompetitionStatus(competitions, "DRAFT"),
      countCompetitionStatus(competitions, "OFFLINE"),
      competitionItems.stream().mapToInt(DashboardCompetitionItem::registrationCount).sum(),
      competitionItems.stream().mapToInt(DashboardCompetitionItem::submissionCount).sum(),
      competitionItems.stream().mapToInt(DashboardCompetitionItem::awardCount).sum(),
      competitionItems.stream().mapToInt(DashboardCompetitionItem::awardPoints).sum(),
      countUsersByRole("TEACHER"),
      countUsersByRole("STUDENT")
    );
  }

  private TeacherDashboardOverview buildTeacherOverview(List<DashboardCompetitionItem> competitionItems) {
    return new TeacherDashboardOverview(
      competitionItems.size(),
      countDashboardStatus(competitionItems, "PUBLISHED"),
      countDashboardStatus(competitionItems, "DRAFT"),
      countDashboardStatus(competitionItems, "OFFLINE"),
      competitionItems.stream().mapToInt(DashboardCompetitionItem::registrationCount).sum(),
      competitionItems.stream().mapToInt(DashboardCompetitionItem::submissionCount).sum(),
      competitionItems.stream().mapToInt(DashboardCompetitionItem::awardCount).sum(),
      competitionItems.stream().mapToInt(DashboardCompetitionItem::awardPoints).sum()
    );
  }

  private List<DashboardDistributionItem> buildStatusDistribution(List<CompetitionEntity> competitions) {
    Map<String, String> labels = Map.of(
      "PUBLISHED", "已发布",
      "DRAFT", "草稿",
      "OFFLINE", "已下架"
    );
    return List.of("PUBLISHED", "DRAFT", "OFFLINE").stream()
      .map(status -> new DashboardDistributionItem(
        status,
        labels.getOrDefault(status, status),
        countCompetitionStatus(competitions, status)
      ))
      .toList();
  }

  private List<DashboardCompetitionItem> buildCompetitionItems(
    List<CompetitionEntity> competitions,
    Map<Long, Integer> registrationCountMap,
    Map<Long, Integer> submissionCountMap,
    Map<Long, Integer> awardCountMap,
    Map<Long, Integer> awardPointsMap
  ) {
    return competitions.stream()
      .map(item -> new DashboardCompetitionItem(
        item.getId(),
        item.getOrganizerId(),
        item.getTitle(),
        item.getStatus(),
        registrationCountMap.getOrDefault(item.getId(), 0),
        submissionCountMap.getOrDefault(item.getId(), 0),
        awardCountMap.getOrDefault(item.getId(), 0),
        awardPointsMap.getOrDefault(item.getId(), 0),
        Boolean.TRUE.equals(item.getIsRecommended()),
        Boolean.TRUE.equals(item.getIsPinned()),
        item.getStartAt(),
        item.getEndAt()
      ))
      .sorted(Comparator
        .comparingInt(DashboardCompetitionItem::registrationCount).reversed()
        .thenComparingInt(DashboardCompetitionItem::submissionCount).reversed()
        .thenComparingInt(DashboardCompetitionItem::awardCount).reversed()
        .thenComparing(DashboardCompetitionItem::competitionId))
      .toList();
  }

  private <T> Map<Long, Integer> buildCountMap(List<T> items, Function<T, Long> keyGetter) {
    Map<Long, Integer> result = new LinkedHashMap<>();
    for (T item : items) {
      Long key = keyGetter.apply(item);
      if (key == null) {
        continue;
      }
      result.put(key, result.getOrDefault(key, 0) + 1);
    }
    return result;
  }

  private Map<Long, Integer> buildAwardPointsMap(List<ScoreEntity> scores) {
    Map<Long, Integer> result = new LinkedHashMap<>();
    for (ScoreEntity item : scores) {
      Long key = item.getCompetitionId();
      if (key == null) {
        continue;
      }
      result.put(key, result.getOrDefault(key, 0) + (item.getPoints() == null ? 0 : item.getPoints()));
    }
    return result;
  }

  private int countUsersByRole(String roleCode) {
    Long count = userMapper.selectCount(Wrappers.<UserEntity>lambdaQuery()
      .eq(UserEntity::getRoleCode, roleCode)
      .eq(UserEntity::getStatus, "ENABLED"));
    return count == null ? 0 : count.intValue();
  }

  private int countCompetitionStatus(List<CompetitionEntity> competitions, String status) {
    return (int) competitions.stream()
      .filter(item -> status.equals(item.getStatus()))
      .count();
  }

  private int countDashboardStatus(List<DashboardCompetitionItem> competitions, String status) {
    return (int) competitions.stream()
      .filter(item -> status.equals(item.status()))
      .count();
  }

  private UserEntity getRequiredTeacher(Long teacherId) {
    if (teacherId == null) {
      throw new IllegalArgumentException("老师不能为空");
    }
    UserEntity teacher = userMapper.selectById(teacherId);
    if (teacher == null || !"ENABLED".equals(teacher.getStatus())) {
      throw new IllegalArgumentException("老师不存在");
    }
    return teacher;
  }

  private String buildCompetitionCsv(String header, List<DashboardCompetitionItem> competitions) {
    StringBuilder builder = new StringBuilder(header).append('\n');
    for (DashboardCompetitionItem item : competitions) {
      builder.append(escapeCsv(item.title())).append(',')
        .append(escapeCsv(item.status())).append(',')
        .append(item.registrationCount()).append(',')
        .append(item.submissionCount()).append(',')
        .append(item.awardCount()).append(',')
        .append(item.awardPoints()).append('\n');
    }
    return builder.toString();
  }

  private String escapeCsv(String value) {
    if (value == null) {
      return "";
    }
    String escaped = value.replace("\"", "\"\"");
    if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
      return "\"" + escaped + "\"";
    }
    return escaped;
  }
}
