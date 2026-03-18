package com.campus.competition.modules.profile.service;

import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.persistence.UserEntity;
import com.campus.competition.modules.profile.mapper.FeedbackMapper;
import com.campus.competition.modules.profile.model.CancelAccountCommand;
import com.campus.competition.modules.profile.model.ChangePasswordCommand;
import com.campus.competition.modules.profile.model.FeedbackCommand;
import com.campus.competition.modules.profile.model.ProfileSummary;
import com.campus.competition.modules.profile.model.UpdateProfileCommand;
import com.campus.competition.modules.profile.persistence.FeedbackEntity;
import java.time.LocalDateTime;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

  private final UserMapper userMapper;
  private final FeedbackMapper feedbackMapper;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public ProfileService(UserMapper userMapper, FeedbackMapper feedbackMapper) {
    this.userMapper = userMapper;
    this.feedbackMapper = feedbackMapper;
  }

  public ProfileSummary getProfile(Long userId) {
    return toSummary(getUser(userId));
  }

  public ProfileSummary updateProfile(UpdateProfileCommand command) {
    if (command.userId() == null) {
      throw new IllegalArgumentException("用户不能为空");
    }
    if (command.realName() == null || command.realName().isBlank()) {
      throw new IllegalArgumentException("姓名不能为空");
    }
    if (command.phone() == null || command.phone().isBlank()) {
      throw new IllegalArgumentException("手机号不能为空");
    }

    UserEntity user = getUser(command.userId());
    user.setRealName(command.realName().trim());
    user.setPhone(command.phone().trim());
    user.setAvatarUrl(normalizeText(command.avatarUrl()));
    user.setCampusName(normalizeText(command.campusName()));
    user.setGradeName(normalizeText(command.gradeName()));
    user.setMajorName(normalizeText(command.majorName()));
    user.setDepartmentName(normalizeText(command.departmentName()));
    user.setBio(normalizeText(command.bio()));
    user.setNotifyResult(defaultBoolean(command.notifyResult(), user.getNotifyResult(), true));
    user.setNotifyPoints(defaultBoolean(command.notifyPoints(), user.getNotifyPoints(), true));
    user.setAllowPrivateMessage(defaultBoolean(command.allowPrivateMessage(), user.getAllowPrivateMessage(), true));
    user.setPublicCompetition(defaultBoolean(command.publicCompetition(), user.getPublicCompetition(), true));
    user.setPublicPoints(defaultBoolean(command.publicPoints(), user.getPublicPoints(), true));
    user.setPublicSubmission(defaultBoolean(command.publicSubmission(), user.getPublicSubmission(), true));
    user.setUpdatedAt(LocalDateTime.now());
    userMapper.updateById(user);
    return toSummary(user);
  }

  public boolean changePassword(ChangePasswordCommand command) {
    if (command.userId() == null) {
      throw new IllegalArgumentException("用户不能为空");
    }
    if (command.oldPassword() == null || command.oldPassword().isBlank()) {
      throw new IllegalArgumentException("旧密码不能为空");
    }
    if (command.newPassword() == null || command.newPassword().length() < 8) {
      throw new IllegalArgumentException("新密码长度不能少于 8 位");
    }

    UserEntity user = getUser(command.userId());
    if (!passwordEncoder.matches(command.oldPassword(), user.getPasswordHash())) {
      throw new IllegalArgumentException("旧密码错误");
    }
    user.setPasswordHash(passwordEncoder.encode(command.newPassword()));
    user.setUpdatedAt(LocalDateTime.now());
    userMapper.updateById(user);
    return true;
  }

  public Long submitFeedback(FeedbackCommand command) {
    if (command.userId() == null) {
      throw new IllegalArgumentException("用户不能为空");
    }
    if (command.content() == null || command.content().isBlank()) {
      throw new IllegalArgumentException("反馈内容不能为空");
    }

    getUser(command.userId());

    FeedbackEntity entity = new FeedbackEntity();
    entity.setUserId(command.userId());
    entity.setContent(command.content().trim());
    entity.setImageUrls(command.imageUrls() == null ? "" : String.join("||", command.imageUrls()));
    entity.setStatus("SUBMITTED");
    entity.setCreatedAt(LocalDateTime.now());
    feedbackMapper.insert(entity);
    return entity.getId();
  }

  public boolean cancelAccount(CancelAccountCommand command) {
    if (command.userId() == null) {
      throw new IllegalArgumentException("用户不能为空");
    }
    if (!"确认注销".equals(command.confirmText())) {
      throw new IllegalArgumentException("注销确认语不正确");
    }

    UserEntity user = getUser(command.userId());
    user.setStatus("CANCELLED");
    user.setAllowPrivateMessage(false);
    user.setNotifyPoints(false);
    user.setNotifyResult(false);
    user.setUpdatedAt(LocalDateTime.now());
    userMapper.updateById(user);
    return true;
  }

  private UserEntity getUser(Long userId) {
    UserEntity user = userMapper.selectById(userId);
    if (user == null) {
      throw new IllegalArgumentException("用户不存在");
    }
    return user;
  }

  private ProfileSummary toSummary(UserEntity user) {
    return new ProfileSummary(
      user.getId(),
      user.getStudentNo(),
      user.getRealName(),
      user.getPhone(),
      defaultText(user.getAvatarUrl()),
      defaultText(user.getCampusName()),
      defaultText(user.getGradeName()),
      defaultText(user.getMajorName()),
      defaultText(user.getDepartmentName()),
      defaultText(user.getBio()),
      user.getRoleCode(),
      user.getNotifyResult() == null || user.getNotifyResult(),
      user.getNotifyPoints() == null || user.getNotifyPoints(),
      user.getAllowPrivateMessage() == null || user.getAllowPrivateMessage(),
      user.getPublicCompetition() == null || user.getPublicCompetition(),
      user.getPublicPoints() == null || user.getPublicPoints(),
      user.getPublicSubmission() == null || user.getPublicSubmission(),
      user.getStatus()
    );
  }

  private String normalizeText(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String defaultText(String value) {
    return value == null ? "" : value;
  }

  private Boolean defaultBoolean(Boolean incoming, Boolean current, boolean defaultValue) {
    if (incoming != null) {
      return incoming;
    }
    if (current != null) {
      return current;
    }
    return defaultValue;
  }
}
