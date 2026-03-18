package com.campus.competition.modules.audit;

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
class LocalAuditApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldBlockSensitiveContentAndRecordViolations() throws Exception {
    LocalDateTime now = LocalDateTime.now();
    String signupStartAt = now.minusDays(1).withSecond(0).withNano(0).toString();
    String signupEndAt = now.plusHours(6).withSecond(0).withNano(0).toString();
    String startAt = now.minusHours(1).withSecond(0).withNano(0).toString();
    String endAt = now.plusDays(1).withSecond(0).withNano(0).toString();

    long teacherId = loginAndGetUserId("T20260001", "Abcd1234", "TEACHER");
    long studentId = loginAndGetUserId("S20260001", "Abcd1234", "STUDENT");

    mockMvc.perform(post("/api/admin/competitions")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "organizerId": %d,
            "title": "刷分挑战赛",
            "description": "用于验证比赛内容敏感词拦截",
            "signupStartAt": "%s",
            "signupEndAt": "%s",
            "startAt": "%s",
            "endAt": "%s",
            "quota": 30
          }
          """.formatted(teacherId, signupStartAt, signupEndAt, startAt, endAt)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.message").value("比赛内容包含敏感词，请修改后再提交"));

    long competitionId = extractLongField(
      mockMvc.perform(post("/api/admin/competitions")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "organizerId": %d,
              "title": "本地审核联调赛",
              "description": "用于验证作品和消息审核",
              "signupStartAt": "%s",
              "signupEndAt": "%s",
              "startAt": "%s",
              "endAt": "%s",
              "quota": 30
            }
            """.formatted(teacherId, signupStartAt, signupEndAt, startAt, endAt)))
        .andExpect(status().isOk())
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

    mockMvc.perform(post("/api/app/messages/private")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "senderUserId": %d,
            "receiverUserId": %d,
            "content": "这里有刷分攻略，快看一下。"
          }
          """.formatted(studentId, teacherId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.message").value("消息内容包含敏感词，请修改后再发送"));

    mockMvc.perform(post("/api/app/submissions")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "competitionId": %d,
            "userId": %d,
            "fileUrl": "https://example.com/works/final-script.exe",
            "reuploadAllowed": false
          }
          """.formatted(competitionId, studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.message").value("作品文件类型不支持，请上传白名单格式文件"));

    mockMvc.perform(get("/api/admin/audit/rules"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.sensitiveWords[0]").value("作弊"))
      .andExpect(jsonPath("$.data.allowedSubmissionExtensions[0]").value("pdf"));

    MvcResult violationResult = mockMvc.perform(get("/api/admin/audit/violations"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.length()").value(3))
      .andReturn();

    JsonNode data = objectMapper.readTree(violationResult.getResponse().getContentAsString()).path("data");
    assertViolation(data.get(0), "SUBMISSION", "作品文件类型不支持", "final-script.exe");
    assertViolation(data.get(1), "MESSAGE", "消息内容包含敏感词", "刷分");
    assertViolation(data.get(2), "COMPETITION", "比赛内容包含敏感词", "刷分挑战赛");
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
    return objectMapper.readTree(responseBody).at(pointer).asLong();
  }

  private void assertViolation(JsonNode item, String scene, String reason, String snippet) {
    if (!scene.equals(item.path("scene").asText())) {
      throw new AssertionError("违规场景不正确");
    }
    if (!item.path("reason").asText("").contains(reason)) {
      throw new AssertionError("违规原因不正确");
    }
    if (!item.path("contentSnippet").asText("").contains(snippet)) {
      throw new AssertionError("违规内容摘要不正确");
    }
  }
}
