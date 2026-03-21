package com.campus.competition.modules.competition;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.persistence.UserEntity;
import com.campus.competition.support.AuthTestSupport;
import com.campus.competition.support.AuthTestSupport.AuthSession;
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
class CompetitionManageApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserMapper userMapper;

  @Test
  void shouldSaveDraftUpdateFeatureAndOfflineCompetition() throws Exception {
    long teacherId = resolveTeacherId();
    AuthSession adminSession = AuthTestSupport.login(mockMvc, objectMapper, "A20260001", "Abcd1234", "ADMIN");
    LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

    long competitionId = extractLongField(
      mockMvc.perform(AuthTestSupport.authorized(
          post("/api/admin/competitions/draft")
            .contentType(APPLICATION_JSON)
            .content("""
              {
                "organizerId": %d,
                "title": "校园未来设计赛（草稿）",
                "description": "草稿阶段的比赛描述",
                "signupStartAt": "%s",
                "signupEndAt": "%s",
                "startAt": "%s",
                "endAt": "%s",
                "quota": 80,
                "participantType": "STUDENT_ONLY",
                "advisorTeacherId": %d
              }
              """.formatted(
              teacherId,
              now.plusDays(1),
              now.plusDays(3),
              now.plusDays(4),
              now.plusDays(5),
              teacherId)),
          adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andReturn(),
      "competitionId"
    );

    mockMvc.perform(AuthTestSupport.authorized(get("/api/admin/competitions"), adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[0].id").value(competitionId))
      .andExpect(jsonPath("$.data[0].status").value("DRAFT"));

    mockMvc.perform(AuthTestSupport.authorized(
        put("/api/admin/competitions/" + competitionId)
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "organizerId": %d,
              "title": "校园未来设计赛",
              "description": "更新后的比赛描述",
              "signupStartAt": "%s",
              "signupEndAt": "%s",
              "startAt": "%s",
              "endAt": "%s",
              "quota": 120,
              "status": "PUBLISHED",
              "participantType": "STUDENT_ONLY",
              "advisorTeacherId": %d
            }
            """.formatted(
            teacherId,
            now.minusDays(1),
            now.plusDays(1),
            now.plusDays(2),
            now.plusDays(3),
            teacherId)),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.id").value(competitionId))
      .andExpect(jsonPath("$.data.title").value("校园未来设计赛"))
      .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

    mockMvc.perform(AuthTestSupport.authorized(
        post("/api/admin/competitions/" + competitionId + "/feature")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "recommended": true,
              "pinned": true
            }
            """),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.recommended").value(true))
      .andExpect(jsonPath("$.data.pinned").value(true));

    mockMvc.perform(AuthTestSupport.authorized(get("/api/admin/competitions"), adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[0].recommended").value(true))
      .andExpect(jsonPath("$.data[0].pinned").value(true));

    mockMvc.perform(get("/api/app/competitions"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[0].id").value(competitionId))
      .andExpect(jsonPath("$.data[0].status").value("PUBLISHED"));

    mockMvc.perform(AuthTestSupport.authorized(post("/api/admin/competitions/" + competitionId + "/offline"), adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.offline").value(true));

    mockMvc.perform(AuthTestSupport.authorized(get("/api/admin/competitions"), adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[0].status").value("OFFLINE"));

    mockMvc.perform(get("/api/app/competitions"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  void shouldPersistFeatureFlagsWithinMainSaveAndSortPinnedCompetitionsFirst() throws Exception {
    long teacherId = resolveTeacherId();
    AuthSession adminSession = AuthTestSupport.login(mockMvc, objectMapper, "A20260001", "Abcd1234", "ADMIN");
    LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

    long olderCompetitionId = extractLongField(
      mockMvc.perform(AuthTestSupport.authorized(
          post("/api/admin/competitions")
            .contentType(APPLICATION_JSON)
            .content("""
              {
                "organizerId": %d,
                "title": "较早创建的普通比赛",
                "description": "用于验证置顶排序",
                "signupStartAt": "%s",
                "signupEndAt": "%s",
                "startAt": "%s",
                "endAt": "%s",
                "quota": 60,
                "participantType": "STUDENT_ONLY",
                "advisorTeacherId": %d
              }
              """.formatted(
              teacherId,
              now.minusDays(3),
              now.plusDays(2),
              now.plusDays(3),
              now.plusDays(4),
              teacherId)),
          adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andReturn(),
      "competitionId"
    );

    long pinnedCompetitionId = extractLongField(
      mockMvc.perform(AuthTestSupport.authorized(
          post("/api/admin/competitions/draft")
            .contentType(APPLICATION_JSON)
            .content("""
              {
                "organizerId": %d,
                "title": "准备置顶的比赛",
                "description": "先保存草稿，再通过主保存更新置顶",
                "signupStartAt": "%s",
                "signupEndAt": "%s",
                "startAt": "%s",
                "endAt": "%s",
                "quota": 80,
                "participantType": "STUDENT_ONLY",
                "advisorTeacherId": %d
              }
              """.formatted(
              teacherId,
              now.minusDays(2),
              now.plusDays(2),
              now.plusDays(3),
              now.plusDays(4),
              teacherId)),
          adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andReturn(),
      "competitionId"
    );

    mockMvc.perform(AuthTestSupport.authorized(
        put("/api/admin/competitions/" + pinnedCompetitionId)
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "organizerId": %d,
              "title": "准备置顶的比赛",
              "description": "通过主保存直接写入推荐和置顶状态",
              "signupStartAt": "%s",
              "signupEndAt": "%s",
              "startAt": "%s",
              "endAt": "%s",
              "quota": 80,
              "status": "PUBLISHED",
              "participantType": "STUDENT_ONLY",
              "advisorTeacherId": %d,
              "recommended": true,
              "pinned": true
            }
            """.formatted(
            teacherId,
            now.minusDays(2),
            now.plusDays(2),
            now.plusDays(3),
            now.plusDays(4),
            teacherId)),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.id").value(pinnedCompetitionId))
      .andExpect(jsonPath("$.data.recommended").value(true))
      .andExpect(jsonPath("$.data.pinned").value(true));

    long newerCompetitionId = extractLongField(
      mockMvc.perform(AuthTestSupport.authorized(
          post("/api/admin/competitions")
            .contentType(APPLICATION_JSON)
            .content("""
              {
                "organizerId": %d,
                "title": "较晚创建的普通比赛",
                "description": "用于验证非置顶比赛不会压过置顶比赛",
                "signupStartAt": "%s",
                "signupEndAt": "%s",
                "startAt": "%s",
                "endAt": "%s",
                "quota": 90,
                "participantType": "STUDENT_ONLY",
                "advisorTeacherId": %d
              }
              """.formatted(
              teacherId,
              now.minusDays(1),
              now.plusDays(2),
              now.plusDays(3),
              now.plusDays(4),
              teacherId)),
          adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andReturn(),
      "competitionId"
    );

    mockMvc.perform(AuthTestSupport.authorized(get("/api/admin/competitions"), adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[0].id").value(pinnedCompetitionId))
      .andExpect(jsonPath("$.data[0].pinned").value(true))
      .andExpect(jsonPath("$.data[1].id").value(newerCompetitionId))
      .andExpect(jsonPath("$.data[2].id").value(olderCompetitionId));

    mockMvc.perform(get("/api/app/competitions"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[0].id").value(pinnedCompetitionId))
      .andExpect(jsonPath("$.data[0].pinned").value(true));
  }

  private long resolveTeacherId() {
    UserEntity teacher = userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery()
      .eq(UserEntity::getStudentNo, "T20260001"));
    Assertions.assertNotNull(teacher);
    return teacher.getId();
  }

  private long extractLongField(MvcResult result, String fieldName) throws Exception {
    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    return root.at("/data/" + fieldName).asLong();
  }
}
