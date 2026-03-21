package com.campus.competition.modules.result.controller;

import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.persistence.UserEntity;
import com.campus.competition.modules.auth.security.AuthContext;
import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.points.model.PointsAccountSummary;
import com.campus.competition.modules.points.model.PointsRecordSummary;
import com.campus.competition.modules.points.service.PointsService;
import com.campus.competition.modules.result.model.CompetitionResultItem;
import com.campus.competition.modules.score.model.ScoreSummary;
import com.campus.competition.modules.score.service.ScoreService;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/results")
public class AppResultController {

  private final ScoreService scoreService;
  private final PointsService pointsService;
  private final UserMapper userMapper;

  public AppResultController(ScoreService scoreService, PointsService pointsService, UserMapper userMapper) {
    this.scoreService = scoreService;
    this.pointsService = pointsService;
    this.userMapper = userMapper;
  }

  @GetMapping("/competition/{competitionId}")
  public ApiResponse<List<CompetitionResultItem>> listByCompetition(@PathVariable Long competitionId) {
    List<ScoreSummary> scores = scoreService.listByCompetition(competitionId);
    Map<Long, UserEntity> userMap = userMapper.selectBatchIds(scores.stream()
        .map(ScoreSummary::studentId)
        .distinct()
        .toList())
      .stream()
      .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
    return ApiResponse.success(scores.stream().map(item -> {
      UserEntity user = userMap.get(item.studentId());
      return new CompetitionResultItem(
        item.id(),
        item.competitionId(),
        item.studentId(),
        user == null ? "" : user.getStudentNo(),
        user == null ? "学生 " + item.studentId() : user.getRealName(),
        item.score(),
        item.rank(),
        item.awardName(),
        item.points(),
        item.publishedAt(),
        item.reviewerName(),
        item.reviewComment()
      );
    }).toList());
  }

  @GetMapping("/student/{studentId}")
  public ApiResponse<Map<String, Object>> studentOverview(@PathVariable Long studentId) {
    AuthContext.requireUser(studentId);
    PointsAccountSummary account = pointsService.getAccount(studentId);
    List<ScoreSummary> results = scoreService.listByStudent(studentId);
    List<PointsRecordSummary> records = pointsService.listRecords(studentId);
    return ApiResponse.success(Map.of(
      "account", account,
      "results", results,
      "records", records
    ));
  }

  @GetMapping("/points")
  public ApiResponse<PointsAccountSummary> points(@RequestParam Long userId) {
    AuthContext.requireUser(userId);
    return ApiResponse.success(pointsService.getAccount(userId));
  }
}
