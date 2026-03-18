package com.campus.competition.modules.org.controller;

import com.campus.competition.modules.common.model.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/campuses")
public class AdminCampusController {

  @GetMapping
  public ApiResponse<List<CampusView>> list() {
    return ApiResponse.success(List.of(
      new CampusView(1L, "MAIN", "主校区", "ENABLED"),
      new CampusView(2L, "EAST", "东校区", "ENABLED")
    ));
  }

  public record CampusView(
    Long id,
    String campusCode,
    String campusName,
    String status
  ) {
  }
}
