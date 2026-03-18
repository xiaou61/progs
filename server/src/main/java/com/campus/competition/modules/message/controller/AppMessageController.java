package com.campus.competition.modules.message.controller;

import com.campus.competition.modules.common.model.ApiResponse;
import com.campus.competition.modules.message.model.ChatMessageSummary;
import com.campus.competition.modules.message.model.ConversationClearCommand;
import com.campus.competition.modules.message.model.ConversationPreferenceCommand;
import com.campus.competition.modules.message.model.ConsultConversationSummary;
import com.campus.competition.modules.message.model.ConsultTeacherCommand;
import com.campus.competition.modules.message.model.ConversationSummary;
import com.campus.competition.modules.message.model.SendGroupMessageCommand;
import com.campus.competition.modules.message.model.SendPrivateMessageCommand;
import com.campus.competition.modules.message.model.SystemMessageSummary;
import com.campus.competition.modules.message.service.MessageService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/messages")
public class AppMessageController {

  private final MessageService messageService;

  public AppMessageController(MessageService messageService) {
    this.messageService = messageService;
  }

  @GetMapping("/system")
  public ApiResponse<List<SystemMessageSummary>> systemMessages(@RequestParam Long userId) {
    return ApiResponse.success(messageService.listSystemMessages(userId));
  }

  @GetMapping("/conversations")
  public ApiResponse<List<ConversationSummary>> conversations(@RequestParam Long userId) {
    return ApiResponse.success(messageService.listConversations(userId));
  }

  @PostMapping("/conversations/{conversationId}/pin")
  public ApiResponse<ConversationSummary> pinConversation(
    @PathVariable Long conversationId,
    @RequestBody ConversationPreferenceCommand command
  ) {
    return ApiResponse.success(messageService.updatePinned(conversationId, command));
  }

  @PostMapping("/conversations/{conversationId}/mute")
  public ApiResponse<ConversationSummary> muteConversation(
    @PathVariable Long conversationId,
    @RequestBody ConversationPreferenceCommand command
  ) {
    return ApiResponse.success(messageService.updateMuted(conversationId, command));
  }

  @PostMapping("/conversations/{conversationId}/clear")
  public ApiResponse<ConversationSummary> clearConversation(
    @PathVariable Long conversationId,
    @RequestBody ConversationClearCommand command
  ) {
    return ApiResponse.success(messageService.clearConversation(conversationId, command));
  }

  @PostMapping("/consult")
  public ApiResponse<ConsultConversationSummary> consult(@RequestBody ConsultTeacherCommand command) {
    return ApiResponse.success(messageService.consultTeacher(command));
  }

  @PostMapping("/private")
  public ApiResponse<Map<String, Long>> sendPrivate(@RequestBody SendPrivateMessageCommand command) {
    return ApiResponse.success(Map.of("messageId", messageService.sendPrivate(command)));
  }

  @GetMapping("/private")
  public ApiResponse<List<ChatMessageSummary>> privateMessages(@RequestParam Long userId, @RequestParam Long peerUserId) {
    return ApiResponse.success(messageService.listPrivateMessages(userId, peerUserId));
  }

  @PostMapping("/group")
  public ApiResponse<Map<String, Long>> sendGroup(@RequestBody SendGroupMessageCommand command) {
    return ApiResponse.success(Map.of("messageId", messageService.sendGroup(command)));
  }

  @GetMapping("/group/{competitionId}")
  public ApiResponse<List<ChatMessageSummary>> groupMessages(
    @PathVariable Long competitionId,
    @RequestParam Long userId
  ) {
    return ApiResponse.success(messageService.listGroupMessages(competitionId, userId));
  }
}
