package com.campus.competition.modules.review.controller;

import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.review.model.ReviewTaskSummary;
import com.campus.competition.modules.review.model.SubmitReviewCommand;
import com.campus.competition.modules.review.service.ReviewService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {

  private final ReviewService reviewService;

  public AdminReviewController(ReviewService reviewService) {
    this.reviewService = reviewService;
  }

  @GetMapping("/tasks")
  public ApiResponse<List<ReviewTaskSummary>> listTasks(@RequestParam Long competitionId) {
    return ApiResponse.success(reviewService.listTasks(competitionId));
  }

  @PostMapping("/submit")
  public ApiResponse<Map<String, Boolean>> submit(@RequestBody SubmitReviewCommand command) {
    return ApiResponse.success(Map.of("reviewed", reviewService.submitReview(command)));
  }
}
