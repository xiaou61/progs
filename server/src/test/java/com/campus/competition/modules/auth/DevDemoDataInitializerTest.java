package com.campus.competition.modules.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.model.LoginCommand;
import com.campus.competition.modules.auth.model.LoginResult;
import com.campus.competition.modules.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles({"test", "demo-data"})
@Transactional
class DevDemoDataInitializerTest {

  @Autowired
  private AuthService authService;

  @Autowired
  private UserMapper userMapper;

  @Test
  void shouldSeedTeacherAndStudentAccountsForDemoLogin() {
    LoginResult teacher = authService.login(new LoginCommand("T20260001", "Abcd1234", "TEACHER"));
    LoginResult student = authService.login(new LoginCommand("S20260001", "Abcd1234", "STUDENT"));
    LoginResult admin = authService.login(new LoginCommand("A20260001", "Abcd1234", "ADMIN"));

    assertNotNull(teacher);
    assertNotNull(student);
    assertNotNull(admin);
    assertEquals(3L, userMapper.selectCount(null));
  }
}
