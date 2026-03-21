package com.campus.competition.modules.common.config;

import com.campus.competition.modules.profile.service.ProfileFileStorageService;
import com.campus.competition.modules.submission.service.SubmissionFileStorageService;
import com.campus.competition.modules.system.service.BannerFileStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadStaticResourceConfig implements WebMvcConfigurer {

  private final ProfileFileStorageService profileFileStorageService;
  private final SubmissionFileStorageService submissionFileStorageService;
  private final BannerFileStorageService bannerFileStorageService;

  public UploadStaticResourceConfig(
    ProfileFileStorageService profileFileStorageService,
    SubmissionFileStorageService submissionFileStorageService,
    BannerFileStorageService bannerFileStorageService
  ) {
    this.profileFileStorageService = profileFileStorageService;
    this.submissionFileStorageService = submissionFileStorageService;
    this.bannerFileStorageService = bannerFileStorageService;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/uploads/profile/**")
      .addResourceLocations(profileFileStorageService.getResourceLocation());
    registry.addResourceHandler("/uploads/submissions/**")
      .addResourceLocations(submissionFileStorageService.getResourceLocation());
    registry.addResourceHandler("/uploads/banners/**")
      .addResourceLocations(bannerFileStorageService.getResourceLocation());
  }
}
