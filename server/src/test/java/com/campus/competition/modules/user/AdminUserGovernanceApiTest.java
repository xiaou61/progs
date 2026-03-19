package com.campus.competition.modules.user;

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
class AdminUserGovernanceApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldManageRolesAndGovernUsers() throws Exception {
    AuthSession adminSession = AuthTestSupport.login(mockMvc, objectMapper, "A20260001", "Abcd1234", "ADMIN");

    mockMvc.perform(AuthTestSupport.authorized(get("/api/admin/roles"), adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[0].roleCode").value("STUDENT"));

    mockMvc.perform(AuthTestSupport.authorized(
        post("/api/admin/roles")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "roleCode": "JUDGE",
              "roleName": "评委",
              "description": "负责评审作品与发布结果",
              "permissionCodes": ["REVIEW_MANAGE", "SCORE_PUBLISH"]
            }
            """),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.roleCode").value("JUDGE"))
      .andExpect(jsonPath("$.data.permissionCodes[0]").value("REVIEW_MANAGE"));

    mockMvc.perform(AuthTestSupport.authorized(
        put("/api/admin/roles/JUDGE")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "roleName": "赛事评委",
              "description": "负责评审作品、录入评语和协助发布结果",
              "permissionCodes": ["REVIEW_MANAGE", "SCORE_PUBLISH", "RESULT_VIEW"]
            }
            """),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.roleName").value("赛事评委"))
      .andExpect(jsonPath("$.data.permissionCodes[2]").value("RESULT_VIEW"));

    mockMvc.perform(AuthTestSupport.authorized(
        post("/api/admin/users")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "studentNo": "S20260101",
              "realName": "新建学生",
              "phone": "13800000101",
              "roleCode": "STUDENT",
              "password": "Abcd5678"
            }
            """),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.studentNo").value("S20260101"))
      .andExpect(jsonPath("$.data.realName").value("新建学生"))
      .andExpect(jsonPath("$.data.roleCode").value("STUDENT"))
      .andExpect(jsonPath("$.data.status").value("ENABLED"));

    mockMvc.perform(post("/api/app/auth/login")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "studentNo": "S20260101",
            "password": "Abcd5678",
            "roleCode": "STUDENT"
          }
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.roleCode").value("STUDENT"));

    long studentId = findUserIdByStudentNo("S20260001");

    mockMvc.perform(AuthTestSupport.authorized(
        post("/api/admin/users/" + studentId + "/freeze")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "reason": "测试冻结账号"
            }
            """),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.updated").value(true));

    mockMvc.perform(post("/api/app/auth/login")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "studentNo": "S20260001",
            "password": "Abcd1234",
            "roleCode": "STUDENT"
          }
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.message").value("账号已停用"));

    mockMvc.perform(AuthTestSupport.authorized(
        post("/api/admin/users/" + studentId + "/unfreeze"),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.updated").value(true));

    mockMvc.perform(AuthTestSupport.authorized(
        post("/api/admin/users/" + studentId + "/reset-password")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "newPassword": "Abcd5678"
            }
            """),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.updated").value(true));

    mockMvc.perform(AuthTestSupport.authorized(
        post("/api/admin/users/" + studentId + "/role")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "roleCode": "JUDGE"
            }
            """),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.roleCode").value("JUDGE"));

    mockMvc.perform(post("/api/app/auth/login")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "studentNo": "S20260001",
            "password": "Abcd5678",
            "roleCode": "JUDGE"
          }
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.roleCode").value("JUDGE"));

    mockMvc.perform(AuthTestSupport.authorized(get("/api/admin/users"), adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[?(@.studentNo=='S20260001')].roleCode").value("JUDGE"))
      .andExpect(jsonPath("$.data[?(@.studentNo=='S20260001')].status").value("ENABLED"));
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
