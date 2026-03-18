package com.campus.competition.modules.profile.controller;

import com.campus.competition.modules.auth.security.AuthContext;
import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.profile.model.ProfileFileUploadResult;
import com.campus.competition.modules.profile.service.ProfileFileStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/app/profile/files")
public class AppProfileFileController {

  private final ProfileFileStorageService profileFileStorageService;

  public AppProfileFileController(ProfileFileStorageService profileFileStorageService) {
    this.profileFileStorageService = profileFileStorageService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<ProfileFileUploadResult> upload(@RequestPart("file") MultipartFile file) {
    AuthContext.currentUser();
    return ApiResponse.success(profileFileStorageService.store(file));
  }
}
