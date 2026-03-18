package com.campus.competition.modules.auth;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.campus.competition.modules.auth.model.RegisterCommand;
import com.campus.competition.modules.auth.service.AuthService;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

  @Test
  void shouldRegisterStudentWithUniqueStudentNo() {
    AuthService authService = new AuthService();
    RegisterCommand command = new RegisterCommand(
      "20260001",
      "张三",
      "13800000000",
      "STUDENT",
      "Abcd1234"
    );

    Long userId = authService.register(command);

    assertNotNull(userId);
  }
}
