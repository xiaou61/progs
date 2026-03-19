package com.campus.competition.modules.auth.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {

  private static final Logger log = LoggerFactory.getLogger(AuthTokenService.class);
  private static final String HMAC_ALGORITHM = "HmacSHA256";

  private final ObjectMapper objectMapper;
  private final byte[] secretKey;
  private final long expireSeconds;

  public AuthTokenService(
    ObjectMapper objectMapper,
    @Value("${app.auth.token-secret:}") String configuredSecret,
    @Value("${app.auth.token-expire-seconds:43200}") long expireSeconds
  ) {
    this.objectMapper = objectMapper;
    this.secretKey = resolveSecret(configuredSecret);
    this.expireSeconds = expireSeconds;
  }

  public String issueToken(Long userId, String studentNo, String roleCode) {
    long issuedAt = Instant.now().getEpochSecond();
    TokenPayload payload = new TokenPayload(
      1,
      userId,
      studentNo,
      roleCode,
      issuedAt,
      issuedAt + expireSeconds
    );
    String payloadPart = encodePayload(payload);
    String signaturePart = sign(payloadPart);
    return payloadPart + "." + signaturePart;
  }

  public AuthPrincipal parse(String token) {
    if (token == null || token.isBlank()) {
      throw new UnauthorizedException("登录凭证不能为空");
    }

    String[] parts = token.trim().split("\\.");
    if (parts.length != 2) {
      throw new UnauthorizedException("登录凭证无效");
    }

    String payloadPart = parts[0];
    String signaturePart = parts[1];
    String expectedSignature = sign(payloadPart);
    if (!MessageDigest.isEqual(
      expectedSignature.getBytes(StandardCharsets.UTF_8),
      signaturePart.getBytes(StandardCharsets.UTF_8)
    )) {
      throw new UnauthorizedException("登录凭证校验失败");
    }

    TokenPayload payload = decodePayload(payloadPart);
    long now = Instant.now().getEpochSecond();
    if (payload.expireAt() < now) {
      throw new UnauthorizedException("登录已过期，请重新登录");
    }
    if (payload.userId() == null || payload.roleCode() == null || payload.studentNo() == null) {
      throw new UnauthorizedException("登录凭证无效");
    }

    return new AuthPrincipal(payload.userId(), payload.studentNo(), payload.roleCode());
  }

  private String encodePayload(TokenPayload payload) {
    try {
      byte[] bytes = objectMapper.writeValueAsBytes(payload);
      return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("生成登录凭证失败", ex);
    }
  }

  private TokenPayload decodePayload(String payloadPart) {
    try {
      byte[] jsonBytes = Base64.getUrlDecoder().decode(payloadPart);
      return objectMapper.readValue(jsonBytes, TokenPayload.class);
    } catch (Exception ex) {
      throw new UnauthorizedException("登录凭证无效");
    }
  }

  private String sign(String payloadPart) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(new SecretKeySpec(secretKey, HMAC_ALGORITHM));
      byte[] signature = mac.doFinal(payloadPart.getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("登录凭证签名失败", ex);
    }
  }

  private byte[] resolveSecret(String configuredSecret) {
    if (configuredSecret != null && !configuredSecret.isBlank()) {
      return configuredSecret.trim().getBytes(StandardCharsets.UTF_8);
    }

    byte[] bytes = new byte[32];
    new SecureRandom().nextBytes(bytes);
    log.warn("APP_AUTH_TOKEN_SECRET 未配置，当前使用运行时随机密钥。服务重启后旧 token 会失效。");
    return bytes;
  }

  private record TokenPayload(
    int version,
    Long userId,
    String studentNo,
    String roleCode,
    long issuedAt,
    long expireAt
  ) {
  }
}
