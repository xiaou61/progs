package com.campus.competition.modules.auth;

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
class AuthGuardApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldRejectAdminEndpointWithoutToken() throws Exception {
    mockMvc.perform(get("/api/admin/users"))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.code").value(401));
  }

  @Test
  void shouldRejectAdminEndpointForNonAdminToken() throws Exception {
    String studentToken = loginAndGetToken("S20260001", "Abcd1234", "STUDENT");

    mockMvc.perform(get("/api/admin/users")
        .header("Authorization", "Bearer " + studentToken))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.code").value(403));
  }

  @Test
  void shouldRejectProfileAccessWhenRequestUserDoesNotMatchTokenOwner() throws Exception {
    String studentToken = loginAndGetToken("S20260001", "Abcd1234", "STUDENT");
    Long teacherId = resolveUserId("T20260001");

    mockMvc.perform(get("/api/app/profile")
        .param("userId", String.valueOf(teacherId))
        .header("Authorization", "Bearer " + studentToken))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.code").value(403));
  }

  @Test
  void shouldAllowProfileAccessWhenRequestUserMatchesTokenOwner() throws Exception {
    String studentToken = loginAndGetToken("S20260001", "Abcd1234", "STUDENT");
    Long studentId = resolveUserId("S20260001");

    mockMvc.perform(get("/api/app/profile")
        .param("userId", String.valueOf(studentId))
        .header("Authorization", "Bearer " + studentToken))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.studentNo").value("S20260001"));
  }

  @Test
  void shouldReturnDisplayIdentityOnLogin() throws Exception {
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
      .andExpect(jsonPath("$.data.studentNo").value("A20260001"))
      .andExpect(jsonPath("$.data.realName").value("系统管理员"));
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

  private Long resolveUserId(String studentNo) {
    UserEntity user = userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery()
      .eq(UserEntity::getStudentNo, studentNo));
    org.junit.jupiter.api.Assertions.assertNotNull(user);
    return user.getId();
  }
}
