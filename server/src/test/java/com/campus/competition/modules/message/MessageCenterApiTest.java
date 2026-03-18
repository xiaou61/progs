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
class MessageCenterApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldSupportSystemMessagesPrivateConsultAndCompetitionGroupChat() throws Exception {
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
              "title": "消息中心联调验证赛",
              "description": "用于验证系统消息、私信咨询和比赛群聊能力",
              "signupStartAt": "%s",
              "signupEndAt": "%s",
              "startAt": "%s",
              "endAt": "%s",
              "quota": 80
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

    mockMvc.perform(get("/api/app/messages/system")
        .param("userId", String.valueOf(studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[0].title").value("欢迎使用校园竞赛平台"));

    mockMvc.perform(post("/api/app/messages/consult")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "competitionId": %d,
            "userId": %d,
            "content": "老师您好，我想咨询报名要求。"
          }
          """.formatted(competitionId, studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.peerUserId").value(teacherId))
      .andExpect(jsonPath("$.data.conversationType").value("PRIVATE"));

    mockMvc.perform(post("/api/app/messages/private")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "senderUserId": %d,
            "receiverUserId": %d,
            "content": "欢迎提问，报名通过后记得进群。"
          }
          """.formatted(teacherId, studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    mockMvc.perform(post("/api/app/messages/group")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "competitionId": %d,
            "senderUserId": %d,
            "content": "各位同学请在今晚六点前提交终稿。"
          }
          """.formatted(competitionId, teacherId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    mockMvc.perform(post("/api/app/messages/group")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "competitionId": %d,
            "senderUserId": %d,
            "content": "收到，我会按时提交。"
          }
          """.formatted(competitionId, studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    mockMvc.perform(get("/api/app/messages/private")
        .param("userId", String.valueOf(studentId))
        .param("peerUserId", String.valueOf(teacherId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[0].content").value("老师您好，我想咨询报名要求。"))
      .andExpect(jsonPath("$.data[1].content").value("欢迎提问，报名通过后记得进群。"));

    MvcResult teacherConversationResult = mockMvc.perform(get("/api/app/messages/conversations")
        .param("userId", String.valueOf(teacherId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andReturn();

    JsonNode teacherConversations = objectMapper
      .readTree(teacherConversationResult.getResponse().getContentAsString())
      .path("data");

    assertConversationUnread(teacherConversations, "PRIVATE", studentId, null, 1);
    assertConversationUnread(teacherConversations, "GROUP", null, competitionId, 1);

    mockMvc.perform(get("/api/app/messages/group/" + competitionId)
        .param("userId", String.valueOf(teacherId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[0].content").value("各位同学请在今晚六点前提交终稿。"))
      .andExpect(jsonPath("$.data[1].content").value("收到，我会按时提交。"));
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
    JsonNode root = objectMapper.readTree(responseBody);
    return root.at(pointer).asLong();
  }

  private void assertConversationUnread(
    JsonNode conversations,
    String conversationType,
    Long peerUserId,
    Long competitionId,
    int unreadCount
  ) {
    for (JsonNode item : conversations) {
      boolean typeMatched = conversationType.equals(item.path("conversationType").asText());
      boolean peerMatched = peerUserId == null || peerUserId.equals(item.path("peerUserId").isMissingNode()
        ? null
        : item.path("peerUserId").asLong());
      boolean competitionMatched = competitionId == null || competitionId.equals(item.path("competitionId").isMissingNode()
        ? null
        : item.path("competitionId").asLong());
      if (typeMatched && peerMatched && competitionMatched) {
        if (item.path("unreadCount").asInt() != unreadCount) {
          throw new AssertionError("会话未读数不正确");
        }
        return;
      }
    }
    throw new AssertionError("未找到目标会话");
  }
}
