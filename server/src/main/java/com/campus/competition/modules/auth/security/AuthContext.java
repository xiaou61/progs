package com.campus.competition.modules.auth.security;

import java.util.Objects;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class AuthContext {

  public static final String REQUEST_ATTRIBUTE = AuthPrincipal.class.getName();

  private AuthContext() {
  }

  public static AuthPrincipal currentUser() {
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes == null) {
      throw new UnauthorizedException("未登录");
    }
    Object principal = attributes.getRequest().getAttribute(REQUEST_ATTRIBUTE);
    if (principal instanceof AuthPrincipal authPrincipal) {
      return authPrincipal;
    }
    throw new UnauthorizedException("未登录");
  }

  public static void requireRole(String roleCode) {
    if (!Objects.equals(currentUser().roleCode(), roleCode)) {
      throw new ForbiddenException("无权访问该资源");
    }
  }

  public static void requireUser(Long userId) {
    if (userId == null) {
      throw new IllegalArgumentException("用户不能为空");
    }
    if (!Objects.equals(currentUser().userId(), userId)) {
      throw new ForbiddenException("无权访问其他用户数据");
    }
  }

  public static void requireTeacher(Long teacherId) {
    if (teacherId == null) {
      throw new IllegalArgumentException("老师不能为空");
    }
    requireRole("TEACHER");
    if (!Objects.equals(currentUser().userId(), teacherId)) {
      throw new ForbiddenException("无权访问其他老师数据");
    }
  }
}
