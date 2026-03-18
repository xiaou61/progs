package com.campus.competition.modules.audit.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.audit.mapper.ViolationRecordMapper;
import com.campus.competition.modules.audit.model.AuditRuleSummary;
import com.campus.competition.modules.audit.model.ViolationRecordSummary;
import com.campus.competition.modules.audit.persistence.ViolationRecordEntity;
import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.persistence.UserEntity;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ContentAuditService {

  private static final String SCENE_COMPETITION = "COMPETITION";
  private static final String SCENE_MESSAGE = "MESSAGE";
  private static final String SCENE_SUBMISSION = "SUBMISSION";
  private static final String COMPETITION_MESSAGE = "比赛内容包含敏感词，请修改后再提交";
  private static final String CHAT_MESSAGE = "消息内容包含敏感词，请修改后再发送";
  private static final String SUBMISSION_MESSAGE = "作品文件类型不支持，请上传白名单格式文件";
  private static final List<String> SENSITIVE_WORDS = List.of("作弊", "刷分", "代考", "枪手");
  private static final List<String> ALLOWED_SUBMISSION_EXTENSIONS = List.of(
    "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "zip", "jpg", "jpeg", "png", "mp4"
  );
  private static final List<String> BLOCKED_SUBMISSION_EXTENSIONS = List.of("exe", "bat", "cmd", "msi", "js", "sh");

  private final ViolationRecordMapper violationRecordMapper;
  private final UserMapper userMapper;

  public ContentAuditService(ViolationRecordMapper violationRecordMapper, UserMapper userMapper) {
    this.violationRecordMapper = violationRecordMapper;
    this.userMapper = userMapper;
  }

  public void auditCompetition(Long userId, String title, String description) {
    List<String> hitWords = findSensitiveWords(joinText(title, description));
    if (!hitWords.isEmpty()) {
      recordViolation(SCENE_COMPETITION, null, userId, COMPETITION_MESSAGE, hitWords, joinText(title, description));
      throw new IllegalArgumentException(COMPETITION_MESSAGE);
    }
  }

  public void auditMessage(Long userId, String content) {
    List<String> hitWords = findSensitiveWords(content);
    if (!hitWords.isEmpty()) {
      recordViolation(SCENE_MESSAGE, null, userId, CHAT_MESSAGE, hitWords, content);
      throw new IllegalArgumentException(CHAT_MESSAGE);
    }
  }

  public void auditSubmission(Long competitionId, Long userId, String fileUrl) {
    String extension = extractExtension(fileUrl);
    if (!ALLOWED_SUBMISSION_EXTENSIONS.contains(extension)) {
      recordViolation(
        SCENE_SUBMISSION,
        competitionId,
        userId,
        SUBMISSION_MESSAGE,
        extension.isBlank() ? List.of("unknown") : List.of(extension),
        fileUrl
      );
      throw new IllegalArgumentException(SUBMISSION_MESSAGE);
    }
  }

  public AuditRuleSummary getRuleSummary() {
    return new AuditRuleSummary(
      SENSITIVE_WORDS,
      ALLOWED_SUBMISSION_EXTENSIONS,
      BLOCKED_SUBMISSION_EXTENSIONS
    );
  }

  public List<ViolationRecordSummary> listViolationRecords() {
    List<ViolationRecordEntity> entities = violationRecordMapper.selectList(
      Wrappers.<ViolationRecordEntity>lambdaQuery()
        .orderByDesc(ViolationRecordEntity::getId)
    );
    Map<Long, String> userNameMap = loadUserNameMap(entities.stream().map(ViolationRecordEntity::getUserId).toList());
    return entities.stream()
      .map(entity -> new ViolationRecordSummary(
        entity.getId(),
        entity.getScene(),
        entity.getBizId(),
        entity.getUserId(),
        userNameMap.getOrDefault(entity.getUserId(), "未知用户"),
        entity.getReason(),
        splitHitWords(entity.getHitWords()),
        entity.getContentSnippet(),
        entity.getCreatedAt()
      ))
      .toList();
  }

  private void recordViolation(
    String scene,
    Long bizId,
    Long userId,
    String reason,
    List<String> hitWords,
    String content
  ) {
    ViolationRecordEntity entity = new ViolationRecordEntity();
    entity.setScene(scene);
    entity.setBizId(bizId);
    entity.setUserId(userId);
    entity.setReason(reason);
    entity.setHitWords(String.join(",", hitWords));
    entity.setContentSnippet(toSnippet(content));
    entity.setCreatedAt(LocalDateTime.now());
    violationRecordMapper.insert(entity);
  }

  private List<String> findSensitiveWords(String content) {
    if (content == null || content.isBlank()) {
      return List.of();
    }
    LinkedHashSet<String> hitWords = new LinkedHashSet<>();
    for (String word : SENSITIVE_WORDS) {
      if (content.contains(word)) {
        hitWords.add(word);
      }
    }
    return hitWords.stream().toList();
  }

  private Map<Long, String> loadUserNameMap(List<Long> userIds) {
    List<Long> validIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
    if (validIds.isEmpty()) {
      return Map.of();
    }
    return userMapper.selectBatchIds(validIds).stream()
      .collect(Collectors.toMap(UserEntity::getId, UserEntity::getRealName, (left, right) -> left));
  }

  private List<String> splitHitWords(String hitWords) {
    if (hitWords == null || hitWords.isBlank()) {
      return List.of();
    }
    return Arrays.stream(hitWords.split(","))
      .map(String::trim)
      .filter(item -> !item.isBlank())
      .toList();
  }

  private String joinText(String first, String second) {
    String left = first == null ? "" : first.trim();
    String right = second == null ? "" : second.trim();
    return (left + " " + right).trim();
  }

  private String extractExtension(String fileUrl) {
    if (fileUrl == null || fileUrl.isBlank()) {
      return "";
    }
    String sanitized = fileUrl.trim();
    int queryIndex = sanitized.indexOf('?');
    if (queryIndex >= 0) {
      sanitized = sanitized.substring(0, queryIndex);
    }
    int hashIndex = sanitized.indexOf('#');
    if (hashIndex >= 0) {
      sanitized = sanitized.substring(0, hashIndex);
    }
    int dotIndex = sanitized.lastIndexOf('.');
    if (dotIndex < 0 || dotIndex == sanitized.length() - 1) {
      return "";
    }
    return sanitized.substring(dotIndex + 1).toLowerCase();
  }

  private String toSnippet(String content) {
    String normalized = content == null ? "" : content.replaceAll("\\s+", " ").trim();
    if (normalized.length() <= 120) {
      return normalized;
    }
    return normalized.substring(0, 117) + "...";
  }
}
