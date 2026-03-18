package com.campus.competition.modules.registration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.model.RegisterCommand;
import com.campus.competition.modules.auth.persistence.UserEntity;
import com.campus.competition.modules.auth.service.AuthService;
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
class RegistrationManageApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private AuthService authService;

  @Test
  void shouldCancelRejectManualAddAndMarkAttendance() throws Exception {
    long teacherId = resolveUserId("T20260001");
    long studentId = resolveUserId("S20260001");
    long manualUserId = createUser("S20260002", "李同学", "13800000022");
    long presentUserId = createUser("S20260003", "赵同学", "13800000023");
    long absentUserId = createUser("S20260004", "孙同学", "13800000024");
    LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

    long competitionId = extractLongField(
      mockMvc.perform(post("/api/admin/competitions")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "organizerId": %d,
              "title": "参赛管理增强验证赛",
              "description": "用于验证取消报名、后台驳回、手动加人和到场标记",
              "signupStartAt": "%s",
              "signupEndAt": "%s",
              "startAt": "%s",
              "endAt": "%s",
              "quota": 50
            }
            """.formatted(
            teacherId,
            now.minusDays(1),
            now.plusDays(2),
            now.plusDays(3),
            now.plusDays(4))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andReturn(),
      "competitionId"
    );

    long registrationId = extractLongField(
      mockMvc.perform(post("/api/app/registrations")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "competitionId": %d,
              "userId": %d
            }
            """.formatted(competitionId, studentId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andReturn(),
      "registrationId"
    );

    mockMvc.perform(get("/api/admin/registrations/competition/" + competitionId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data[0].id").value(registrationId))
      .andExpect(jsonPath("$.data[0].status").value("REGISTERED"))
      .andExpect(jsonPath("$.data[0].attendanceStatus").value("PENDING"));

    mockMvc.perform(get("/api/app/registrations/competition/" + competitionId + "/user/" + studentId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.id").value(registrationId))
      .andExpect(jsonPath("$.data.status").value("REGISTERED"));

    mockMvc.perform(post("/api/app/registrations/" + registrationId + "/cancel")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "userId": %d
          }
          """.formatted(studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.cancelled").value(true));

    mockMvc.perform(get("/api/app/registrations/competition/" + competitionId + "/user/" + studentId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.id").value(registrationId))
      .andExpect(jsonPath("$.data.status").value("CANCELLED"));

    long manualRegistrationId = extractLongField(
      mockMvc.perform(post("/api/admin/registrations/manual")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "competitionId": %d,
              "userId": %d,
              "remark": "后台补录"
            }
            """.formatted(competitionId, manualUserId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andReturn(),
      "registrationId"
    );

    mockMvc.perform(post("/api/admin/registrations/" + manualRegistrationId + "/reject")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "reason": "资料不完整"
          }
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.rejected").value(true));

    long presentRegistrationId = extractLongField(
      mockMvc.perform(post("/api/admin/registrations/manual")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "competitionId": %d,
              "userId": %d,
              "remark": "现场补录"
            }
            """.formatted(competitionId, presentUserId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andReturn(),
      "registrationId"
    );

    mockMvc.perform(post("/api/admin/registrations/" + presentRegistrationId + "/attendance")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "attendanceStatus": "PRESENT"
          }
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.marked").value(true));

    long absentRegistrationId = extractLongField(
      mockMvc.perform(post("/api/admin/registrations/manual")
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "competitionId": %d,
              "userId": %d,
              "remark": "预留缺席"
            }
            """.formatted(competitionId, absentUserId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andReturn(),
      "registrationId"
    );

    mockMvc.perform(post("/api/admin/registrations/" + absentRegistrationId + "/attendance")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "attendanceStatus": "ABSENT"
          }
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.marked").value(true));

    mockMvc.perform(get("/api/admin/registrations/competition/" + competitionId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.length()").value(4))
      .andExpect(jsonPath("$.data[0].status").value("CANCELLED"))
      .andExpect(jsonPath("$.data[1].status").value("REJECTED"))
      .andExpect(jsonPath("$.data[1].remark").value("资料不完整"))
      .andExpect(jsonPath("$.data[2].attendanceStatus").value("PRESENT"))
      .andExpect(jsonPath("$.data[3].attendanceStatus").value("ABSENT"));
  }

  private long createUser(String studentNo, String realName, String phone) {
    return authService.register(new RegisterCommand(studentNo, realName, phone, "STUDENT", "Abcd1234"));
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
