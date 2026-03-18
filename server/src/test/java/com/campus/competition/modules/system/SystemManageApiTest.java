package com.campus.competition.modules.system;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SystemManageApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldManageCampusBannerAndConfig() throws Exception {
    mockMvc.perform(post("/api/admin/campuses")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "campusCode": "WEST",
            "campusName": "西校区",
            "status": "ENABLED"
          }
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.campusCode").value("WEST"));

    mockMvc.perform(put("/api/admin/banners/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "title": "春季比赛季主视觉-更新",
            "status": "DISABLED",
            "jumpPath": "/pages/home/index"
          }
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.status").value("DISABLED"));

    mockMvc.perform(put("/api/admin/configs")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "platformName": "校园师生比赛管理平台 Pro",
            "mvpPhase": "Phase 2",
            "pointsEnabled": false,
            "submissionReuploadEnabled": false
          }
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.platformName").value("校园师生比赛管理平台 Pro"));

    mockMvc.perform(get("/api/admin/campuses"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[?(@.campusCode=='WEST')]").exists());

    mockMvc.perform(get("/api/admin/banners"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[0].jumpPath").value("/pages/home/index"));

    mockMvc.perform(get("/api/admin/configs"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.pointsEnabled").value(false));
  }
}
