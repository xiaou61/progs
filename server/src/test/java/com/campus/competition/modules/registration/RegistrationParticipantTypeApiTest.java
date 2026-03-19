package com.campus.competition.modules.registration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
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
class RegistrationParticipantTypeApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserMapper userMapper;

  @Test
  void shouldRejectRoleMismatchRegistrationByParticipantType() throws Exception {
    AuthSession adminSession = AuthTestSupport.login(mockMvc, objectMapper, "A20260001", "Abcd1234", "ADMIN");
    AuthSession studentSession = AuthTestSupport.login(mockMvc, objectMapper, "S20260001", "Abcd1234", "STUDENT");
    AuthSession teacherSession = AuthTestSupport.login(mockMvc, objectMapper, "T20260001", "Abcd1234", "TEACHER");
    long organizerId = resolveUserId("T20260001");
    long advisorTeacherId = resolveUserId("T20260001");
    LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

    long studentCompetitionId = createCompetition(
      adminSession,
      organizerId,
      now,
      """
        "participantType": "STUDENT_ONLY",
        "advisorTeacherId": %d
        """.formatted(advisorTeacherId)
    );

    long teacherCompetitionId = createCompetition(
      adminSession,
      organizerId,
      now,
      """
        "participantType": "TEACHER_ONLY"
        """
    );

    mockMvc.perform(AuthTestSupport.authorized(
        post("/api/app/registrations")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "competitionId": %d,
              "userId": %d
            }
            """.formatted(studentCompetitionId, teacherSession.userId())),
        teacherSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.message").value("当前比赛仅限学生报名"));

    mockMvc.perform(AuthTestSupport.authorized(
        post("/api/app/registrations")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "competitionId": %d,
              "userId": %d
            }
            """.formatted(teacherCompetitionId, studentSession.userId())),
        studentSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.message").value("当前比赛仅限老师报名"));

    mockMvc.perform(AuthTestSupport.authorized(
        post("/api/app/registrations")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "competitionId": %d,
              "userId": %d
            }
            """.formatted(studentCompetitionId, studentSession.userId())),
        studentSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.registrationId").exists());
  }

  private long createCompetition(AuthSession adminSession, long organizerId, LocalDateTime now, String extensionJson) throws Exception {
    MvcResult result = mockMvc.perform(AuthTestSupport.authorized(
        post("/api/admin/competitions")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "organizerId": %d,
              "title": "报名参赛类型验证赛",
              "description": "用于验证报名时的角色限制",
              "signupStartAt": "%s",
              "signupEndAt": "%s",
              "startAt": "%s",
              "endAt": "%s",
              "quota": 40,
              %s
            }
            """.formatted(
            organizerId,
            now.minusDays(1),
            now.plusDays(2),
            now.plusDays(3),
            now.plusDays(4),
            extensionJson.trim())),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andReturn();

    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    return root.path("data").path("competitionId").asLong();
  }

  private long resolveUserId(String studentNo) {
    UserEntity user = userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery()
      .eq(UserEntity::getStudentNo, studentNo));
    Assertions.assertNotNull(user);
    return user.getId();
  }
}
