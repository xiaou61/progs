package com.campus.competition.modules.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campus.competition.support.AuthTestSupport;
import com.campus.competition.support.AuthTestSupport.AuthSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo-data"})
class SystemManageApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void shouldManageCampusBannerAndConfig() throws Exception {
    AuthSession adminSession = AuthTestSupport.login(mockMvc, objectMapper, "A20260001", "Abcd1234", "ADMIN");

    mockMvc.perform(AuthTestSupport.authorized(
        post("/api/admin/campuses")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
            {
              "campusCode": "WEST",
              "campusName": "西校区",
              "status": "ENABLED"
            }
            """),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.campusCode").value("WEST"));

    mockMvc.perform(AuthTestSupport.authorized(
        put("/api/admin/banners/1")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
            {
              "title": "春季比赛季主视觉-更新",
              "status": "DISABLED",
              "jumpPath": "/pages/home/index"
            }
            """),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.status").value("DISABLED"));

    mockMvc.perform(AuthTestSupport.authorized(
        put("/api/admin/configs")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
            {
              "platformName": "校园师生比赛管理平台 Pro",
              "mvpPhase": "Phase 2",
              "pointsEnabled": false,
              "submissionReuploadEnabled": false
            }
            """),
        adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.platformName").value("校园师生比赛管理平台 Pro"));

    mockMvc.perform(AuthTestSupport.authorized(get("/api/admin/campuses"), adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[?(@.campusCode=='WEST')]").exists());

    mockMvc.perform(AuthTestSupport.authorized(get("/api/admin/banners"), adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[0].jumpPath").value("/pages/home/index"));

    mockMvc.perform(AuthTestSupport.authorized(get("/api/admin/configs"), adminSession))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.pointsEnabled").value(false));

    Integer campusCount = jdbcTemplate.queryForObject(
      "select count(*) from org_campus where campus_code = ?",
      Integer.class,
      "WEST"
    );
    assertThat(campusCount).isEqualTo(1);

    Integer bannerTableCount = jdbcTemplate.queryForObject(
      "select count(*) from information_schema.tables where upper(table_name) = ?",
      Integer.class,
      "SYS_BANNER"
    );
    assertThat(bannerTableCount).isEqualTo(1);

    Integer configTableCount = jdbcTemplate.queryForObject(
      "select count(*) from information_schema.tables where upper(table_name) = ?",
      Integer.class,
      "SYS_PLATFORM_CONFIG"
    );
    assertThat(configTableCount).isEqualTo(1);

    Map<String, Object> bannerRow = jdbcTemplate.queryForMap(
      "select title, status, jump_path from sys_banner where id = ?",
      1L
    );
    assertThat(bannerRow.get("TITLE")).isEqualTo("春季比赛季主视觉-更新");
    assertThat(bannerRow.get("STATUS")).isEqualTo("DISABLED");
    assertThat(bannerRow.get("JUMP_PATH")).isEqualTo("/pages/home/index");

    Map<String, Object> configRow = jdbcTemplate.queryForMap(
      "select platform_name, mvp_phase, points_enabled, submission_reupload_enabled from sys_platform_config where id = ?",
      1L
    );
    assertThat(configRow.get("PLATFORM_NAME")).isEqualTo("校园师生比赛管理平台 Pro");
    assertThat(configRow.get("MVP_PHASE")).isEqualTo("Phase 2");
    assertThat(configRow.get("POINTS_ENABLED")).isEqualTo(false);
    assertThat(configRow.get("SUBMISSION_REUPLOAD_ENABLED")).isEqualTo(false);
  }
}
