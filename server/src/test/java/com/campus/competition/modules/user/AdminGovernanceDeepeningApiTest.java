package com.campus.competition.modules.user;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campus.competition.support.AuthTestSupport;
import com.campus.competition.support.AuthTestSupport.AuthSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class AdminGovernanceDeepeningApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldSupportAdminLoginViolationGovernanceAndLogExport() throws Exception {
    AuthSession adminSession = AuthTestSupport.login(mockMvc, objectMapper, "A20260001", "Abcd1234", "ADMIN");

    mockMvc.perform(AuthTestSupport.authorized(
        post("/api/admin/users")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "studentNo": "T20260102",
              "realName": "新建老师",
              "phone": "13800000102",
              "roleCode": "TEACHER",
              "password": "Abcd5678"
            }
            """),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.studentNo").value("T20260102"));

    long studentId = findUserIdByStudentNo("S20260001");

    mockMvc.perform(AuthTestSupport.authorized(
        get("/api/admin/logs")
          .param("action", "USER_CREATE"),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[0].action").value("USER_CREATE"))
      .andExpect(jsonPath("$.data[0].target").value("T20260102"));

    mockMvc.perform(AuthTestSupport.authorized(
        post("/api/admin/users/" + studentId + "/violation")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "violating": true,
              "reason": "多次刷分咨询，进入人工复核"
            }
            """),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.updated").value(true));

    mockMvc.perform(AuthTestSupport.authorized(get("/api/admin/users"), adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[?(@.studentNo=='S20260001')].violationMarked").value(true))
      .andExpect(jsonPath("$.data[?(@.studentNo=='S20260001')].violationReason").value("多次刷分咨询，进入人工复核"));

    mockMvc.perform(AuthTestSupport.authorized(
        get("/api/admin/logs")
          .param("action", "VIOLATION_MARK"),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[0].action").value("VIOLATION_MARK"))
      .andExpect(jsonPath("$.data[0].target").value("S20260001"));

    mockMvc.perform(AuthTestSupport.authorized(
        get("/api/admin/logs/export")
          .param("action", "VIOLATION_MARK"),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(content().contentType("text/csv;charset=UTF-8"))
      .andExpect(content().string(org.hamcrest.Matchers.containsString("VIOLATION_MARK")));

    mockMvc.perform(AuthTestSupport.authorized(
        post("/api/admin/users/" + studentId + "/violation")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "violating": false,
              "reason": "人工复核完成，解除违规标记"
            }
            """),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.updated").value(true));

    mockMvc.perform(AuthTestSupport.authorized(get("/api/admin/users"), adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[?(@.studentNo=='S20260001')].violationMarked").value(false));

    mockMvc.perform(AuthTestSupport.authorized(
        get("/api/admin/logs")
          .param("action", "VIOLATION_CLEAR"),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[0].action").value("VIOLATION_CLEAR"));
  }

  private long findUserIdByStudentNo(String studentNo) throws Exception {
    AuthSession adminSession = AuthTestSupport.login(mockMvc, objectMapper, "A20260001", "Abcd1234", "ADMIN");
    MvcResult result = mockMvc.perform(AuthTestSupport.authorized(get("/api/admin/users"), adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andReturn();

    JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    for (JsonNode item : data) {
      if (studentNo.equals(item.path("studentNo").asText())) {
        return item.path("id").asLong();
      }
    }
    throw new AssertionError("未找到目标用户");
  }
}
