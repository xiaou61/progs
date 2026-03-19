package com.campus.competition.modules.competition;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class CompetitionParticipantTypeApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserMapper userMapper;

  @Test
  void shouldRejectInvalidAdvisorTeacherRules() throws Exception {
    AuthSession adminSession = AuthTestSupport.login(mockMvc, objectMapper, "A20260001", "Abcd1234", "ADMIN");
    long organizerId = resolveUserId("T20260001");
    long studentId = resolveUserId("S20260001");
    LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

    mockMvc.perform(AuthTestSupport.authorized(
        post("/api/admin/competitions/draft")
          .contentType(APPLICATION_JSON)
          .content(buildPayload(
            organizerId,
            now,
            """
              "participantType": "STUDENT_ONLY"
              """
          )),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.message").value("学生赛必须指定指导老师"));

    mockMvc.perform(AuthTestSupport.authorized(
        post("/api/admin/competitions/draft")
          .contentType(APPLICATION_JSON)
          .content(buildPayload(
            organizerId,
            now,
            """
              "participantType": "STUDENT_ONLY",
              "advisorTeacherId": %d
              """.formatted(studentId)
          )),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.message").value("指导老师必须是老师账号"));

    mockMvc.perform(AuthTestSupport.authorized(
        post("/api/admin/competitions/draft")
          .contentType(APPLICATION_JSON)
          .content(buildPayload(
            organizerId,
            now,
            """
              "participantType": "TEACHER_ONLY",
              "advisorTeacherId": %d
              """.formatted(organizerId)
          )),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.message").value("老师赛不能指定指导老师"));
  }

  @Test
  void shouldReturnParticipantTypeAndAdvisorTeacherForStudentCompetition() throws Exception {
    AuthSession adminSession = AuthTestSupport.login(mockMvc, objectMapper, "A20260001", "Abcd1234", "ADMIN");
    long organizerId = resolveUserId("T20260001");
    long advisorTeacherId = resolveUserId("T20260001");
    LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

    long competitionId = extractCompetitionId(
      mockMvc.perform(AuthTestSupport.authorized(
          post("/api/admin/competitions/draft")
            .contentType(APPLICATION_JSON)
            .content(buildPayload(
              organizerId,
              now,
              """
                "participantType": "STUDENT_ONLY",
                "advisorTeacherId": %d
                """.formatted(advisorTeacherId)
            )),
          adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andReturn()
    );

    mockMvc.perform(AuthTestSupport.authorized(get("/api/admin/competitions"), adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[0].id").value(competitionId))
      .andExpect(jsonPath("$.data[0].participantType").value("STUDENT_ONLY"))
      .andExpect(jsonPath("$.data[0].advisorTeacherId").value(advisorTeacherId));

    mockMvc.perform(get("/api/app/competitions/" + competitionId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.id").value(competitionId))
      .andExpect(jsonPath("$.data.participantType").value("STUDENT_ONLY"))
      .andExpect(jsonPath("$.data.advisorTeacherId").value(advisorTeacherId));
  }

  private long resolveUserId(String studentNo) {
    UserEntity user = userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery()
      .eq(UserEntity::getStudentNo, studentNo));
    Assertions.assertNotNull(user);
    return user.getId();
  }

  private String buildPayload(long organizerId, LocalDateTime now, String extensionJson) {
    return """
      {
        "organizerId": %d,
        "title": "参赛类型测试赛",
        "description": "用于验证学生赛和老师赛的发布规则",
        "signupStartAt": "%s",
        "signupEndAt": "%s",
        "startAt": "%s",
        "endAt": "%s",
        "quota": 60,
        %s
      }
      """.formatted(
      organizerId,
      now.plusDays(1),
      now.plusDays(2),
      now.plusDays(3),
      now.plusDays(4),
      extensionJson.trim()
    );
  }

  private long extractCompetitionId(MvcResult result) throws Exception {
    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    return root.path("data").path("competitionId").asLong();
  }
}
