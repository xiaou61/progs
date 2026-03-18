package com.campus.competition.modules.common.config;

import com.campus.competition.modules.profile.service.ProfileFileStorageService;
import com.campus.competition.modules.submission.service.SubmissionFileStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadStaticResourceConfig implements WebMvcConfigurer {

  private final ProfileFileStorageService profileFileStorageService;
  private final SubmissionFileStorageService submissionFileStorageService;

  public UploadStaticResourceConfig(
    ProfileFileStorageService profileFileStorageService,
    SubmissionFileStorageService submissionFileStorageService
  ) {
    this.profileFileStorageService = profileFileStorageService;
    this.submissionFileStorageService = submissionFileStorageService;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/uploads/profile/**")
      .addResourceLocations(profileFileStorageService.getResourceLocation());
    registry.addResourceHandler("/uploads/submissions/**")
      .addResourceLocations(submissionFileStorageService.getResourceLocation());
  }
}
