package com.campus.competition.modules.profile;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.persistence.UserEntity;
import com.campus.competition.modules.profile.mapper.FeedbackMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo-data"})
@Transactional
class ProfileApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private FeedbackMapper feedbackMapper;

  @Test
  void shouldQueryUpdatePasswordFeedbackAndCancelProfile() throws Exception {
    Long studentId = resolveStudentId();

    mockMvc.perform(get("/api/app/profile")
        .param("userId", String.valueOf(studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.studentNo").value("S20260001"))
      .andExpect(jsonPath("$.data.realName").value("张同学"));

    mockMvc.perform(put("/api/app/profile")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "userId": %d,
            "realName": "软件工程 2 班学生",
            "phone": "13900000002",
            "avatarUrl": "https://example.com/avatar-student.png",
            "gradeName": "2026级",
            "majorName": "软件工程",
            "departmentName": "",
            "bio": "专注校园创新比赛",
            "notifyResult": false,
            "notifyPoints": true,
            "allowPrivateMessage": false,
            "publicCompetition": true,
            "publicPoints": false,
            "publicSubmission": false
          }
          """.formatted(studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.realName").value("软件工程 2 班学生"))
      .andExpect(jsonPath("$.data.avatarUrl").value("https://example.com/avatar-student.png"))
      .andExpect(jsonPath("$.data.notifyResult").value(false))
      .andExpect(jsonPath("$.data.allowPrivateMessage").value(false));

    mockMvc.perform(post("/api/app/profile/password")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "userId": %d,
            "oldPassword": "Abcd1234",
            "newPassword": "Abcd5678"
          }
          """.formatted(studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    mockMvc.perform(post("/api/app/auth/login")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "studentNo": "S20260001",
            "password": "Abcd5678",
            "roleCode": "STUDENT"
          }
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.userId").value(studentId));

    mockMvc.perform(post("/api/app/profile/feedback")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "userId": %d,
            "content": "希望个人中心支持更多作品筛选能力",
            "imageUrls": [
              "https://example.com/feedback-1.png"
            ]
          }
          """.formatted(studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    org.junit.jupiter.api.Assertions.assertEquals(1L, feedbackMapper.selectCount(null));

    mockMvc.perform(post("/api/app/profile/cancel")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "userId": %d,
            "confirmText": "确认注销"
          }
          """.formatted(studentId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0));

    org.junit.jupiter.api.Assertions.assertEquals("CANCELLED", userMapper.selectById(studentId).getStatus());

    mockMvc.perform(post("/api/app/auth/login")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "studentNo": "S20260001",
            "password": "Abcd5678",
            "roleCode": "STUDENT"
          }
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.message").value("账号已停用"));
  }

  private Long resolveStudentId() {
    UserEntity user = userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery()
      .eq(UserEntity::getStudentNo, "S20260001"));
    org.junit.jupiter.api.Assertions.assertNotNull(user);
    return user.getId();
  }
}
