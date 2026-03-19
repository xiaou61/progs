package com.campus.competition.modules.competition;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campus.competition.support.AuthTestSupport;
import com.campus.competition.support.AuthTestSupport.AuthSession;
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
class TeacherCompetitionManageApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldAllowTeacherToManageOwnCompetitionsAndUpdateDraftWithoutDuplicating() throws Exception {
    AuthSession teacherSession = AuthTestSupport.login(mockMvc, objectMapper, "T20260001", "Abcd1234", "TEACHER");
    LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

    long competitionId = extractCompetitionId(
      mockMvc.perform(AuthTestSupport.authorized(
          post("/api/app/teachers/" + teacherSession.userId() + "/competitions/draft")
            .contentType(APPLICATION_JSON)
            .content("""
              {
                "title": "老师端草稿赛",
                "description": "老师端新建草稿",
                "signupStartAt": "%s",
                "signupEndAt": "%s",
                "startAt": "%s",
                "endAt": "%s",
                "quota": 60
              }
              """.formatted(
              now.plusDays(1),
              now.plusDays(2),
              now.plusDays(3),
              now.plusDays(4))),
          teacherSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andReturn()
    );

    mockMvc.perform(AuthTestSupport.authorized(
        get("/api/app/teachers/" + teacherSession.userId() + "/competitions"),
        teacherSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.length()").value(1))
      .andExpect(jsonPath("$.data[0].id").value(competitionId))
      .andExpect(jsonPath("$.data[0].status").value("DRAFT"));

    mockMvc.perform(AuthTestSupport.authorized(
        put("/api/app/teachers/" + teacherSession.userId() + "/competitions/" + competitionId)
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "title": "老师端草稿赛（更新）",
              "description": "老师端更新草稿",
              "signupStartAt": "%s",
              "signupEndAt": "%s",
              "startAt": "%s",
              "endAt": "%s",
              "quota": 90,
              "status": "DRAFT"
            }
            """.formatted(
            now.plusDays(1),
            now.plusDays(3),
            now.plusDays(4),
            now.plusDays(5))),
        teacherSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.id").value(competitionId))
      .andExpect(jsonPath("$.data.title").value("老师端草稿赛（更新）"))
      .andExpect(jsonPath("$.data.status").value("DRAFT"));

    mockMvc.perform(AuthTestSupport.authorized(
        get("/api/app/teachers/" + teacherSession.userId() + "/competitions"),
        teacherSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.length()").value(1))
      .andExpect(jsonPath("$.data[0].id").value(competitionId))
      .andExpect(jsonPath("$.data[0].title").value("老师端草稿赛（更新）"));
  }

  private long extractCompetitionId(MvcResult result) throws Exception {
    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    return root.path("data").path("competitionId").asLong();
  }
}
