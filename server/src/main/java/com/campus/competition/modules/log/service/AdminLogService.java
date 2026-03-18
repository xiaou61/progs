package com.campus.competition.modules.log.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.log.mapper.OperationLogMapper;
import com.campus.competition.modules.log.model.OperationLogSummary;
import com.campus.competition.modules.log.persistence.OperationLogEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AdminLogService {

  public static final String DEFAULT_OPERATOR_NAME = "系统管理员";

  private final OperationLogMapper operationLogMapper;

  public AdminLogService(OperationLogMapper operationLogMapper) {
    this.operationLogMapper = operationLogMapper;
  }

  public List<OperationLogSummary> listLogs(String operatorName, String action, String target) {
    String normalizedOperatorName = normalizeFilter(operatorName);
    String normalizedAction = normalizeAction(action);
    String normalizedTarget = normalizeFilter(target);
    return operationLogMapper.selectList(Wrappers.<OperationLogEntity>lambdaQuery()
        .like(normalizedOperatorName != null, OperationLogEntity::getOperatorName, normalizedOperatorName)
        .eq(normalizedAction != null, OperationLogEntity::getAction, normalizedAction)
        .like(normalizedTarget != null, OperationLogEntity::getTarget, normalizedTarget)
        .orderByDesc(OperationLogEntity::getCreatedAt)
        .orderByDesc(OperationLogEntity::getId))
      .stream()
      .map(this::toSummary)
      .toList();
  }

  public String exportCsv(String operatorName, String action, String target) {
    StringBuilder builder = new StringBuilder("operatorName,action,target,detail,createdAt\n");
    for (OperationLogSummary item : listLogs(operatorName, action, target)) {
      builder.append(csv(item.operatorName())).append(',')
        .append(csv(item.action())).append(',')
        .append(csv(item.target())).append(',')
        .append(csv(item.detail())).append(',')
        .append(csv(item.createdAt() == null ? "" : item.createdAt().toString().replace('T', ' ')))
        .append('\n');
    }
    return builder.toString();
  }

  public void record(String action, String target, String detail) {
    OperationLogEntity entity = new OperationLogEntity();
    entity.setOperatorName(DEFAULT_OPERATOR_NAME);
    entity.setAction(normalizeRequired(action, "日志动作不能为空"));
    entity.setTarget(normalizeRequired(target, "日志目标不能为空"));
    entity.setDetail(normalizeOptional(detail));
    entity.setCreatedAt(LocalDateTime.now());
    operationLogMapper.insert(entity);
  }

  private OperationLogSummary toSummary(OperationLogEntity entity) {
    return new OperationLogSummary(
      entity.getId(),
      entity.getOperatorName(),
      entity.getAction(),
      entity.getTarget(),
      entity.getDetail(),
      entity.getCreatedAt()
    );
  }

  private String csv(String value) {
    String normalized = value == null ? "" : value;
    return "\"" + normalized.replace("\"", "\"\"") + "\"";
  }

  private String normalizeRequired(String value, String errorMessage) {
    String normalized = value == null ? "" : value.trim();
    if (normalized.isBlank()) {
      throw new IllegalArgumentException(errorMessage);
    }
    return normalized;
  }

  private String normalizeOptional(String value) {
    String normalized = value == null ? "" : value.trim();
    return normalized.isBlank() ? null : normalized;
  }

  private String normalizeFilter(String value) {
    String normalized = value == null ? "" : value.trim();
    return normalized.isBlank() ? null : normalized;
  }

  private String normalizeAction(String value) {
    String normalized = normalizeFilter(value);
    return normalized == null ? null : normalized.toUpperCase();
  }
}
