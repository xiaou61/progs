package com.campus.competition.modules.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.campus.competition.modules.auth.security.AuthContext;
import com.campus.competition.modules.auth.security.AuthPrincipal;
import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.system.controller.AdminBannerFileController;
import com.campus.competition.modules.system.model.BannerFileUploadResult;
import com.campus.competition.modules.system.service.BannerFileStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class AdminBannerFileControllerTest {

  @TempDir
  java.nio.file.Path tempDir;

  @AfterEach
  void clearRequestContext() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  void shouldUploadBannerImageForAdmin() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(AuthContext.REQUEST_ATTRIBUTE, new AuthPrincipal(1001L, "A20260001", "ADMIN"));
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    BannerFileStorageService storageService = new BannerFileStorageService(tempDir.toString());
    AdminBannerFileController controller = new AdminBannerFileController(storageService);
    MockMultipartFile file = new MockMultipartFile("file", "banner.png", "image/png", new byte[] {1, 2, 3});

    ApiResponse<BannerFileUploadResult> response = controller.upload(file);

    assertEquals(0, response.getCode());
    assertTrue(response.getData().imageUrl().startsWith("/uploads/banners/"));
    assertEquals("banner.png", response.getData().originalFileName());
  }
}
