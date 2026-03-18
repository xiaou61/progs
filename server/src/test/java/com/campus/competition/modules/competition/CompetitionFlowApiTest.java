package com.campus.competition.modules.competition;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
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
class CompetitionFlowApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldCompleteTeacherToStudentCompetitionFlow() throws Exception {
    LocalDateTime now = LocalDateTime.now();
    String signupStartAt = now.minusDays(1).withSecond(0).withNano(0).toString();
    String signupEndAt = now.plusHours(2).withSecond(0).withNano(0).toString();
    String startAt = now.minusHours(1).withSecond(0).withNano(0).toString();
    String endAt = now.plusHours(2).withSecond(0).withNano(0).toString();

    long teacherId = loginAndGetUserId("T20260001", "Abcd1234", "TEACHER");
    long studentId = loginAndGetUserId("S20260001", "Abcd1234", "STUDENT");

    long competitionId = extractLongField(
      mockMvc.perform(post("/api/admin/competitions")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "organizerId": %d,
              "title": "自动化联调校园创新赛",
              "description": "用于验证老师发布、学生参与、后台出结果的完整链路",
              "signupStartAt": "%s",
              "signupEndAt": "%s",
              "startAt": "%s",
              "endAt": "%s",
              "quota": 120
            }
            """.formatted(teacherId, signupStartAt, signupEndAt, startAt, endAt)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andReturn(),
      "competitionId"
    );

    mockMvc.perform(get("/api/app/competitions/" + competitionId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.id").value(competitionId))
      .andExpect(jsonPath("$.data.title").value("自动化联调校园创新赛"));

    long registrationId = extractLongField(
      mockMvc.perform(post("/api/app/registrations")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "competitionId": %d,
              "userId": %d
            }
            """.formatted(competitionId, studentId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andReturn(),
      "registrationId"
    );

    mockMvc.perform(get("/api/app/registrations/competition/" + competitionId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[0].id").value(registrationId))
      .andExpect(jsonPath("$.data[0].userId").value(studentId))
      .andExpect(jsonPath("$.data[0].status").value("REGISTERED"));

    mockMvc.perform(post("/api/app/checkins")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "competitionId": %d,
            "userId": %d,
            "method": "QRCODE"
          }
          """.formatted(competitionId, studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.checked").value(true));

    mockMvc.perform(get("/api/app/checkins/competition/" + competitionId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[0].userId").value(studentId))
      .andExpect(jsonPath("$.data[0].method").value("QRCODE"));

    long submissionId = extractLongField(
      mockMvc.perform(post("/api/app/submissions")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "competitionId": %d,
              "userId": %d,
              "fileUrl": "https://example.com/innovation-v1.pptx",
              "reuploadAllowed": false
            }
            """.formatted(competitionId, studentId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andReturn(),
      "submissionId"
    );

    mockMvc.perform(get("/api/app/submissions/competition/" + competitionId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[0].id").value(submissionId))
      .andExpect(jsonPath("$.data[0].userId").value(studentId))
      .andExpect(jsonPath("$.data[0].versionNo").value(1));

    long scoreId = extractLongField(
      mockMvc.perform(post("/api/admin/scores/publish")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "competitionId": %d,
              "studentId": %d,
              "score": 96,
              "rank": 1,
              "awardName": "一等奖",
              "points": 30
            }
            """.formatted(competitionId, studentId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andReturn(),
      "scoreId"
    );

    mockMvc.perform(get("/api/admin/scores/competition/" + competitionId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[0].id").value(scoreId))
      .andExpect(jsonPath("$.data[0].studentId").value(studentId))
      .andExpect(jsonPath("$.data[0].points").value(30));

    mockMvc.perform(get("/api/app/results/student/" + studentId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.account.userId").value(studentId))
      .andExpect(jsonPath("$.data.account.availablePoints").value(30))
      .andExpect(jsonPath("$.data.account.totalPoints").value(30))
      .andExpect(jsonPath("$.data.results[0].competitionId").value(competitionId))
      .andExpect(jsonPath("$.data.results[0].awardName").value("一等奖"))
      .andExpect(jsonPath("$.data.records[0].changeAmount").value(30))
      .andExpect(jsonPath("$.data.records[0].bizId").value(scoreId));
  }

  private long loginAndGetUserId(String studentNo, String password, String roleCode) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/app/auth/login")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "studentNo": "%s",
            "password": "%s",
            "roleCode": "%s"
          }
          """.formatted(studentNo, password, roleCode)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andReturn();

    return extractLongValue(result.getResponse().getContentAsString(), "/data/userId");
  }

  private long extractLongField(MvcResult result, String fieldName) throws Exception {
    return extractLongValue(result.getResponse().getContentAsString(), "/data/" + fieldName);
  }

  private long extractLongValue(String responseBody, String pointer) throws Exception {
    JsonNode root = objectMapper.readTree(responseBody);
    return root.at(pointer).asLong();
  }
}
