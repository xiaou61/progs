package com.campus.competition.modules.dashboard;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class DashboardOverviewApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldProvideAdminAndTeacherDashboardOverviewWithCsvExport() throws Exception {
    LocalDateTime now = LocalDateTime.now();
    String signupStartAt = now.minusDays(1).withSecond(0).withNano(0).toString();
    String signupEndAt = now.plusHours(6).withSecond(0).withNano(0).toString();
    String startAt = now.minusHours(1).withSecond(0).withNano(0).toString();
    String endAt = now.plusDays(1).withSecond(0).withNano(0).toString();

    long teacherId = loginAndGetUserId("T20260001", "Abcd1234", "TEACHER");
    long studentId = loginAndGetUserId("S20260001", "Abcd1234", "STUDENT");

    long publishedCompetitionId = extractLongField(
      mockMvc.perform(post("/api/admin/competitions")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "organizerId": %d,
              "title": "数据大屏实战赛",
              "description": "用于验证后台和老师端大屏统计",
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

    mockMvc.perform(post("/api/admin/competitions/draft")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "organizerId": %d,
            "title": "老师看板草稿赛",
            "description": "用于验证草稿统计",
            "signupStartAt": "%s",
            "signupEndAt": "%s",
            "startAt": "%s",
            "endAt": "%s",
            "quota": 20
          }
          """.formatted(teacherId, signupStartAt, signupEndAt, startAt, endAt)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    mockMvc.perform(post("/api/app/registrations")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "competitionId": %d,
            "userId": %d
          }
          """.formatted(publishedCompetitionId, studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    mockMvc.perform(post("/api/app/submissions")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "competitionId": %d,
            "userId": %d,
            "fileUrl": "https://example.com/works/dashboard.pdf",
            "reuploadAllowed": false
          }
          """.formatted(publishedCompetitionId, studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    mockMvc.perform(post("/api/admin/scores/publish")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "competitionId": %d,
            "studentId": %d,
            "score": 95,
            "rank": 1,
            "awardName": "一等奖",
            "points": 30,
            "reviewerName": "王老师",
            "reviewComment": "整体表现优秀"
          }
          """.formatted(publishedCompetitionId, studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    mockMvc.perform(get("/api/admin/dashboard/overview"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.overview.totalCompetitionCount").value(2))
      .andExpect(jsonPath("$.data.overview.publishedCompetitionCount").value(1))
      .andExpect(jsonPath("$.data.overview.draftCompetitionCount").value(1))
      .andExpect(jsonPath("$.data.overview.totalRegistrationCount").value(1))
      .andExpect(jsonPath("$.data.overview.totalSubmissionCount").value(1))
      .andExpect(jsonPath("$.data.overview.totalAwardCount").value(1))
      .andExpect(jsonPath("$.data.overview.totalAwardPoints").value(30))
      .andExpect(jsonPath("$.data.statusDistribution[?(@.status=='PUBLISHED')].count").value(1))
      .andExpect(jsonPath("$.data.topCompetitions[0].title").value("数据大屏实战赛"))
      .andExpect(jsonPath("$.data.topCompetitions[0].registrationCount").value(1))
      .andExpect(jsonPath("$.data.topCompetitions[0].awardCount").value(1));

    mockMvc.perform(get("/api/app/dashboard/teachers/" + teacherId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.teacherId").value(teacherId))
      .andExpect(jsonPath("$.data.teacherName").value("王老师"))
      .andExpect(jsonPath("$.data.overview.competitionCount").value(2))
      .andExpect(jsonPath("$.data.overview.publishedCompetitionCount").value(1))
      .andExpect(jsonPath("$.data.overview.draftCompetitionCount").value(1))
      .andExpect(jsonPath("$.data.overview.totalRegistrationCount").value(1))
      .andExpect(jsonPath("$.data.overview.totalSubmissionCount").value(1))
      .andExpect(jsonPath("$.data.overview.totalAwardCount").value(1))
      .andExpect(jsonPath("$.data.competitions[0].title").value("数据大屏实战赛"));

    mockMvc.perform(get("/api/admin/dashboard/export"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("title,status,registrations,submissions,awards,points")))
      .andExpect(content().string(containsString("数据大屏实战赛,PUBLISHED,1,1,1,30")));

    mockMvc.perform(get("/api/app/dashboard/teachers/" + teacherId + "/export"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("teacherName,title,status,registrations,submissions,awards,points")))
      .andExpect(content().string(containsString("王老师,数据大屏实战赛,PUBLISHED,1,1,1,30")));
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
}
