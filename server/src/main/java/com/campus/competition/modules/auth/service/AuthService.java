package com.campus.competition.modules.auth.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.model.LoginCommand;
import com.campus.competition.modules.auth.model.LoginResult;
import com.campus.competition.modules.auth.model.RegisterCommand;
import com.campus.competition.modules.auth.model.UserSummary;
import com.campus.competition.modules.auth.persistence.UserEntity;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final AtomicLong idGenerator = new AtomicLong(1);
  private final Map<String, AuthUser> usersByStudentNo = new ConcurrentHashMap<>();
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  private final UserMapper userMapper;

  public AuthService() {
    this.userMapper = null;
  }

  @Autowired
  public AuthService(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  public Long register(RegisterCommand command) {
    validateRegisterCommand(command);
    if (userMapper != null) {
      return registerWithDatabase(command);
    }
    if (usersByStudentNo.containsKey(command.studentNo())) {
      throw new IllegalArgumentException("学号已注册");
    }

    long userId = idGenerator.getAndIncrement();
    AuthUser authUser = new AuthUser(
      userId,
      command.studentNo(),
      command.realName(),
      command.phone(),
      command.roleCode(),
      passwordEncoder.encode(command.password()),
      "ENABLED"
    );
    usersByStudentNo.put(command.studentNo(), authUser);
    return userId;
  }

  public LoginResult login(LoginCommand command) {
    if (userMapper != null) {
      return loginWithDatabase(command);
    }
    AuthUser authUser = usersByStudentNo.get(command.studentNo());
    if (authUser == null) {
      throw new IllegalArgumentException("账号不存在");
    }
    if (!authUser.roleCode().equals(command.roleCode())) {
      throw new IllegalArgumentException("身份不匹配");
    }
    if (!passwordEncoder.matches(command.password(), authUser.passwordHash())) {
      throw new IllegalArgumentException("密码错误");
    }
    return new LoginResult(authUser.id(), authUser.roleCode(), "dev-token-" + authUser.id());
  }

  public List<UserSummary> listUsers() {
    if (userMapper != null) {
      return userMapper.selectList(Wrappers.<UserEntity>lambdaQuery().orderByAsc(UserEntity::getId)).stream()
        .map(user -> new UserSummary(
          user.getId(),
          user.getStudentNo(),
          user.getRealName(),
          user.getPhone(),
          user.getRoleCode(),
          user.getStatus(),
          Boolean.TRUE.equals(user.getViolationMarked()),
          user.getViolationReason()
        ))
        .toList();
    }
    return usersByStudentNo.values().stream()
      .sorted(Comparator.comparing(AuthUser::id))
      .map(user -> new UserSummary(
        user.id(),
        user.studentNo(),
        user.realName(),
        user.phone(),
        user.roleCode(),
        user.status(),
        false,
        null
      ))
      .toList();
  }

  private void validateRegisterCommand(RegisterCommand command) {
    if (command.studentNo() == null || command.studentNo().isBlank()) {
      throw new IllegalArgumentException("学号不能为空");
    }
    if (command.realName() == null || command.realName().isBlank()) {
      throw new IllegalArgumentException("姓名不能为空");
    }
    if (command.password() == null || command.password().length() < 8) {
      throw new IllegalArgumentException("密码长度不能少于 8 位");
    }
  }

  private Long registerWithDatabase(RegisterCommand command) {
    Long exists = userMapper.selectCount(Wrappers.<UserEntity>lambdaQuery()
      .eq(UserEntity::getStudentNo, command.studentNo()));
    if (exists != null && exists > 0) {
      throw new IllegalArgumentException("学号已注册");
    }

    UserEntity entity = new UserEntity();
    entity.setStudentNo(command.studentNo());
    entity.setRealName(command.realName());
    entity.setPhone(command.phone());
    entity.setRoleCode(command.roleCode());
    entity.setPasswordHash(passwordEncoder.encode(command.password()));
    entity.setStatus("ENABLED");
    entity.setViolationMarked(false);
    entity.setViolationReason(null);
    userMapper.insert(entity);
    return entity.getId();
  }

  private LoginResult loginWithDatabase(LoginCommand command) {
    UserEntity user = userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery()
      .eq(UserEntity::getStudentNo, command.studentNo()));
    if (user == null) {
      throw new IllegalArgumentException("账号不存在");
    }
    if (!user.getRoleCode().equals(command.roleCode())) {
      throw new IllegalArgumentException("身份不匹配");
    }
    if (!"ENABLED".equals(user.getStatus())) {
      throw new IllegalArgumentException("账号已停用");
    }
    if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
      throw new IllegalArgumentException("密码错误");
    }
    return new LoginResult(user.getId(), user.getRoleCode(), "dev-token-" + user.getId());
  }

  private record AuthUser(
    Long id,
    String studentNo,
    String realName,
    String phone,
    String roleCode,
    String passwordHash,
    String status
  ) {
  }
}
