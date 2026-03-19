package com.campus.competition.modules.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

  private final AuthPathPolicy authPathPolicy;
  private final AuthTokenService authTokenService;

  public AuthInterceptor(AuthPathPolicy authPathPolicy, AuthTokenService authTokenService) {
    this.authPathPolicy = authPathPolicy;
    this.authTokenService = authTokenService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String path = request.getRequestURI();
    String method = request.getMethod();
    if (!authPathPolicy.isProtectedPath(method, path) || authPathPolicy.isPublicPath(method, path)) {
      return true;
    }

    String authorization = request.getHeader("Authorization");
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      throw new UnauthorizedException("请先登录");
    }

    AuthPrincipal principal = authTokenService.parse(authorization.substring("Bearer ".length()).trim());
    if (authPathPolicy.requiresAdmin(path) && !"ADMIN".equals(principal.roleCode())) {
      throw new ForbiddenException("仅管理员可访问该资源");
    }

    request.setAttribute(AuthContext.REQUEST_ATTRIBUTE, principal);
    return true;
  }
}
