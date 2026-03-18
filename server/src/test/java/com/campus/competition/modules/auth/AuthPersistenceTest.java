package com.campus.competition.modules.auth;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.model.RegisterCommand;
import com.campus.competition.modules.auth.persistence.UserEntity;
import com.campus.competition.modules.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthPersistenceTest {

  @Autowired
  private AuthService authService;

  @Autowired
  private UserMapper userMapper;

  @Test
  void shouldPersistUserIntoDatabase() {
    Long userId = authService.register(new RegisterCommand(
      "20260011",
      "王老师",
      "13800000011",
      "TEACHER",
      "Abcd1234"
    ));

    UserEntity entity = userMapper.selectById(userId);

    assertNotNull(entity);
    assertNotEquals("Abcd1234", entity.getPasswordHash());
  }
}
