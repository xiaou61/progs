package com.campus.competition.modules.profile.controller;

import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.profile.model.CancelAccountCommand;
import com.campus.competition.modules.profile.model.ChangePasswordCommand;
import com.campus.competition.modules.profile.model.FeedbackCommand;
import com.campus.competition.modules.profile.model.ProfileSummary;
import com.campus.competition.modules.profile.model.UpdateProfileCommand;
import com.campus.competition.modules.profile.service.ProfileService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/profile")
public class AppProfileController {

  private final ProfileService profileService;

  public AppProfileController(ProfileService profileService) {
    this.profileService = profileService;
  }

  @GetMapping
  public ApiResponse<ProfileSummary> getProfile(@RequestParam Long userId) {
    return ApiResponse.success(profileService.getProfile(userId));
  }

  @PutMapping
  public ApiResponse<ProfileSummary> updateProfile(@RequestBody UpdateProfileCommand command) {
    return ApiResponse.success(profileService.updateProfile(command));
  }

  @PostMapping("/password")
  public ApiResponse<Map<String, Boolean>> changePassword(@RequestBody ChangePasswordCommand command) {
    return ApiResponse.success(Map.of("changed", profileService.changePassword(command)));
  }

  @PostMapping("/feedback")
  public ApiResponse<Map<String, Long>> submitFeedback(@RequestBody FeedbackCommand command) {
    return ApiResponse.success(Map.of("feedbackId", profileService.submitFeedback(command)));
  }

  @PostMapping("/cancel")
  public ApiResponse<Map<String, Boolean>> cancelAccount(@RequestBody CancelAccountCommand command) {
    return ApiResponse.success(Map.of("cancelled", profileService.cancelAccount(command)));
  }
}
