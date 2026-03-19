package com.campus.competition.support;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public final class AuthTestSupport {

  private AuthTestSupport() {
  }

  public static AuthSession login(MockMvc mockMvc, ObjectMapper objectMapper, String studentNo, String password, String roleCode)
    throws Exception {
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

    JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    return new AuthSession(
      data.path("userId").asLong(),
      data.path("roleCode").asText(),
      data.path("token").asText()
    );
  }

  public static MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder builder, AuthSession session) {
    return builder.header("Authorization", "Bearer " + session.token());
  }

  public record AuthSession(
    long userId,
    String roleCode,
    String token
  ) {
  }
}
