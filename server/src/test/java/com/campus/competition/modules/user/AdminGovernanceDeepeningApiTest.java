package com.campus.competition.modules.user;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    mockMvc.perform(post("/api/app/auth/login")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "studentNo": "A20260001",
            "password": "Abcd1234",
            "roleCode": "ADMIN"
          }
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.roleCode").value("ADMIN"));

    long studentId = findUserIdByStudentNo("S20260001");

    mockMvc.perform(post("/api/admin/users/" + studentId + "/violation")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "violating": true,
            "reason": "多次刷分咨询，进入人工复核"
          }
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.updated").value(true));

    mockMvc.perform(get("/api/admin/users"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[?(@.studentNo=='S20260001')].violationMarked").value(true))
      .andExpect(jsonPath("$.data[?(@.studentNo=='S20260001')].violationReason").value("多次刷分咨询，进入人工复核"));

    mockMvc.perform(get("/api/admin/logs")
        .param("action", "VIOLATION_MARK"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[0].action").value("VIOLATION_MARK"))
      .andExpect(jsonPath("$.data[0].target").value("S20260001"));

    mockMvc.perform(get("/api/admin/logs/export")
        .param("action", "VIOLATION_MARK"))
      .andExpect(status().isOk())
      .andExpect(content().contentType("text/csv;charset=UTF-8"))
      .andExpect(content().string(org.hamcrest.Matchers.containsString("VIOLATION_MARK")));

    mockMvc.perform(post("/api/admin/users/" + studentId + "/violation")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "violating": false,
            "reason": "人工复核完成，解除违规标记"
          }
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.updated").value(true));

    mockMvc.perform(get("/api/admin/users"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[?(@.studentNo=='S20260001')].violationMarked").value(false));

    mockMvc.perform(get("/api/admin/logs")
        .param("action", "VIOLATION_CLEAR"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[0].action").value("VIOLATION_CLEAR"));
  }

  private long findUserIdByStudentNo(String studentNo) throws Exception {
    MvcResult result = mockMvc.perform(get("/api/admin/users"))
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
