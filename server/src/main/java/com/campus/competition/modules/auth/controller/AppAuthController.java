package com.campus.competition.modules.auth.controller;

import com.campus.competition.modules.auth.model.LoginCommand;
import com.campus.competition.modules.auth.model.LoginResult;
import com.campus.competition.modules.auth.model.RegisterCommand;
import com.campus.competition.modules.auth.service.AuthService;
import com.campus.competition.modules.common.model.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/auth")
public class AppAuthController {

  private final AuthService authService;

  public AppAuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ApiResponse<Map<String, Long>> register(@RequestBody RegisterCommand command) {
    Long userId = authService.register(command);
    return ApiResponse.success(Map.of("userId", userId));
  }

  @PostMapping("/login")
  public ApiResponse<LoginResult> login(@RequestBody LoginCommand command) {
    return ApiResponse.success(authService.login(command));
  }
}
