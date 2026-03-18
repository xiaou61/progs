package com.campus.competition.modules.message.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.competition.modules.audit.service.ContentAuditService;
import com.campus.competition.modules.auth.mapper.UserMapper;
import com.campus.competition.modules.auth.persistence.UserEntity;
import com.campus.competition.modules.competition.model.CompetitionDetail;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.message.mapper.ConversationMapper;
import com.campus.competition.modules.message.mapper.MessageMapper;
import com.campus.competition.modules.message.mapper.SystemMessageMapper;
import com.campus.competition.modules.message.model.ChatMessageSummary;
import com.campus.competition.modules.message.model.ConversationClearCommand;
import com.campus.competition.modules.message.model.ConversationPreferenceCommand;
import com.campus.competition.modules.message.model.ConsultConversationSummary;
import com.campus.competition.modules.message.model.ConsultTeacherCommand;
import com.campus.competition.modules.message.model.ConversationSummary;
import com.campus.competition.modules.message.model.SendGroupMessageCommand;
import com.campus.competition.modules.message.model.SendPrivateMessageCommand;
import com.campus.competition.modules.message.model.SystemMessageSummary;
import com.campus.competition.modules.message.persistence.ConversationEntity;
import com.campus.competition.modules.message.persistence.MessageEntity;
import com.campus.competition.modules.message.persistence.SystemMessageEntity;
import com.campus.competition.modules.registration.model.RegistrationSummary;
import com.campus.competition.modules.registration.service.RegistrationService;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

  private static final String TYPE_PRIVATE = "PRIVATE";
  private static final String TYPE_GROUP = "GROUP";
  private static final String STATUS_REGISTERED = "REGISTERED";

  private final SystemMessageMapper systemMessageMapper;
  private final ConversationMapper conversationMapper;
  private final MessageMapper messageMapper;
  private final UserMapper userMapper;
  private final CompetitionService competitionService;
  private final RegistrationService registrationService;
  @Autowired(required = false)
  private ContentAuditService contentAuditService;

  public MessageService(
    SystemMessageMapper systemMessageMapper,
    ConversationMapper conversationMapper,
    MessageMapper messageMapper,
    UserMapper userMapper,
    CompetitionService competitionService,
    RegistrationService registrationService
  ) {
    this.systemMessageMapper = systemMessageMapper;
    this.conversationMapper = conversationMapper;
    this.messageMapper = messageMapper;
    this.userMapper = userMapper;
    this.competitionService = competitionService;
    this.registrationService = registrationService;
  }

  public List<SystemMessageSummary> listSystemMessages(Long userId) {
    UserEntity user = getRequiredUser(userId);
    ensureDefaultSystemMessage(user);
    return systemMessageMapper.selectList(Wrappers.<SystemMessageEntity>lambdaQuery()
        .eq(SystemMessageEntity::getUserId, userId)
        .orderByAsc(SystemMessageEntity::getCreatedAt)
        .orderByAsc(SystemMessageEntity::getId))
      .stream()
      .map(entity -> new SystemMessageSummary(
        entity.getId(),
        entity.getTitle(),
        entity.getContent(),
        entity.getBizType(),
        entity.getBizId(),
        Boolean.TRUE.equals(entity.getReadFlag()),
        entity.getCreatedAt()
      ))
      .toList();
  }

  public List<ConversationSummary> listConversations(Long userId) {
    getRequiredUser(userId);
    return conversationMapper.selectList(Wrappers.<ConversationEntity>lambdaQuery()
        .eq(ConversationEntity::getOwnerUserId, userId)
        .orderByDesc(ConversationEntity::getIsPinned)
        .orderByDesc(ConversationEntity::getLastMessageAt)
        .orderByDesc(ConversationEntity::getId))
      .stream()
      .map(this::toConversationSummary)
      .toList();
  }

  public ConversationSummary updatePinned(Long conversationId, ConversationPreferenceCommand command) {
    ConversationEntity conversation = getRequiredOwnedConversation(conversationId, requireUserId(command), "会话不存在");
    conversation.setIsPinned(resolveEnabled(command));
    conversation.setUpdatedAt(LocalDateTime.now());
    conversationMapper.updateById(conversation);
    return toConversationSummary(conversation);
  }

  public ConversationSummary updateMuted(Long conversationId, ConversationPreferenceCommand command) {
    ConversationEntity conversation = getRequiredOwnedConversation(conversationId, requireUserId(command), "会话不存在");
    conversation.setIsMuted(resolveEnabled(command));
    conversation.setUpdatedAt(LocalDateTime.now());
    conversationMapper.updateById(conversation);
    return toConversationSummary(conversation);
  }

  public ConversationSummary clearConversation(Long conversationId, ConversationClearCommand command) {
    Long userId = command == null ? null : command.userId();
    ConversationEntity conversation = getRequiredOwnedConversation(conversationId, userId, "会话不存在");
    LocalDateTime now = LocalDateTime.now();
    conversation.setUnreadCount(0);
    conversation.setLastMessage("");
    conversation.setClearedAt(now);
    conversation.setUpdatedAt(now);
    conversationMapper.updateById(conversation);
    return toConversationSummary(conversation);
  }

  public ConsultConversationSummary consultTeacher(ConsultTeacherCommand command) {
    if (command == null || command.competitionId() == null || command.userId() == null) {
      throw new IllegalArgumentException("咨询参数不能为空");
    }
    CompetitionDetail competition = competitionService.getCompetition(command.competitionId());
    Long teacherId = competition.organizerId();
    if (Objects.equals(teacherId, command.userId())) {
      throw new IllegalArgumentException("不能咨询自己发起的比赛");
    }
    UserEntity teacher = getRequiredUser(teacherId);
    if (!Boolean.TRUE.equals(teacher.getAllowPrivateMessage())) {
      throw new IllegalArgumentException("当前老师暂未开启私信");
    }

    String content = normalizeContent(command.content(), "老师您好，我想咨询比赛相关要求。");
    sendPrivate(new SendPrivateMessageCommand(command.userId(), teacherId, content));
    return new ConsultConversationSummary(TYPE_PRIVATE, teacherId, teacher.getRealName(), competition.id());
  }

  public Long sendPrivate(SendPrivateMessageCommand command) {
    if (command == null || command.senderUserId() == null || command.receiverUserId() == null) {
      throw new IllegalArgumentException("私信参数不能为空");
    }
    UserEntity sender = getRequiredUser(command.senderUserId());
    UserEntity receiver = getRequiredUser(command.receiverUserId());
    if (!Boolean.TRUE.equals(receiver.getAllowPrivateMessage())) {
      throw new IllegalArgumentException("对方暂未开启私信");
    }

    LocalDateTime now = LocalDateTime.now();
    String content = normalizeRequiredContent(command.content());
    if (contentAuditService != null) {
      contentAuditService.auditMessage(sender.getId(), content);
    }

    MessageEntity entity = new MessageEntity();
    entity.setConversationType(TYPE_PRIVATE);
    entity.setSenderUserId(sender.getId());
    entity.setReceiverUserId(receiver.getId());
    entity.setContent(content);
    entity.setCreatedAt(now);
    messageMapper.insert(entity);

    upsertPrivateConversation(sender.getId(), receiver, content, now, 0);
    upsertPrivateConversation(receiver.getId(), sender, content, now, 1);
    return entity.getId();
  }

  public List<ChatMessageSummary> listPrivateMessages(Long userId, Long peerUserId) {
    UserEntity currentUser = getRequiredUser(userId);
    getRequiredUser(peerUserId);
    ConversationEntity conversation = findPrivateConversation(userId, peerUserId);
    List<MessageEntity> messages = messageMapper.selectList(Wrappers.<MessageEntity>lambdaQuery()
      .eq(MessageEntity::getConversationType, TYPE_PRIVATE)
      .and(wrapper -> wrapper
        .and(inner -> inner
          .eq(MessageEntity::getSenderUserId, userId)
          .eq(MessageEntity::getReceiverUserId, peerUserId))
        .or(inner -> inner
          .eq(MessageEntity::getSenderUserId, peerUserId)
          .eq(MessageEntity::getReceiverUserId, userId)))
      .orderByAsc(MessageEntity::getCreatedAt)
      .orderByAsc(MessageEntity::getId)).stream()
      .filter(message -> isVisibleAfterClear(message.getCreatedAt(), conversation))
      .toList();

    clearPrivateUnread(userId, peerUserId);
    Map<Long, String> userNameMap = loadUserNameMap(messages.stream().map(MessageEntity::getSenderUserId).toList());

    return messages.stream()
      .map(message -> new ChatMessageSummary(
        message.getId(),
        message.getConversationType(),
        message.getSenderUserId(),
        userNameMap.getOrDefault(message.getSenderUserId(), "未知用户"),
        message.getReceiverUserId(),
        message.getCompetitionId(),
        message.getContent(),
        message.getCreatedAt(),
        Objects.equals(message.getSenderUserId(), currentUser.getId())
      ))
      .toList();
  }

  public Long sendGroup(SendGroupMessageCommand command) {
    if (command == null || command.competitionId() == null || command.senderUserId() == null) {
      throw new IllegalArgumentException("群聊参数不能为空");
    }
    CompetitionDetail competition = competitionService.getCompetition(command.competitionId());
    UserEntity sender = getRequiredUser(command.senderUserId());
    ensureGroupMember(command.competitionId(), sender.getId(), competition);

    LocalDateTime now = LocalDateTime.now();
    String content = normalizeRequiredContent(command.content());
    if (contentAuditService != null) {
      contentAuditService.auditMessage(sender.getId(), content);
    }

    MessageEntity entity = new MessageEntity();
    entity.setConversationType(TYPE_GROUP);
    entity.setSenderUserId(sender.getId());
    entity.setCompetitionId(command.competitionId());
    entity.setContent(content);
    entity.setCreatedAt(now);
    messageMapper.insert(entity);

    for (Long memberUserId : loadGroupMemberIds(command.competitionId(), competition)) {
      upsertGroupConversation(memberUserId, competition, content, now, Objects.equals(memberUserId, sender.getId()) ? 0 : 1);
    }
    return entity.getId();
  }

  public List<ChatMessageSummary> listGroupMessages(Long competitionId, Long userId) {
    CompetitionDetail competition = competitionService.getCompetition(competitionId);
    ensureGroupMember(competitionId, userId, competition);
    ConversationEntity conversation = findGroupConversation(userId, competitionId);
    List<MessageEntity> messages = messageMapper.selectList(Wrappers.<MessageEntity>lambdaQuery()
      .eq(MessageEntity::getConversationType, TYPE_GROUP)
      .eq(MessageEntity::getCompetitionId, competitionId)
      .orderByAsc(MessageEntity::getCreatedAt)
      .orderByAsc(MessageEntity::getId)).stream()
      .filter(message -> isVisibleAfterClear(message.getCreatedAt(), conversation))
      .toList();

    clearGroupUnread(userId, competitionId);
    Map<Long, String> userNameMap = loadUserNameMap(messages.stream().map(MessageEntity::getSenderUserId).toList());

    return messages.stream()
      .map(message -> new ChatMessageSummary(
        message.getId(),
        message.getConversationType(),
        message.getSenderUserId(),
        userNameMap.getOrDefault(message.getSenderUserId(), "未知用户"),
        message.getReceiverUserId(),
        message.getCompetitionId(),
        message.getContent(),
        message.getCreatedAt(),
        Objects.equals(message.getSenderUserId(), userId)
      ))
      .toList();
  }

  private void ensureDefaultSystemMessage(UserEntity user) {
    Long count = systemMessageMapper.selectCount(Wrappers.<SystemMessageEntity>lambdaQuery()
      .eq(SystemMessageEntity::getUserId, user.getId()));
    if (count != null && count > 0) {
      return;
    }

    SystemMessageEntity entity = new SystemMessageEntity();
    entity.setUserId(user.getId());
    entity.setTitle("欢迎使用校园竞赛平台");
    entity.setContent("你好，" + user.getRealName() + "。消息中心已开通，后续系统通知、比赛提醒和咨询消息都会在这里查看。");
    entity.setBizType("WELCOME");
    entity.setReadFlag(false);
    entity.setCreatedAt(LocalDateTime.now());
    systemMessageMapper.insert(entity);
  }

  private void upsertPrivateConversation(Long ownerUserId, UserEntity peerUser, String content, LocalDateTime lastMessageAt, int unreadDelta) {
    ConversationEntity conversation = conversationMapper.selectOne(Wrappers.<ConversationEntity>lambdaQuery()
      .eq(ConversationEntity::getOwnerUserId, ownerUserId)
      .eq(ConversationEntity::getConversationType, TYPE_PRIVATE)
      .eq(ConversationEntity::getPeerUserId, peerUser.getId()));

    if (conversation == null) {
      conversation = new ConversationEntity();
      conversation.setOwnerUserId(ownerUserId);
      conversation.setConversationType(TYPE_PRIVATE);
      conversation.setPeerUserId(peerUser.getId());
      conversation.setTitle(peerUser.getRealName());
      conversation.setLastMessage(content);
      conversation.setLastMessageAt(lastMessageAt);
      conversation.setUnreadCount(unreadDelta);
      conversation.setIsPinned(false);
      conversation.setIsMuted(false);
      conversation.setClearedAt(null);
      conversation.setCreatedAt(lastMessageAt);
      conversation.setUpdatedAt(lastMessageAt);
      conversationMapper.insert(conversation);
      return;
    }

    conversation.setTitle(peerUser.getRealName());
    conversation.setLastMessage(content);
    conversation.setLastMessageAt(lastMessageAt);
    conversation.setUnreadCount(Math.max(0, conversation.getUnreadCount() + unreadDelta));
    conversation.setUpdatedAt(lastMessageAt);
    conversationMapper.updateById(conversation);
  }

  private void upsertGroupConversation(Long ownerUserId, CompetitionDetail competition, String content, LocalDateTime lastMessageAt, int unreadDelta) {
    ConversationEntity conversation = conversationMapper.selectOne(Wrappers.<ConversationEntity>lambdaQuery()
      .eq(ConversationEntity::getOwnerUserId, ownerUserId)
      .eq(ConversationEntity::getConversationType, TYPE_GROUP)
      .eq(ConversationEntity::getCompetitionId, competition.id()));

    if (conversation == null) {
      conversation = new ConversationEntity();
      conversation.setOwnerUserId(ownerUserId);
      conversation.setConversationType(TYPE_GROUP);
      conversation.setCompetitionId(competition.id());
      conversation.setTitle(competition.title());
      conversation.setLastMessage(content);
      conversation.setLastMessageAt(lastMessageAt);
      conversation.setUnreadCount(unreadDelta);
      conversation.setIsPinned(false);
      conversation.setIsMuted(false);
      conversation.setClearedAt(null);
      conversation.setCreatedAt(lastMessageAt);
      conversation.setUpdatedAt(lastMessageAt);
      conversationMapper.insert(conversation);
      return;
    }

    conversation.setTitle(competition.title());
    conversation.setLastMessage(content);
    conversation.setLastMessageAt(lastMessageAt);
    conversation.setUnreadCount(Math.max(0, conversation.getUnreadCount() + unreadDelta));
    conversation.setUpdatedAt(lastMessageAt);
    conversationMapper.updateById(conversation);
  }

  private void clearPrivateUnread(Long userId, Long peerUserId) {
    ConversationEntity conversation = conversationMapper.selectOne(Wrappers.<ConversationEntity>lambdaQuery()
      .eq(ConversationEntity::getOwnerUserId, userId)
      .eq(ConversationEntity::getConversationType, TYPE_PRIVATE)
      .eq(ConversationEntity::getPeerUserId, peerUserId));
    if (conversation == null || conversation.getUnreadCount() == null || conversation.getUnreadCount() == 0) {
      return;
    }
    conversation.setUnreadCount(0);
    conversation.setUpdatedAt(LocalDateTime.now());
    conversationMapper.updateById(conversation);
  }

  private void clearGroupUnread(Long userId, Long competitionId) {
    ConversationEntity conversation = conversationMapper.selectOne(Wrappers.<ConversationEntity>lambdaQuery()
      .eq(ConversationEntity::getOwnerUserId, userId)
      .eq(ConversationEntity::getConversationType, TYPE_GROUP)
      .eq(ConversationEntity::getCompetitionId, competitionId));
    if (conversation == null || conversation.getUnreadCount() == null || conversation.getUnreadCount() == 0) {
      return;
    }
    conversation.setUnreadCount(0);
    conversation.setUpdatedAt(LocalDateTime.now());
    conversationMapper.updateById(conversation);
  }

  private ConversationEntity findPrivateConversation(Long userId, Long peerUserId) {
    return conversationMapper.selectOne(Wrappers.<ConversationEntity>lambdaQuery()
      .eq(ConversationEntity::getOwnerUserId, userId)
      .eq(ConversationEntity::getConversationType, TYPE_PRIVATE)
      .eq(ConversationEntity::getPeerUserId, peerUserId));
  }

  private ConversationEntity findGroupConversation(Long userId, Long competitionId) {
    return conversationMapper.selectOne(Wrappers.<ConversationEntity>lambdaQuery()
      .eq(ConversationEntity::getOwnerUserId, userId)
      .eq(ConversationEntity::getConversationType, TYPE_GROUP)
      .eq(ConversationEntity::getCompetitionId, competitionId));
  }

  private ConversationEntity getRequiredOwnedConversation(Long conversationId, Long userId, String errorMessage) {
    getRequiredUser(userId);
    if (conversationId == null) {
      throw new IllegalArgumentException("会话不能为空");
    }
    ConversationEntity conversation = conversationMapper.selectOne(Wrappers.<ConversationEntity>lambdaQuery()
      .eq(ConversationEntity::getId, conversationId)
      .eq(ConversationEntity::getOwnerUserId, userId));
    if (conversation == null) {
      throw new IllegalArgumentException(errorMessage);
    }
    return conversation;
  }

  private Long requireUserId(ConversationPreferenceCommand command) {
    if (command == null || command.userId() == null) {
      throw new IllegalArgumentException("用户不能为空");
    }
    return command.userId();
  }

  private boolean resolveEnabled(ConversationPreferenceCommand command) {
    if (command == null || command.enabled() == null) {
      throw new IllegalArgumentException("会话参数不能为空");
    }
    return Boolean.TRUE.equals(command.enabled());
  }

  private boolean isVisibleAfterClear(LocalDateTime createdAt, ConversationEntity conversation) {
    if (conversation == null || conversation.getClearedAt() == null) {
      return true;
    }
    return createdAt != null && createdAt.isAfter(conversation.getClearedAt());
  }

  private void ensureGroupMember(Long competitionId, Long userId, CompetitionDetail competition) {
    getRequiredUser(userId);
    if (Objects.equals(competition.organizerId(), userId)) {
      return;
    }
    boolean joined = registrationService.listByCompetition(competitionId).stream()
      .anyMatch(item -> Objects.equals(item.userId(), userId) && STATUS_REGISTERED.equals(item.status()));
    if (!joined) {
      throw new IllegalArgumentException("当前用户未加入比赛群聊");
    }
  }

  private List<Long> loadGroupMemberIds(Long competitionId, CompetitionDetail competition) {
    LinkedHashSet<Long> memberIds = new LinkedHashSet<>();
    memberIds.add(competition.organizerId());
    registrationService.listByCompetition(competitionId).stream()
      .filter(item -> STATUS_REGISTERED.equals(item.status()))
      .map(RegistrationSummary::userId)
      .forEach(memberIds::add);
    return memberIds.stream().toList();
  }

  private Map<Long, String> loadUserNameMap(List<Long> userIds) {
    List<Long> validIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
    if (validIds.isEmpty()) {
      return Map.of();
    }
    return userMapper.selectBatchIds(validIds).stream()
      .collect(Collectors.toMap(UserEntity::getId, UserEntity::getRealName, (left, right) -> left));
  }

  private UserEntity getRequiredUser(Long userId) {
    if (userId == null) {
      throw new IllegalArgumentException("用户不能为空");
    }
    UserEntity user = userMapper.selectById(userId);
    if (user == null) {
      throw new IllegalArgumentException("用户不存在");
    }
    if (!"ENABLED".equals(user.getStatus())) {
      throw new IllegalArgumentException("用户已停用");
    }
    return user;
  }

  private String normalizeContent(String content, String fallback) {
    String normalized = content == null ? "" : content.trim();
    return normalized.isBlank() ? fallback : normalized;
  }

  private String normalizeRequiredContent(String content) {
    String normalized = normalizeContent(content, "");
    if (normalized.isBlank()) {
      throw new IllegalArgumentException("消息内容不能为空");
    }
    return normalized;
  }

  private ConversationSummary toConversationSummary(ConversationEntity entity) {
    return new ConversationSummary(
      entity.getId(),
      entity.getConversationType(),
      entity.getPeerUserId(),
      entity.getCompetitionId(),
      entity.getTitle(),
      entity.getLastMessage(),
      entity.getLastMessageAt(),
      entity.getUnreadCount() == null ? 0 : entity.getUnreadCount(),
      Boolean.TRUE.equals(entity.getIsPinned()),
      Boolean.TRUE.equals(entity.getIsMuted())
    );
  }
}
