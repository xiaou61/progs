package com.campus.competition.modules.message;

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
class MessageConversationManageApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldSupportConversationPinMuteAndClearActions() throws Exception {
    LocalDateTime now = LocalDateTime.now();
    String signupStartAt = now.minusDays(1).withSecond(0).withNano(0).toString();
    String signupEndAt = now.plusHours(4).withSecond(0).withNano(0).toString();
    String startAt = now.plusHours(1).withSecond(0).withNano(0).toString();
    String endAt = now.plusDays(1).withSecond(0).withNano(0).toString();

    long teacherId = loginAndGetUserId("T20260001", "Abcd1234", "TEACHER");
    long studentId = loginAndGetUserId("S20260001", "Abcd1234", "STUDENT");
    long competitionId = extractLongField(
      mockMvc.perform(post("/api/admin/competitions")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "organizerId": %d,
              "title": "消息会话治理验证赛",
              "description": "用于验证置顶、免打扰和清空会话",
              "signupStartAt": "%s",
              "signupEndAt": "%s",
              "startAt": "%s",
              "endAt": "%s",
              "quota": 50
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

    mockMvc.perform(post("/api/app/messages/consult")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "competitionId": %d,
            "userId": %d,
            "content": "老师您好，我想先咨询一下赛题要求。"
          }
          """.formatted(competitionId, studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    mockMvc.perform(post("/api/app/messages/group")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "competitionId": %d,
            "senderUserId": %d,
            "content": "同学们好，群里会同步最新通知。"
          }
          """.formatted(competitionId, teacherId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    JsonNode teacherConversations = readConversations(teacherId);
    long privateConversationId = findConversationId(teacherConversations, "PRIVATE", studentId, null);
    long groupConversationId = findConversationId(teacherConversations, "GROUP", null, competitionId);

    mockMvc.perform(post("/api/app/messages/conversations/" + privateConversationId + "/pin")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "userId": %d,
            "enabled": true
          }
          """.formatted(teacherId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.pinned").value(true));

    mockMvc.perform(post("/api/app/messages/conversations/" + groupConversationId + "/mute")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "userId": %d,
            "enabled": true
          }
          """.formatted(teacherId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.muted").value(true));

    mockMvc.perform(post("/api/app/messages/conversations/" + privateConversationId + "/clear")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "userId": %d
          }
          """.formatted(teacherId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.lastMessage").isEmpty())
      .andExpect(jsonPath("$.data.unreadCount").value(0));

    JsonNode refreshedTeacherConversations = readConversations(teacherId);
    assertConversationFlag(refreshedTeacherConversations, "PRIVATE", studentId, null, "pinned", true);
    assertConversationFlag(refreshedTeacherConversations, "GROUP", null, competitionId, "muted", true);
    assertConversationLastMessage(refreshedTeacherConversations, "PRIVATE", studentId, null, "");

    mockMvc.perform(get("/api/app/messages/private")
        .param("userId", String.valueOf(teacherId))
        .param("peerUserId", String.valueOf(studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.length()").value(0));

    mockMvc.perform(post("/api/app/messages/private")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "senderUserId": %d,
            "receiverUserId": %d,
            "content": "老师您好，我已经看完规则了。"
          }
          """.formatted(studentId, teacherId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    JsonNode unreadTeacherConversations = readConversations(teacherId);
    assertConversationUnread(unreadTeacherConversations, "PRIVATE", studentId, null, 1);

    mockMvc.perform(get("/api/app/messages/private")
        .param("userId", String.valueOf(teacherId))
        .param("peerUserId", String.valueOf(studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.length()").value(1))
      .andExpect(jsonPath("$.data[0].content").value("老师您好，我已经看完规则了。"));
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

  private JsonNode readConversations(long userId) throws Exception {
    MvcResult result = mockMvc.perform(get("/api/app/messages/conversations")
        .param("userId", String.valueOf(userId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
  }

  private long extractLongField(MvcResult result, String fieldName) throws Exception {
    return extractLongValue(result.getResponse().getContentAsString(), "/data/" + fieldName);
  }

  private long extractLongValue(String responseBody, String pointer) throws Exception {
    return objectMapper.readTree(responseBody).at(pointer).asLong();
  }

  private long findConversationId(JsonNode conversations, String conversationType, Long peerUserId, Long competitionId) {
    for (JsonNode item : conversations) {
      if (matchesConversation(item, conversationType, peerUserId, competitionId)) {
        return item.path("id").asLong();
      }
    }
    throw new AssertionError("未找到目标会话");
  }

  private void assertConversationUnread(
    JsonNode conversations,
    String conversationType,
    Long peerUserId,
    Long competitionId,
    int unreadCount
  ) {
    for (JsonNode item : conversations) {
      if (matchesConversation(item, conversationType, peerUserId, competitionId)) {
        if (item.path("unreadCount").asInt() != unreadCount) {
          throw new AssertionError("会话未读数不正确");
        }
        return;
      }
    }
    throw new AssertionError("未找到目标会话");
  }

  private void assertConversationFlag(
    JsonNode conversations,
    String conversationType,
    Long peerUserId,
    Long competitionId,
    String fieldName,
    boolean expected
  ) {
    for (JsonNode item : conversations) {
      if (matchesConversation(item, conversationType, peerUserId, competitionId)) {
        if (item.path(fieldName).asBoolean() != expected) {
          throw new AssertionError("会话标记字段不正确: " + fieldName);
        }
        return;
      }
    }
    throw new AssertionError("未找到目标会话");
  }

  private void assertConversationLastMessage(
    JsonNode conversations,
    String conversationType,
    Long peerUserId,
    Long competitionId,
    String expectedMessage
  ) {
    for (JsonNode item : conversations) {
      if (matchesConversation(item, conversationType, peerUserId, competitionId)) {
        String actualMessage = item.path("lastMessage").asText("");
        if (!actualMessage.contains(expectedMessage)) {
          throw new AssertionError("会话预览内容不正确");
        }
        return;
      }
    }
    throw new AssertionError("未找到目标会话");
  }

  private boolean matchesConversation(JsonNode item, String conversationType, Long peerUserId, Long competitionId) {
    boolean typeMatched = conversationType.equals(item.path("conversationType").asText());
    boolean peerMatched = peerUserId == null || peerUserId.equals(item.path("peerUserId").isMissingNode()
      ? null
      : item.path("peerUserId").asLong());
    boolean competitionMatched = competitionId == null || competitionId.equals(item.path("competitionId").isMissingNode()
      ? null
      : item.path("competitionId").asLong());
    return typeMatched && peerMatched && competitionMatched;
  }
}
