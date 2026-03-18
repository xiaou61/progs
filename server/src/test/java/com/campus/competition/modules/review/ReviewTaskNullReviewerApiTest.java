package com.campus.competition.modules.review;

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
import org.hamcrest.Matchers;
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
class ReviewTaskNullReviewerApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserMapper userMapper;

  @Test
  void shouldNotReturnPlaceholderReviewerNameForPendingTask() throws Exception {
    String adminToken = loginAndGetToken("A20260001", "Abcd1234", "ADMIN");
    String studentToken = loginAndGetToken("S20260001", "Abcd1234", "STUDENT");
    long teacherId = resolveUserId("T20260001");
    long studentId = resolveUserId("S20260001");
    LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

    long competitionId = extractLongField(
      mockMvc.perform(
          post("/api/admin/competitions")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(APPLICATION_JSON)
            .content("""
              {
                "organizerId": %d,
                "title": "待评任务空评委名验证赛",
                "description": "用于验证待评任务不再回填占位评委名",
                "signupStartAt": "%s",
                "signupEndAt": "%s",
                "startAt": "%s",
                "endAt": "%s",
                "quota": 20
              }
              """.formatted(
              teacherId,
              now.minusDays(1),
              now.plusDays(2),
              now.minusHours(1),
              now.plusDays(3))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andReturn(),
      "competitionId"
    );

    mockMvc.perform(
        post("/api/app/registrations")
          .header("Authorization", "Bearer " + studentToken)
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "competitionId": %d,
              "userId": %d
            }
            """.formatted(competitionId, studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    mockMvc.perform(
        post("/api/app/submissions")
          .header("Authorization", "Bearer " + studentToken)
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "competitionId": %d,
              "userId": %d,
              "fileUrl": "https://example.com/pending-review-work.pdf",
              "reuploadAllowed": false
            }
            """.formatted(competitionId, studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    mockMvc.perform(
        get("/api/admin/reviews/tasks")
          .header("Authorization", "Bearer " + adminToken)
          .param("competitionId", String.valueOf(competitionId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[0].status").value("PENDING"))
      .andExpect(jsonPath("$.data[0].reviewerName").value(Matchers.nullValue()));
  }

  private String loginAndGetToken(String studentNo, String password, String roleCode) throws Exception {
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

    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    return root.path("data").path("token").asText();
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
