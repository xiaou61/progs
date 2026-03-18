package com.campus.competition.modules.auth.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.model.RegisterCommand;
import com.campus.competition.modules.auth.persistence.UserEntity;
import com.campus.competition.modules.auth.service.AuthService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "demo-data"})
public class DemoAccountInitializer {

  @Bean
  ApplicationRunner demoAccountRunner(AuthService authService, UserMapper userMapper) {
    return args -> {
      ensureAccount(authService, userMapper, new RegisterCommand(
        "T20260001",
        "王老师",
        "13800000011",
        "TEACHER",
        "Abcd1234"
      ));
      ensureAccount(authService, userMapper, new RegisterCommand(
        "S20260001",
        "张同学",
        "13800000001",
        "STUDENT",
        "Abcd1234"
      ));
      ensureAccount(authService, userMapper, new RegisterCommand(
        "A20260001",
        "系统管理员",
        "13800000099",
        "ADMIN",
        "Abcd1234"
      ));
    };
  }

  private void ensureAccount(AuthService authService, UserMapper userMapper, RegisterCommand command) {
    Long exists = userMapper.selectCount(Wrappers.<UserEntity>lambdaQuery()
      .eq(UserEntity::getStudentNo, command.studentNo()));
    if (exists == null || exists == 0) {
      authService.register(command);
    }
  }
}
