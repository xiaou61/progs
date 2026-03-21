package com.campus.competition.modules.system.controller;

import com.campus.competition.modules.auth.security.AuthContext;
import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.system.model.BannerFileUploadResult;
import com.campus.competition.modules.system.service.BannerFileStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/banner-files")
public class AdminBannerFileController {

  private final BannerFileStorageService bannerFileStorageService;

  public AdminBannerFileController(BannerFileStorageService bannerFileStorageService) {
    this.bannerFileStorageService = bannerFileStorageService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<BannerFileUploadResult> upload(@RequestPart("file") MultipartFile file) {
    AuthContext.requireRole("ADMIN");
    return ApiResponse.success(bannerFileStorageService.store(file));
  }
}
