package com.campus.competition.modules.submission.controller;

import com.campus.competition.modules.auth.security.AuthContext;
import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.submission.model.SubmissionFileUploadResult;
import com.campus.competition.modules.submission.service.SubmissionFileStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/app/submission-files")
public class AppSubmissionFileController {

  private final SubmissionFileStorageService submissionFileStorageService;

  public AppSubmissionFileController(SubmissionFileStorageService submissionFileStorageService) {
    this.submissionFileStorageService = submissionFileStorageService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<SubmissionFileUploadResult> upload(@RequestPart("file") MultipartFile file) {
    AuthContext.currentUser();
    return ApiResponse.success(submissionFileStorageService.store(file));
  }
}
