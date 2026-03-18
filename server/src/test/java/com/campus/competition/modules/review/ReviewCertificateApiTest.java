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
class ReviewCertificateApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserMapper userMapper;

  @Test
  void shouldSubmitReviewAndExposeCertificateInStudentOverview() throws Exception {
    long teacherId = resolveUserId("T20260001");
    long studentId = resolveUserId("S20260001");
    LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

    long competitionId = extractLongField(
      mockMvc.perform(post("/api/admin/competitions")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "organizerId": %d,
              "title": "评审增强与电子奖状验证赛",
              "description": "用于验证评审意见和电子奖状首版闭环",
              "signupStartAt": "%s",
              "signupEndAt": "%s",
              "startAt": "%s",
              "endAt": "%s",
              "quota": 30
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

    long submissionId = extractLongField(
      mockMvc.perform(post("/api/app/submissions")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "competitionId": %d,
              "userId": %d,
              "fileUrl": "https://example.com/review-work-v1.pptx",
              "reuploadAllowed": false
            }
            """.formatted(competitionId, studentId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andReturn(),
      "submissionId"
    );

    mockMvc.perform(post("/api/admin/reviews/submit")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "competitionId": %d,
            "submissionId": %d,
            "studentId": %d,
            "reviewerName": "王老师",
            "reviewComment": "作品结构完整，方案表达清晰，建议补充落地数据。",
            "suggestedScore": 94
          }
          """.formatted(competitionId, submissionId, studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.reviewed").value(true));

    mockMvc.perform(get("/api/admin/reviews/tasks").param("competitionId", String.valueOf(competitionId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[0].status").value("COMPLETED"))
      .andExpect(jsonPath("$.data[0].reviewComment").value("作品结构完整，方案表达清晰，建议补充落地数据。"))
      .andExpect(jsonPath("$.data[0].suggestedScore").value(94));

    mockMvc.perform(post("/api/admin/scores/publish")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "competitionId": %d,
            "studentId": %d,
            "score": 96,
            "rank": 1,
            "awardName": "一等奖",
            "points": 30,
            "reviewerName": "王老师",
            "reviewComment": "作品结构完整，方案表达清晰，建议补充落地数据。"
          }
          """.formatted(competitionId, studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    mockMvc.perform(get("/api/app/results/student/" + studentId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.results[0].reviewerName").value("王老师"))
      .andExpect(jsonPath("$.data.results[0].reviewComment").value("作品结构完整，方案表达清晰，建议补充落地数据。"))
      .andExpect(jsonPath("$.data.results[0].certificateNo").isNotEmpty())
      .andExpect(jsonPath("$.data.results[0].certificateTitle").value("评审增强与电子奖状验证赛电子奖状"));
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
