package com.campus.competition.modules.points;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.persistence.UserEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo-data"})
@Transactional
class DailyTaskOverviewApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserMapper userMapper;

  @Test
  void shouldGrantDailyTaskPointsAndReturnPersonalOverview() throws Exception {
    long teacherId = resolveUserId("T20260001");
    long studentId = resolveUserId("S20260001");
    LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

    long competitionId = extractLongField(
      mockMvc.perform(post("/api/admin/competitions")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "organizerId": %d,
              "title": "每日任务与个人汇总验证赛",
              "description": "用于验证每日签到、比赛分享和个人比赛汇总",
              "signupStartAt": "%s",
              "signupEndAt": "%s",
              "startAt": "%s",
              "endAt": "%s",
              "quota": 50
            }
            """.formatted(
            teacherId,
            now.minusDays(1),
            now.plusDays(2),
            now.minusHours(2),
            now.plusDays(3))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andReturn(),
      "competitionId"
    );

    mockMvc.perform(post("/api/app/registrations")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "competitionId": %d,
            "userId": %d
          }
          """.formatted(competitionId, studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    mockMvc.perform(post("/api/app/submissions")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "competitionId": %d,
            "userId": %d,
            "fileUrl": "https://example.com/daily-task-work.pptx",
            "reuploadAllowed": false
          }
          """.formatted(competitionId, studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    mockMvc.perform(post("/api/admin/scores/publish")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "competitionId": %d,
            "studentId": %d,
            "score": 92,
            "rank": 1,
            "awardName": "一等奖",
            "points": 30
          }
          """.formatted(competitionId, studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    mockMvc.perform(get("/api/app/points/tasks/" + studentId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.task.dailyCheckinDone").value(false))
      .andExpect(jsonPath("$.data.task.competitionShareDone").value(false))
      .andExpect(jsonPath("$.data.task.todayTaskPoints").value(0))
      .andExpect(jsonPath("$.data.overview.registeredCompetitionCount").value(1))
      .andExpect(jsonPath("$.data.overview.submittedWorkCount").value(1))
      .andExpect(jsonPath("$.data.overview.awardCount").value(1))
      .andExpect(jsonPath("$.data.overview.totalAwardPoints").value(30))
      .andExpect(jsonPath("$.data.overview.totalPoints").value(30));

    mockMvc.perform(post("/api/app/points/tasks/checkin")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "userId": %d
          }
          """.formatted(studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.granted").value(true))
      .andExpect(jsonPath("$.data.changeAmount").value(5))
      .andExpect(jsonPath("$.data.availablePoints").value(35));

    mockMvc.perform(post("/api/app/points/tasks/checkin")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "userId": %d
          }
          """.formatted(studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.message").value("今日已完成签到"));

    mockMvc.perform(post("/api/app/points/tasks/share")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "userId": %d,
            "competitionId": %d
          }
          """.formatted(studentId, competitionId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.granted").value(true))
      .andExpect(jsonPath("$.data.changeAmount").value(3))
      .andExpect(jsonPath("$.data.availablePoints").value(38));

    mockMvc.perform(post("/api/app/points/tasks/share")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "userId": %d,
            "competitionId": %d
          }
          """.formatted(studentId, competitionId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.message").value("今日已完成比赛分享"));

    mockMvc.perform(get("/api/app/points/tasks/" + studentId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.task.dailyCheckinDone").value(true))
      .andExpect(jsonPath("$.data.task.competitionShareDone").value(true))
      .andExpect(jsonPath("$.data.task.todayTaskPoints").value(8))
      .andExpect(jsonPath("$.data.overview.registeredCompetitionCount").value(1))
      .andExpect(jsonPath("$.data.overview.submittedWorkCount").value(1))
      .andExpect(jsonPath("$.data.overview.awardCount").value(1))
      .andExpect(jsonPath("$.data.overview.totalAwardPoints").value(30))
      .andExpect(jsonPath("$.data.overview.totalPoints").value(38));
  }

  private long resolveUserId(String studentNo) {
    UserEntity user = userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery()
      .eq(UserEntity::getStudentNo, studentNo));
    Assertions.assertNotNull(user);
    return user.getId();
  }

  private long extractLongField(MvcResult result, String fieldName) throws Exception {
    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    return root.at("/data/" + fieldName).asLong();
  }
}
