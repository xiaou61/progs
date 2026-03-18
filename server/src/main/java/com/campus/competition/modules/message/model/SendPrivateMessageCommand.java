package com.campus.competition.modules.message.model;

public record SendPrivateMessageCommand(
  Long senderUserId,
  Long receiverUserId,
  String content
) {
}
